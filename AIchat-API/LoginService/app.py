import re
from datetime import datetime, timedelta, timezone

from flask import Flask, jsonify, request

from auth import generate_login_token
from config import get_settings
from sms_service import SmsService, SmsServiceError

app = Flask(__name__)
settings = get_settings()
sms_service = SmsService(settings)

PHONE_NUMBER_PATTERN = re.compile(r"^\d{6,15}$")


def _json_error(message: str, status_code: int = 400, **extra):
    payload = {"success": False, "message": message}
    payload.update(extra)
    return jsonify(payload), status_code


def _get_json_body():
    return request.get_json(silent=True) or {}


def _normalize_phone_number(phone_number: str | None) -> str | None:
    if phone_number is None:
        return None
    normalized = "".join(ch for ch in str(phone_number) if ch.isdigit())
    return normalized or None


def _validate_phone_number(phone_number: str | None) -> str | None:
    normalized = _normalize_phone_number(phone_number)
    if not normalized or not PHONE_NUMBER_PATTERN.fullmatch(normalized):
        return None
    return normalized


def _check_runtime_config():
    missing = settings.missing_runtime_settings()
    if missing:
        return _json_error(
            "服务缺少必要环境变量配置。",
            500,
            missingSettings=missing,
        )
    return None


def _check_sms_runtime_config():
    missing = settings.missing_sms_settings()
    if missing:
        return _json_error(
            "服务缺少必要环境变量配置。",
            500,
            missingSettings=missing,
        )
    return None


def _check_token_runtime_config():
    missing = settings.missing_token_settings()
    if missing:
        return _json_error(
            "服务缺少必要环境变量配置。",
            500,
            missingSettings=missing,
        )
    return None


def _is_test_login_phone(phone_number: str | None, country_code: str | None) -> bool:
    return (
        settings.enable_test_login_account
        and bool(phone_number)
        and phone_number == settings.test_login_phone_number
        and str(country_code or settings.sms_country_code).strip()
        == settings.test_login_country_code
    )


def _build_login_response(phone_number: str, country_code: str):
    token = generate_login_token(
        secret_key=settings.login_token_secret,
        phone_number=phone_number,
        country_code=country_code,
        expires_seconds=settings.login_token_expires_seconds,
        algorithm=settings.login_token_algorithm,
        issuer=settings.login_token_issuer,
    )
    expires_at = datetime.now(timezone.utc) + timedelta(
        seconds=settings.login_token_expires_seconds
    )

    return jsonify(
        {
            "success": True,
            "message": "登录成功。",
            "data": {
                "token": token,
                "tokenType": "Bearer",
                "tokenFormat": "JWT",
                "expiresIn": settings.login_token_expires_seconds,
                "expiresAt": expires_at.isoformat(),
                "user": {
                    "phoneNumber": phone_number,
                    "countryCode": country_code,
                },
            },
        }
    )


@app.after_request
def add_cors_headers(response):
    response.headers["Access-Control-Allow-Origin"] = settings.cors_allow_origin
    response.headers["Access-Control-Allow-Headers"] = "Content-Type, Authorization"
    response.headers["Access-Control-Allow-Methods"] = "GET, POST, OPTIONS"
    return response


@app.route("/", methods=["GET"])
def index():
    return jsonify(
        {
            "service": "LoginService",
            "message": "手机号验证码登录云函数已就绪。",
            "tokenFormat": "JWT",
            "endpoints": {
                "health": "GET /health",
                "sendCode": "POST /api/auth/send-code",
                "login": "POST /api/auth/login",
            },
        }
    )


@app.route("/health", methods=["GET"])
def health():
    return jsonify(
        {
            "success": True,
            "service": "LoginService",
            "configured": not bool(settings.missing_runtime_settings()),
            "missingSettings": settings.missing_runtime_settings(),
            "tokenFormat": "JWT",
            "testLoginEnabled": settings.enable_test_login_account,
        }
    )


@app.route("/api/auth/send-code", methods=["POST", "OPTIONS"])
def send_code():
    if request.method == "OPTIONS":
        return ("", 204)

    payload = _get_json_body()
    phone_number = _validate_phone_number(payload.get("phoneNumber"))
    if not phone_number:
        return _json_error("phoneNumber 格式不正确，必须是 6 到 15 位数字。")

    country_code = str(payload.get("countryCode") or settings.sms_country_code).strip()
    out_id = payload.get("outId")

    if _is_test_login_phone(phone_number, country_code):
        return jsonify(
            {
                "success": True,
                "message": "测试账号验证码已就绪。",
                "data": {
                    "requestId": "test-login-request",
                    "bizId": "test-login-biz",
                    "outId": out_id,
                    "validTimeSeconds": settings.sms_valid_time_seconds,
                    "codeLength": len(settings.test_login_verify_code),
                    "isTestAccount": True,
                },
            }
        )

    config_error = _check_sms_runtime_config()
    if config_error:
        return config_error

    try:
        result = sms_service.send_verify_code(
            phone_number=phone_number,
            country_code=country_code,
            out_id=out_id,
        )
    except SmsServiceError as exc:
        return _json_error(
            exc.message,
            exc.status_code,
            code=exc.code,
            details=exc.details,
        )

    return jsonify(
        {
            "success": True,
            "message": "验证码发送成功。",
            "data": result,
        }
    )


@app.route("/api/auth/login", methods=["POST", "OPTIONS"])
def login():
    if request.method == "OPTIONS":
        return ("", 204)

    payload = _get_json_body()
    phone_number = _validate_phone_number(payload.get("phoneNumber"))
    if not phone_number:
        return _json_error("phoneNumber 格式不正确，必须是 6 到 15 位数字。")

    verify_code = str(payload.get("verifyCode") or "").strip()
    if not verify_code:
        return _json_error("verifyCode 不能为空。")

    country_code = str(payload.get("countryCode") or settings.sms_country_code).strip()
    out_id = payload.get("outId")

    if _is_test_login_phone(phone_number, country_code):
        config_error = _check_token_runtime_config()
        if config_error:
            return config_error
        if verify_code != settings.test_login_verify_code:
            return _json_error(
                "验证码校验失败。",
                401,
                verifyResult="TEST_CODE_MISMATCH",
            )
        return _build_login_response(
            phone_number=phone_number,
            country_code=country_code,
        )

    config_error = _check_runtime_config()
    if config_error:
        return config_error

    try:
        check_result = sms_service.check_verify_code(
            phone_number=phone_number,
            verify_code=verify_code,
            country_code=country_code,
            out_id=out_id,
        )
    except SmsServiceError as exc:
        return _json_error(
            exc.message,
            exc.status_code,
            code=exc.code,
            details=exc.details,
        )

    if not check_result["passed"]:
        return _json_error(
            "验证码校验失败。",
            401,
            verifyResult=check_result["verifyResult"],
        )

    return _build_login_response(
        phone_number=phone_number,
        country_code=country_code,
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
    app.run(host="0.0.0.0", port=9000)
