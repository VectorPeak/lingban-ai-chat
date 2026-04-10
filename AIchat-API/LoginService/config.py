import os
from dataclasses import dataclass

DEFAULT_LOGIN_TOKEN_SECRET = "replace-this-with-a-strong-secret"


def _get_int(name: str, default: int) -> int:
    value = os.getenv(name)
    if value in (None, ""):
        return default
    return int(value)


def _get_bool(name: str, default: bool) -> bool:
    value = os.getenv(name)
    if value in (None, ""):
        return default
    return value.strip().lower() in {"1", "true", "yes", "on"}


@dataclass(frozen=True)
class Settings:
    aliyun_access_key_id: str
    aliyun_access_key_secret: str
    sms_region_id: str
    sms_endpoint: str
    sms_country_code: str
    sms_sign_name: str
    sms_template_code: str
    sms_template_code_field: str
    sms_template_min_field: str
    sms_template_min_value: str
    sms_code_length: int
    sms_valid_time_seconds: int
    sms_duplicate_policy: int
    sms_scheme_name: str | None
    sms_case_auth_policy: int
    login_token_secret: str
    login_token_algorithm: str
    login_token_issuer: str
    login_token_expires_seconds: int
    enable_test_login_account: bool
    test_login_phone_number: str
    test_login_verify_code: str
    test_login_country_code: str
    cors_allow_origin: str

    def missing_sms_settings(self) -> list[str]:
        missing: list[str] = []
        if not self.aliyun_access_key_id:
            missing.append("ALIYUN_ACCESS_KEY_ID")
        if not self.aliyun_access_key_secret:
            missing.append("ALIYUN_ACCESS_KEY_SECRET")
        if not self.sms_sign_name:
            missing.append("ALIYUN_SMS_SIGN_NAME")
        if not self.sms_template_code:
            missing.append("ALIYUN_SMS_TEMPLATE_CODE")
        return missing

    def missing_token_settings(self) -> list[str]:
        missing: list[str] = []
        if (
            not self.login_token_secret
            or self.login_token_secret == DEFAULT_LOGIN_TOKEN_SECRET
        ):
            missing.append("LOGIN_TOKEN_SECRET")
        return missing

    def missing_runtime_settings(self) -> list[str]:
        return self.missing_sms_settings() + self.missing_token_settings()


def get_settings() -> Settings:
    return Settings(
        aliyun_access_key_id=os.getenv("ALIYUN_ACCESS_KEY_ID", "").strip(),
        aliyun_access_key_secret=os.getenv("ALIYUN_ACCESS_KEY_SECRET", "").strip(),
        sms_region_id=os.getenv("ALIYUN_SMS_REGION_ID", "cn-hangzhou").strip(),
        sms_endpoint=os.getenv("ALIYUN_SMS_ENDPOINT", "dypnsapi.aliyuncs.com").strip(),
        sms_country_code=os.getenv("ALIYUN_SMS_COUNTRY_CODE", "86").strip(),
        sms_sign_name=os.getenv("ALIYUN_SMS_SIGN_NAME", "速通互联验证码").strip(),
        sms_template_code=os.getenv("ALIYUN_SMS_TEMPLATE_CODE", "100001").strip(),
        sms_template_code_field=os.getenv("ALIYUN_SMS_TEMPLATE_CODE_FIELD", "code").strip(),
        sms_template_min_field=os.getenv("ALIYUN_SMS_TEMPLATE_MIN_FIELD", "min").strip(),
        sms_template_min_value=os.getenv("ALIYUN_SMS_TEMPLATE_MIN_VALUE", "5").strip(),
        sms_code_length=_get_int("ALIYUN_SMS_CODE_LENGTH", 6),
        sms_valid_time_seconds=_get_int("ALIYUN_SMS_VALID_TIME_SECONDS", 300),
        sms_duplicate_policy=_get_int("ALIYUN_SMS_DUPLICATE_POLICY", 1),
        sms_scheme_name=os.getenv("ALIYUN_SMS_SCHEME_NAME", "").strip() or None,
        sms_case_auth_policy=_get_int("ALIYUN_SMS_CASE_AUTH_POLICY", 1),
        login_token_secret=os.getenv(
            "LOGIN_TOKEN_SECRET", DEFAULT_LOGIN_TOKEN_SECRET
        ).strip(),
        login_token_algorithm=os.getenv("LOGIN_TOKEN_ALGORITHM", "HS256").strip(),
        login_token_issuer=os.getenv(
            "LOGIN_TOKEN_ISSUER", "AIchat-API/LoginService"
        ).strip(),
        login_token_expires_seconds=_get_int(
            "LOGIN_TOKEN_EXPIRES_SECONDS", 2592000
        ),
        enable_test_login_account=_get_bool("ENABLE_TEST_LOGIN_ACCOUNT", False),
        test_login_phone_number=os.getenv(
            "TEST_LOGIN_PHONE_NUMBER", "18888888888"
        ).strip(),
        test_login_verify_code=os.getenv("TEST_LOGIN_VERIFY_CODE", "1234").strip(),
        test_login_country_code=os.getenv("TEST_LOGIN_COUNTRY_CODE", "86").strip(),
        cors_allow_origin=os.getenv("CORS_ALLOW_ORIGIN", "*").strip(),
    )
