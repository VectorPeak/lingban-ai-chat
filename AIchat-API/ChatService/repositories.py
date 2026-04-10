import json
from typing import Any

from config import Settings
from db import DatabaseError, get_connection
from roles import get_default_role_key, get_default_role_map, get_default_roles, to_public_role


class RepositoryError(Exception):
    def __init__(self, message: str):
        super().__init__(message)
        self.message = message


class ChatRepository:
    def __init__(self, settings: Settings):
        self.settings = settings
        self._role_order_map = {
            role["role_key"]: index for index, role in enumerate(get_default_roles())
        }

    def list_roles(self) -> list[dict]:
        if not self.settings.database_configured:
            return [to_public_role(role) for role in get_default_roles()]

        try:
            with get_connection(self.settings) as connection:
                self._sync_default_roles(connection)
                with connection.cursor() as cursor:
                    cursor.execute(
                        """
                        SELECT
                            id,
                            role_key,
                            nickname,
                            archetype,
                            avatar_url,
                            background_url,
                            persona_summary,
                            opening_message,
                            system_prompt,
                            sort_order
                        FROM chat_roles
                        WHERE is_active = 1
                        ORDER BY sort_order ASC, id ASC
                        """
                    )
                    rows = cursor.fetchall()
                connection.commit()
                ordered_rows = sorted(
                    rows,
                    key=lambda row: (
                        row["sort_order"],
                        self._role_order_map.get(row["role_key"], 10**6),
                        row["id"],
                    ),
                )
                return [to_public_role(row) for row in ordered_rows]
        except DatabaseError as exc:
            raise RepositoryError(exc.message) from exc

    def get_role(self, role_key: str) -> dict:
        role_key = (role_key or get_default_role_key()).strip().lower()
        if not self.settings.database_configured:
            defaults = get_default_role_map()
            if role_key not in defaults:
                raise RepositoryError("指定的聊天角色不存在。")
            role = defaults[role_key]
            role["id"] = None
            return role

        try:
            with get_connection(self.settings) as connection:
                self._sync_default_roles(connection)
                with connection.cursor() as cursor:
                    cursor.execute(
                        """
                        SELECT
                            id,
                            role_key,
                            nickname,
                            archetype,
                            avatar_url,
                            background_url,
                            persona_summary,
                            opening_message,
                            system_prompt,
                            sort_order
                        FROM chat_roles
                        WHERE role_key = %s AND is_active = 1
                        LIMIT 1
                        """,
                        (role_key,),
                    )
                    row = cursor.fetchone()
                connection.commit()
        except DatabaseError as exc:
            raise RepositoryError(exc.message) from exc

        if not row:
            raise RepositoryError("指定的聊天角色不存在。")
        return row

    def get_or_create_user(self, country_code: str, phone_number: str) -> dict:
        self._require_database()

        try:
            with get_connection(self.settings) as connection:
                with connection.cursor() as cursor:
                    cursor.execute(
                        """
                        INSERT INTO app_users (
                            country_code,
                            phone_number,
                            display_name,
                            status,
                            last_login_at
                        )
                        VALUES (%s, %s, %s, %s, NOW())
                        ON DUPLICATE KEY UPDATE
                            display_name = VALUES(display_name),
                            status = 1,
                            last_login_at = NOW(),
                            updated_at = NOW(),
                            id = LAST_INSERT_ID(id)
                        """,
                        (country_code, phone_number, phone_number, 1),
                    )
                    user_id = cursor.lastrowid
                    cursor.execute(
                        """
                        SELECT id, country_code, phone_number, display_name
                        FROM app_users
                        WHERE id = %s
                        LIMIT 1
                        """,
                        (user_id,),
                    )
                    row = cursor.fetchone()
                connection.commit()
        except DatabaseError as exc:
            raise RepositoryError(exc.message) from exc

        if not row:
            raise RepositoryError("创建或读取用户信息失败。")
        return row

    def get_or_create_active_conversation(self, user_id: int, role_id: int | None) -> dict:
        self._require_database()
        if role_id is None:
            raise RepositoryError("数据库未准备好聊天角色数据。")

        try:
            with get_connection(self.settings) as connection:
                with connection.cursor() as cursor:
                    cursor.execute(
                        """
                        SELECT id, user_id, role_id, status, message_count, last_message_at
                        FROM chat_conversations
                        WHERE user_id = %s AND role_id = %s AND status = 'active'
                        ORDER BY id DESC
                        LIMIT 1
                        """,
                        (user_id, role_id),
                    )
                    row = cursor.fetchone()
                    if row:
                        connection.commit()
                        return row

                    cursor.execute(
                        """
                        INSERT INTO chat_conversations (
                            user_id,
                            role_id,
                            status,
                            message_count
                        )
                        VALUES (%s, %s, 'active', 0)
                        """,
                        (user_id, role_id),
                    )
                    conversation_id = cursor.lastrowid
                    cursor.execute(
                        """
                        SELECT id, user_id, role_id, status, message_count, last_message_at
                        FROM chat_conversations
                        WHERE id = %s
                        LIMIT 1
                        """,
                        (conversation_id,),
                    )
                    row = cursor.fetchone()
                connection.commit()
        except DatabaseError as exc:
            raise RepositoryError(exc.message) from exc

        if not row:
            raise RepositoryError("创建当前聊天会话失败。")
        return row

    def list_current_history(self, user_id: int, role_id: int | None, limit: int) -> dict:
        self._require_database()
        if role_id is None:
            raise RepositoryError("数据库未准备好聊天角色数据。")

        try:
            with get_connection(self.settings) as connection:
                with connection.cursor() as cursor:
                    cursor.execute(
                        """
                        SELECT id, user_id, role_id, status, message_count, last_message_at
                        FROM chat_conversations
                        WHERE user_id = %s AND role_id = %s AND status = 'active'
                        ORDER BY id DESC
                        LIMIT 1
                        """,
                        (user_id, role_id),
                    )
                    conversation = cursor.fetchone()
                    if not conversation:
                        connection.commit()
                        return {"conversation": None, "messages": []}

                    cursor.execute(
                        """
                        SELECT
                            id,
                            sender_type,
                            content_text,
                            model_name,
                            has_image,
                            created_at
                        FROM chat_messages
                        WHERE conversation_id = %s
                        ORDER BY id DESC
                        LIMIT %s
                        """,
                        (conversation["id"], limit),
                    )
                    rows = list(reversed(cursor.fetchall()))
                connection.commit()
        except DatabaseError as exc:
            raise RepositoryError(exc.message) from exc

        return {"conversation": conversation, "messages": rows}

    def list_messages_for_prompt(self, conversation_id: int, limit: int) -> list[dict]:
        self._require_database()

        try:
            with get_connection(self.settings) as connection:
                with connection.cursor() as cursor:
                    cursor.execute(
                        """
                        SELECT sender_type, content_text
                        FROM chat_messages
                        WHERE conversation_id = %s
                          AND sender_type IN ('user', 'assistant')
                        ORDER BY id DESC
                        LIMIT %s
                        """,
                        (conversation_id, limit),
                    )
                    rows = list(reversed(cursor.fetchall()))
                connection.commit()
        except DatabaseError as exc:
            raise RepositoryError(exc.message) from exc

        messages: list[dict] = []
        for row in rows:
            messages.append(
                {
                    "role": "assistant" if row["sender_type"] == "assistant" else "user",
                    "content": row["content_text"],
                }
            )
        return messages

    def append_message(
        self,
        conversation_id: int,
        user_id: int,
        role_id: int | None,
        sender_type: str,
        content_text: str,
        model_name: str | None = None,
        has_image: bool = False,
        extra_json: dict[str, Any] | None = None,
        token_usage_json: dict[str, Any] | None = None,
    ) -> dict:
        self._require_database()
        if role_id is None:
            raise RepositoryError("数据库未准备好聊天角色数据。")

        try:
            with get_connection(self.settings) as connection:
                with connection.cursor() as cursor:
                    cursor.execute(
                        """
                        INSERT INTO chat_messages (
                            conversation_id,
                            user_id,
                            role_id,
                            sender_type,
                            content_text,
                            model_name,
                            has_image,
                            extra_json,
                            token_usage_json
                        )
                        VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)
                        """,
                        (
                            conversation_id,
                            user_id,
                            role_id,
                            sender_type,
                            content_text,
                            model_name,
                            1 if has_image else 0,
                            json.dumps(extra_json, ensure_ascii=False) if extra_json else None,
                            json.dumps(token_usage_json, ensure_ascii=False)
                            if token_usage_json
                            else None,
                        ),
                    )
                    message_id = cursor.lastrowid
                    cursor.execute(
                        """
                        UPDATE chat_conversations
                        SET
                            message_count = message_count + 1,
                            last_message_at = NOW(),
                            updated_at = NOW()
                        WHERE id = %s
                        """,
                        (conversation_id,),
                    )
                    cursor.execute(
                        """
                        SELECT id, sender_type, content_text, model_name, has_image, created_at
                        FROM chat_messages
                        WHERE id = %s
                        LIMIT 1
                        """,
                        (message_id,),
                    )
                    row = cursor.fetchone()
                connection.commit()
        except DatabaseError as exc:
            raise RepositoryError(exc.message) from exc

        if not row:
            raise RepositoryError("写入聊天消息失败。")
        return row

    def clear_current_conversation(self, user_id: int, role_id: int | None) -> int:
        self._require_database()
        if role_id is None:
            raise RepositoryError("数据库未准备好聊天角色数据。")

        try:
            with get_connection(self.settings) as connection:
                with connection.cursor() as cursor:
                    cursor.execute(
                        """
                        UPDATE chat_conversations
                        SET
                            status = 'cleared',
                            cleared_at = NOW(),
                            updated_at = NOW()
                        WHERE user_id = %s AND role_id = %s AND status = 'active'
                        """,
                        (user_id, role_id),
                    )
                    affected_rows = cursor.rowcount
                connection.commit()
        except DatabaseError as exc:
            raise RepositoryError(exc.message) from exc

        return affected_rows

    def _sync_default_roles(self, connection):
        roles = get_default_roles()
        active_role_keys = [role["role_key"] for role in roles]
        with connection.cursor() as cursor:
            cursor.executemany(
                """
                INSERT INTO chat_roles (
                    role_key,
                    nickname,
                    archetype,
                    avatar_url,
                    background_url,
                    persona_summary,
                    opening_message,
                    system_prompt,
                    sort_order,
                    is_active
                )
                VALUES (
                    %(role_key)s,
                    %(nickname)s,
                    %(archetype)s,
                    %(avatar_url)s,
                    %(background_url)s,
                    %(persona_summary)s,
                    %(opening_message)s,
                    %(system_prompt)s,
                    %(sort_order)s,
                    1
                )
                ON DUPLICATE KEY UPDATE
                    nickname = VALUES(nickname),
                    archetype = VALUES(archetype),
                    avatar_url = VALUES(avatar_url),
                    background_url = VALUES(background_url),
                    persona_summary = VALUES(persona_summary),
                    opening_message = VALUES(opening_message),
                    system_prompt = VALUES(system_prompt),
                    sort_order = VALUES(sort_order),
                    is_active = 1,
                    updated_at = NOW()
                """,
                roles,
            )
            if active_role_keys:
                placeholders = ", ".join(["%s"] * len(active_role_keys))
                cursor.execute(
                    f"""
                    UPDATE chat_roles
                    SET
                        is_active = 0,
                        updated_at = NOW()
                    WHERE role_key NOT IN ({placeholders})
                    """,
                    active_role_keys,
                )

    def _require_database(self):
        if not self.settings.database_configured:
            raise RepositoryError("聊天服务尚未配置 MySQL 数据库。")
