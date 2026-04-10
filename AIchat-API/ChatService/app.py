import json
from typing import Any

from flask import Flask, Response, jsonify, request, stream_with_context

from auth import LoginTokenError, verify_login_token
from config import get_settings
from db import DatabaseError, ping_database
from llm_service import BailianChatService, ChatExecution, ChatServiceError
from repositories import ChatRepository, RepositoryError
from roles import get_default_role_key, get_default_role_map, to_public_role
from weather_service import QWeatherService

app = Flask(__name__)
settings = get_settings()
chat_service = BailianChatService(settings)
chat_repository = ChatRepository(settings)
weather_service = QWeatherService(settings)


def _json_error(message: str, status_code: int = 400, **extra):
    payload = {"success": False, "message": message}
    payload.update(extra)
    return jsonify(payload), status_code


def _get_json_body():
    return request.get_json(silent=True) or {}


def _get_query_limit(default_value: int, max_value: int = 200) -> int:
    raw_limit = request.args.get("limit")
    if not raw_limit:
        return default_value
    try:
        return max(1, min(int(raw_limit), max_value))
    except ValueError:
        return default_value


def _sse_message(data: Any) -> str:
    return f"data: {json.dumps(data, ensure_ascii=False)}\n\n"


def _as_bool(value: Any, default: bool = False) -> bool:
    if value is None:
        return default
    if isinstance(value, bool):
        return value
    if isinstance(value, str):
        return value.strip().lower() in {"1", "true", "yes", "on"}
    return bool(value)


def _extract_user_message(payload: dict) -> str:
    direct_message = str(payload.get("message") or "").strip()
    if direct_message:
        return direct_message

    messages = payload.get("messages")
    if not isinstance(messages, list):
        return ""

    for item in reversed(messages):
        if not isinstance(item, dict) or item.get("role") != "user":
            continue
        content = item.get("content")
        if isinstance(content, str):
            return content.strip()
        if isinstance(content, list):
            text_parts = []
            for part in content:
                if part.get("type") == "text":
                    text_parts.append(part.get("text") or "")
            return "".join(text_parts).strip()
    return ""


def _extract_role_key(payload: dict) -> str:
    role_key = (
        payload.get("roleKey")
        or payload.get("role_key")
        or request.args.get("roleKey")
        or request.args.get("role_key")
        or get_default_role_key()
    )
    return str(role_key).strip().lower()


def _format_user_for_model(user: dict | None) -> str | None:
    if not user:
        return None
    phone_number = user.get("phone_number") or user.get("phoneNumber")
    country_code = user.get("country_code") or user.get("countryCode")
    if not phone_number or not country_code:
        return None
    return f"{country_code}-{phone_number}"


def _check_runtime_config(require_database: bool = False):
    missing = settings.missing_runtime_settings()
    if missing:
        return _json_error(
            "服务缺少必要环境变量配置。",
            500,
            missingSettings=missing,
        )
    if require_database and not settings.database_configured:
        return _json_error(
            "聊天服务尚未配置 MySQL 数据库。",
            500,
            missingSettings=["MYSQL_HOST", "MYSQL_USER", "MYSQL_PASSWORD", "MYSQL_DATABASE"],
        )
    return None


def _extract_bearer_token(payload: dict | None = None) -> str | None:
    auth_header = request.headers.get("Authorization", "").strip()
    if auth_header.startswith("Bearer "):
        return auth_header[7:].strip()
    payload = payload or _get_json_body()
    token = str(payload.get("token") or "").strip()
    return token or None


def _require_user(payload: dict | None = None):
    if not settings.chat_require_login:
        return {"countryCode": "", "phoneNumber": ""}

    token = _extract_bearer_token(payload)
    if not token:
        raise LoginTokenError("缺少登录凭证。")

    return verify_login_token(
        secret_key=settings.login_token_secret,
        token=token,
        algorithm=settings.login_token_algorithm,
        issuer=settings.login_token_issuer,
    )


def _resolve_role(role_key: str) -> dict:
    try:
        return chat_repository.get_role(role_key)
    except RepositoryError:
        defaults = get_default_role_map()
        if role_key not in defaults:
            raise
        role = defaults[role_key]
        role["id"] = None
        return role


