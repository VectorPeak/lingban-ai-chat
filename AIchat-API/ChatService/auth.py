from typing import Any

import jwt
from jwt import ExpiredSignatureError, InvalidTokenError


class LoginTokenError(Exception):
    def __init__(self, message: str):
        super().__init__(message)
        self.message = message


def verify_login_token(
    secret_key: str,
    token: str,
    algorithm: str = "HS256",
    issuer: str = "AIchat-API/LoginService",
) -> dict[str, Any]:
    try:
        return jwt.decode(
            token,
            secret_key,
            algorithms=[algorithm],
            issuer=issuer,
            options={"require": ["sub", "iat", "exp", "iss"]},
        )
    except ExpiredSignatureError as exc:
        raise LoginTokenError("登录凭证已过期。") from exc
    except InvalidTokenError as exc:
        raise LoginTokenError("登录凭证无效。") from exc
