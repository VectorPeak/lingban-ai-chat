from contextlib import contextmanager

import pymysql
from pymysql.cursors import DictCursor

from config import Settings


class DatabaseError(Exception):
    def __init__(self, message: str):
        super().__init__(message)
        self.message = message


@contextmanager
def get_connection(settings: Settings):
    if not settings.database_configured:
        raise DatabaseError("数据库配置不完整。")

    connection = None
    try:
        connection = pymysql.connect(
            host=settings.mysql_host,
            port=settings.mysql_port,
            user=settings.mysql_user,
            password=settings.mysql_password,
            database=settings.mysql_database,
            charset=settings.mysql_charset,
            connect_timeout=settings.mysql_connect_timeout_seconds,
            cursorclass=DictCursor,
            autocommit=False,
        )
        yield connection
    except pymysql.MySQLError as exc:
        raise DatabaseError(f"MySQL 连接或执行失败: {exc}") from exc
    finally:
        if connection is not None:
            connection.close()


def ping_database(settings: Settings) -> bool:
    with get_connection(settings) as connection:
        with connection.cursor() as cursor:
            cursor.execute("SELECT 1 AS ok")
            cursor.fetchone()
        connection.commit()
    return True