def _create_or_load_user(user_claims: dict) -> dict:
    phone_number = str(user_claims.get("phoneNumber") or "").strip()
    country_code = str(user_claims.get("countryCode") or "86").strip()
    if not phone_number:
        raise RepositoryError("JWT 中缺少手机号信息。")
    return chat_repository.get_or_create_user(country_code=country_code, phone_number=phone_number)


@app.after_request
def add_cors_headers(response):
    response.headers["Access-Control-Allow-Origin"] = settings.cors_allow_origin
    response.headers["Access-Control-Allow-Headers"] = "Content-Type, Authorization"
    response.headers["Access-Control-Allow-Methods"] = "GET, POST, DELETE, OPTIONS"
    return response


@app.route("/", methods=["GET"])
def index():
    return jsonify(
        {
            "service": "ChatService",
            "message": "AI 聊天云函数已就绪。",
            "endpoints": {
                "health": "GET /health",
                "roles": "GET /api/chat/roles",
                "chat": "POST /api/chat/completions",
                "stream": "POST /api/chat/stream",
                "history": "GET /api/chat/history",
                "clear": "POST /api/chat/clear",
            },
        }
    )


@app.route("/health", methods=["GET"])
def health():
    database_reachable = False
    if settings.database_configured:
        try:
            database_reachable = ping_database(settings)
        except DatabaseError:
            database_reachable = False

    return jsonify(
        {
            "success": True,
            "service": "ChatService",
            "configured": not bool(settings.missing_runtime_settings()),
            "missingSettings": settings.missing_runtime_settings(),
            "missingOptionalSettings": settings.missing_optional_settings(),
            "authRequired": settings.chat_require_login,
            "databaseConfigured": settings.database_configured,
            "databaseReachable": database_reachable,
            "weatherConfigured": settings.weather_configured,
            "model": settings.dashscope_text_model,
            "models": {
                "text": settings.dashscope_text_model,
                "vision": settings.dashscope_vision_model,
            },
        }
    )


@app.route("/api/chat/roles", methods=["GET"])
def list_roles():
    try:
        roles = chat_repository.list_roles()
    except RepositoryError:
        roles = [to_public_role(role) for role in get_default_role_map().values()]

    return jsonify({"success": True, "data": roles})


@app.route("/api/chat/history", methods=["GET", "OPTIONS"])
def chat_history():
    if request.method == "OPTIONS":
        return ("", 204)

    config_error = _check_runtime_config(require_database=True)
    if config_error:
        return config_error

    try:
        user_claims = _require_user()
        user = _create_or_load_user(user_claims)
        role = _resolve_role(_extract_role_key({}))
        result = chat_repository.list_current_history(
            user_id=user["id"],
            role_id=role.get("id"),
            limit=_get_query_limit(settings.history_page_size),
        )
    except LoginTokenError as exc:
        return _json_error(exc.message, 401)
    except RepositoryError as exc:
        return _json_error(exc.message, 500)

    messages = [
        {
            "id": item["id"],
            "role": "assistant" if item["sender_type"] == "assistant" else "user",
            "content": item["content_text"],
            "hasImage": bool(item["has_image"]),
            "model": item["model_name"],
            "createdAt": item["created_at"].isoformat() if item["created_at"] else None,
        }
        for item in result["messages"]
    ]

    return jsonify(
        {
            "success": True,
            "data": {
                "role": to_public_role(role),
                "conversationId": result["conversation"]["id"] if result["conversation"] else None,
                "messages": messages,
            },
        }
    )


@app.route("/api/chat/clear", methods=["POST", "OPTIONS"])
def clear_chat():
    if request.method == "OPTIONS":
        return ("", 204)

    config_error = _check_runtime_config(require_database=True)
    if config_error:
        return config_error

    payload = _get_json_body()

    try:
        user_claims = _require_user(payload)
        user = _create_or_load_user(user_claims)
        role = _resolve_role(_extract_role_key(payload))
        cleared_rows = chat_repository.clear_current_conversation(
            user_id=user["id"],
            role_id=role.get("id"),
        )
    except LoginTokenError as exc:
        return _json_error(exc.message, 401)
    except RepositoryError as exc:
        return _json_error(exc.message, 500)

    return jsonify(
        {
            "success": True,
            "message": "当前聊天已清空。",
            "data": {
                "role": to_public_role(role),
                "clearedConversationCount": cleared_rows,
            },
        }
    )


