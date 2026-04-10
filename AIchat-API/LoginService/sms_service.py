import json
from dataclasses import dataclass

from alibabacloud_dypnsapi20170525 import models as dypns_models
from alibabacloud_dypnsapi20170525.client import Client as DypnsClient
from alibabacloud_tea_openapi import models as open_api_models

from config import Settings


@dataclass(frozen=True)
class SmsServiceError(Exception):
    message: str
    code: str | None = None
    status_code: int = 400
    details: dict | None = None

    def __post_init__(self):
        Exception.__init__(self, self.message)


class SmsService:
    def __init__(self, settings: Settings):
        self.settings = settings
        self.client: DypnsClient | None = None

    def _build_client(self) -> DypnsClient:
        config = open_api_models.Config(
            access_key_id=self.settings.aliyun_access_key_id,
            access_key_secret=self.settings.aliyun_access_key_secret,
            region_id=self.settings.sms_region_id,
            endpoint=self.settings.sms_endpoint,
        )
        return DypnsClient(config)

    def _get_client(self) -> DypnsClient:
        if self.client is None:
            self.client = self._build_client()
        return self.client

    def _build_template_param(self) -> str:
        template_payload = {
            self.settings.sms_template_code_field: "##code##",
            self.settings.sms_template_min_field: self.settings.sms_template_min_value,
        }
        return json.dumps(template_payload, ensure_ascii=False)

    @staticmethod
    def _to_dict(tea_model) -> dict:
        if tea_model is None:
            return {}
        if hasattr(tea_model, "to_map"):
            return tea_model.to_map()
        return {"value": str(tea_model)}

    def send_verify_code(
        self,
        phone_number: str,
        country_code: str,
        out_id: str | None = None,
    ) -> dict:
        request = dypns_models.SendSmsVerifyCodeRequest(
            country_code=country_code,
            phone_number=phone_number,
            sign_name=self.settings.sms_sign_name,
            template_code=self.settings.sms_template_code,
            template_param=self._build_template_param(),
            code_length=self.settings.sms_code_length,
            valid_time=self.settings.sms_valid_time_seconds,
            duplicate_policy=self.settings.sms_duplicate_policy,
            scheme_name=self.settings.sms_scheme_name,
            out_id=out_id,
        )

        try:
            response = self._get_client().send_sms_verify_code(request)
        except Exception as exc:
            raise SmsServiceError(
                message="调用阿里云发送验证码接口失败。",
                status_code=502,
                details={"error": str(exc)},
            ) from exc

        body = response.body
        if not body.success or body.code != "OK":
            raise SmsServiceError(
                message=body.message or "阿里云发送验证码失败。",
                code=body.code,
                status_code=400,
                details={"response": self._to_dict(body)},
            )

        model = body.model
        return {
            "requestId": (
                getattr(model, "request_id", None) or getattr(body, "request_id", None)
            ),
            "bizId": getattr(model, "biz_id", None),
            "outId": getattr(model, "out_id", None),
            "validTimeSeconds": self.settings.sms_valid_time_seconds,
            "codeLength": self.settings.sms_code_length,
        }

    def check_verify_code(
        self,
        phone_number: str,
        verify_code: str,
        country_code: str,
        out_id: str | None = None,
    ) -> dict:
        request = dypns_models.CheckSmsVerifyCodeRequest(
            country_code=country_code,
            phone_number=phone_number,
            verify_code=verify_code,
            out_id=out_id,
            scheme_name=self.settings.sms_scheme_name,
            case_auth_policy=self.settings.sms_case_auth_policy,
        )

        try:
            response = self._get_client().check_sms_verify_code(request)
        except Exception as exc:
            raise SmsServiceError(
                message="调用阿里云校验验证码接口失败。",
                status_code=502,
                details={"error": str(exc)},
            ) from exc

        body = response.body
        if not body.success or body.code != "OK":
            raise SmsServiceError(
                message=body.message or "阿里云校验验证码失败。",
                code=body.code,
                status_code=400,
                details={"response": self._to_dict(body)},
            )

        verify_result = getattr(body.model, "verify_result", None)
        return {
            "passed": verify_result == "PASS",
            "verifyResult": verify_result,
            "outId": getattr(body.model, "out_id", None),
        }
