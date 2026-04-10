import os
from dataclasses import dataclass

DEFAULT_LOGIN_TOKEN_SECRET = "replace-this-with-a-strong-secret"


def _get_int(name: str, default: int) -> int:
    value = os.getenv(name)
    if value in (None, ""):
        return default
    return int(value)


def _get_float(name: str, default: float) -> float:
    value = os.getenv(name)
    if value in (None, ""):
        return default
    return float(value)


def _get_bool(name: str, default: bool) -> bool:
    value = os.getenv(name)
    if value in (None, ""):
        return default
    return value.strip().lower() in {"1", "true", "yes", "on"}


@dataclass(frozen=True)
class Settings:
    dashscope_api_key: str
    dashscope_base_url: str
    dashscope_text_model: str
    dashscope_vision_model: str
    default_temperature: float
    request_timeout_seconds: float
    chat_require_login: bool
    login_token_secret: str
    login_token_algorithm: str
    login_token_issuer: str
    mysql_host: str
    mysql_port: int
    mysql_user: str
    mysql_password: str
    mysql_database: str
    mysql_charset: str
    mysql_connect_timeout_seconds: int
    qweather_api_host: str
    qweather_api_key: str
    qweather_timeout_seconds: float
    history_window_size: int
    history_page_size: int
    synthetic_stream_chunk_size: int
    cors_allow_origin: str

    @property
    def database_configured(self) -> bool:
        return all(
            [
                self.mysql_host,
                self.mysql_user,
                self.mysql_password,
                self.mysql_database,
            ]
        )

    @property
    def weather_configured(self) -> bool:
        return bool(self.qweather_api_host and self.qweather_api_key)

    def missing_runtime_settings(self) -> list[str]:
        missing: list[str] = []
        if not self.dashscope_api_key:
            missing.append("DASHSCOPE_API_KEY")
        if not self.database_configured:
            if not self.mysql_host:
                missing.append("MYSQL_HOST")
            if not self.mysql_user:
                missing.append("MYSQL_USER")
            if not self.mysql_password:
                missing.append("MYSQL_PASSWORD")
            if not self.mysql_database:
                missing.append("MYSQL_DATABASE")
        if self.chat_require_login and (
            not self.login_token_secret
            or self.login_token_secret == DEFAULT_LOGIN_TOKEN_SECRET
        ):
            missing.append("LOGIN_TOKEN_SECRET")
        return missing

    def missing_optional_settings(self) -> list[str]:
        missing: list[str] = []
        if not self.qweather_api_host:
            missing.append("QWEATHER_API_HOST")
        if not self.qweather_api_key:
            missing.append("QWEATHER_API_KEY")
        return missing


def get_settings() -> Settings:
    text_model = os.getenv("DASHSCOPE_TEXT_MODEL", "").strip()
    if not text_model:
        text_model = os.getenv("DASHSCOPE_MODEL", "qwen-plus").strip()

    return Settings(
        dashscope_api_key=os.getenv("DASHSCOPE_API_KEY", "").strip(),
        dashscope_base_url=os.getenv(
            "DASHSCOPE_BASE_URL",
            "https://dashscope.aliyuncs.com/compatible-mode/v1",
        ).strip(),
        dashscope_text_model=text_model,
        dashscope_vision_model=os.getenv(
            "DASHSCOPE_VISION_MODEL", "qwen-vl-plus-latest"
        ).strip(),
        default_temperature=_get_float("DASHSCOPE_TEMPERATURE", 0.7),
        request_timeout_seconds=_get_float("DASHSCOPE_REQUEST_TIMEOUT_SECONDS", 25.0),
        chat_require_login=_get_bool("CHAT_REQUIRE_LOGIN", True),
        login_token_secret=os.getenv(
            "LOGIN_TOKEN_SECRET", DEFAULT_LOGIN_TOKEN_SECRET
        ).strip(),
        login_token_algorithm=os.getenv("LOGIN_TOKEN_ALGORITHM", "HS256").strip(),
        login_token_issuer=os.getenv(
            "LOGIN_TOKEN_ISSUER", "AIchat-API/LoginService"
        ).strip(),
        mysql_host=os.getenv("MYSQL_HOST", "").strip(),
        mysql_port=_get_int("MYSQL_PORT", 3306),
        mysql_user=os.getenv("MYSQL_USER", "").strip(),
        mysql_password=os.getenv("MYSQL_PASSWORD", "").strip(),
        mysql_database=os.getenv("MYSQL_DATABASE", "").strip(),
        mysql_charset=os.getenv("MYSQL_CHARSET", "utf8mb4").strip(),
        mysql_connect_timeout_seconds=_get_int("MYSQL_CONNECT_TIMEOUT_SECONDS", 5),
        qweather_api_host=os.getenv("QWEATHER_API_HOST", "").strip(),
        qweather_api_key=os.getenv("QWEATHER_API_KEY", "").strip(),
        qweather_timeout_seconds=_get_float("QWEATHER_TIMEOUT_SECONDS", 10.0),
        history_window_size=_get_int("CHAT_HISTORY_WINDOW_SIZE", 20),
        history_page_size=_get_int("CHAT_HISTORY_PAGE_SIZE", 100),
        synthetic_stream_chunk_size=_get_int("SYNTHETIC_STREAM_CHUNK_SIZE", 16),
        cors_allow_origin=os.getenv("CORS_ALLOW_ORIGIN", "*").strip(),
    )