@app.route("/api/chat/completions", methods=["POST", "OPTIONS"])
def chat_completions():
    if request.method == "OPTIONS":
        return ("", 204)

    config_error = _check_runtime_config(require_database=True)
    if config_error:
        return config_error

    payload = _get_json_body()
    if _as_bool(payload.get("stream"), False):
        return _handle_stream_chat(payload)
    return _handle_non_stream_chat(payload)


@app.route("/api/chat/stream", methods=["POST", "OPTIONS"])
def chat_stream():
    if request.method == "OPTIONS":
        return ("", 204)

    config_error = _check_runtime_config(require_database=True)
    if config_error:
        return config_error

    payload = _get_json_body()
    return _handle_stream_chat(payload)


def _handle_non_stream_chat(payload: dict):
    try:
        user_claims = _require_user(payload)
        user = _create_or_load_user(user_claims)
        role = _resolve_role(_extract_role_key(payload))
        conversation = chat_repository.get_or_create_active_conversation(
            user_id=user["id"],
            role_id=role.get("id"),
        )
        history_messages = chat_repository.list_messages_for_prompt(
            conversation_id=conversation["id"],
            limit=settings.history_window_size,
        )
        execution = chat_service.create_chat_execution(
            role_prompt=role["system_prompt"],
            history_messages=history_messages,
            user_message=_extract_user_message(payload),
            stream=False,
            model=payload.get("model"),
            temperature=payload.get("temperature"),
            top_p=payload.get("topP"),
            max_tokens=payload.get("maxTokens"),
            presence_penalty=payload.get("presencePenalty"),
            frequency_penalty=payload.get("frequencyPenalty"),
            user=_format_user_for_model(user),
            image_url=str(payload.get("imageUrl") or "").strip() or None,
            image_data_url=str(payload.get("imageDataUrl") or "").strip() or None,
            weather_city=str(payload.get("weatherCity") or "").strip() or None,
            weather_service=weather_service,
        )
        _persist_chat_messages(
            conversation_id=conversation["id"],
            user=user,
            role=role,
            execution=execution,
            user_message=_extract_user_message(payload),
            payload=payload,
        )
    except LoginTokenError as exc:
        return _json_error(exc.message, 401)
    except (RepositoryError, ChatServiceError) as exc:
        status_code = exc.status_code if isinstance(exc, ChatServiceError) else 500
        details = exc.details if isinstance(exc, ChatServiceError) else None
        return _json_error(exc.message, status_code, details=details)

    return jsonify(
        {
            "success": True,
            "data": execution.response_dict,
            "meta": {
                "role": to_public_role(role),
                "conversationId": conversation["id"],
                "assistantMessage": execution.final_text,
                "weather": execution.weather_context,
                "usedToolCall": execution.used_tool_call,
            },
        }
    )


def _handle_stream_chat(payload: dict):
    try:
        user_claims = _require_user(payload)
        user = _create_or_load_user(user_claims)
        role = _resolve_role(_extract_role_key(payload))
        conversation = chat_repository.get_or_create_active_conversation(
            user_id=user["id"],
            role_id=role.get("id"),
        )
        history_messages = chat_repository.list_messages_for_prompt(
            conversation_id=conversation["id"],
            limit=settings.history_window_size,
        )
        execution = chat_service.create_chat_execution(
            role_prompt=role["system_prompt"],
            history_messages=history_messages,
            user_message=_extract_user_message(payload),
            stream=True,
            model=payload.get("model"),
            temperature=payload.get("temperature"),
            top_p=payload.get("topP"),
            max_tokens=payload.get("maxTokens"),
            presence_penalty=payload.get("presencePenalty"),
            frequency_penalty=payload.get("frequencyPenalty"),
            user=_format_user_for_model(user),
            image_url=str(payload.get("imageUrl") or "").strip() or None,
            image_data_url=str(payload.get("imageDataUrl") or "").strip() or None,
            weather_city=str(payload.get("weatherCity") or "").strip() or None,
            weather_service=weather_service,
        )
    except LoginTokenError as exc:
        return _json_error(exc.message, 401)
    except (RepositoryError, ChatServiceError) as exc:
        status_code = exc.status_code if isinstance(exc, ChatServiceError) else 500
        details = exc.details if isinstance(exc, ChatServiceError) else None
        return _json_error(exc.message, status_code, details=details)

    @stream_with_context
    def generate():
        assistant_text_parts: list[str] = []
        usage_payload = execution.usage

        try:
            chat_repository.append_message(
                conversation_id=conversation["id"],
                user_id=user["id"],
                role_id=role.get("id"),
                sender_type="user",
                content_text=_visible_user_message(payload),
                model_name=None,
                has_image=bool(payload.get("imageUrl") or payload.get("imageDataUrl")),
                extra_json={"weatherCity": payload.get("weatherCity")},
            )

            if execution.provider_stream is not None:
                for chunk in execution.provider_stream:
                    payload_dict = chunk.model_dump()
                    choices = payload_dict.get("choices") or []
                    if choices:
                        delta = choices[0].get("delta") or {}
                        if delta.get("content"):
                            assistant_text_parts.append(delta["content"])
                    if payload_dict.get("usage"):
                        usage_payload = payload_dict["usage"]
                    yield _sse_message(payload_dict)
            else:
                for event in execution.synthetic_stream_events or []:
                    choices = event.get("choices") or []
                    if choices:
                        delta = choices[0].get("delta") or {}
                        if delta.get("content"):
                            assistant_text_parts.append(delta["content"])
                    if event.get("usage"):
                        usage_payload = event["usage"]
                    yield _sse_message(event)

            final_text = "".join(assistant_text_parts) or execution.final_text
            chat_repository.append_message(
                conversation_id=conversation["id"],
                user_id=user["id"],
                role_id=role.get("id"),
                sender_type="assistant",
                content_text=final_text,
                model_name=execution.model,
                has_image=execution.has_image,
                extra_json={"weather": execution.weather_context},
                token_usage_json=usage_payload,
            )
            yield "data: [DONE]\n\n"
        except Exception as exc:
            yield _sse_message({"error": {"message": str(exc)}})
            yield "data: [DONE]\n\n"

    response = Response(generate(), mimetype="text/event-stream")
    response.headers["Cache-Control"] = "no-cache"
    response.headers["X-Accel-Buffering"] = "no"
    return response


def _visible_user_message(payload: dict) -> str:
    message = _extract_user_message(payload)
    if message:
        return message
    if payload.get("imageUrl") or payload.get("imageDataUrl"):
        return "[用户发送了一张图片]"
    return ""


def _persist_chat_messages(
    conversation_id: int,
    user: dict,
    role: dict,
    execution: ChatExecution,
    user_message: str,
    payload: dict,
):
    has_image = execution.has_image
    visible_user_message = user_message.strip() or ("[用户发送了一张图片]" if has_image else "")

    chat_repository.append_message(
        conversation_id=conversation_id,
        user_id=user["id"],
        role_id=role.get("id"),
        sender_type="user",
        content_text=visible_user_message,
        model_name=None,
        has_image=has_image,
        extra_json={"weatherCity": payload.get("weatherCity")},
    )
    chat_repository.append_message(
        conversation_id=conversation_id,
        user_id=user["id"],
        role_id=role.get("id"),
        sender_type="assistant",
        content_text=execution.final_text,
        model_name=execution.model,
        has_image=has_image,
        extra_json={"weather": execution.weather_context},
        token_usage_json=execution.usage,
    )


@app.errorhandler(404)
def not_found(_error):
    return _json_error("接口不存在。", 404)


@app.errorhandler(405)
def method_not_allowed(_error):
    return _json_error("请求方法不支持。", 405)


@app.errorhandler(Exception)
def handle_unexpected_error(error):
    return _json_error("服务内部错误。", 500, error=str(error))


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=9000, threaded=True)
