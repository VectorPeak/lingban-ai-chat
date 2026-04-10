import json
from dataclasses import dataclass
from typing import Any
from uuid import uuid4

from openai import APIError, APITimeoutError, OpenAI

from config import Settings
from weather_service import QWeatherService, WeatherServiceError


class ChatServiceError(Exception):
    def __init__(
        self,
        message: str,
        status_code: int = 400,
        details: dict | None = None,
    ):
        super().__init__(message)
        self.message = message
        self.status_code = status_code
        self.details = details or {}


@dataclass
class ChatExecution:
    model: str
    final_text: str
    response_dict: dict | None = None
    provider_stream: Any = None
    synthetic_stream_events: list[dict] | None = None
    weather_context: dict | None = None
    used_tool_call: bool = False
    has_image: bool = False
    usage: dict | None = None


class BailianChatService:
    def __init__(self, settings: Settings):
        self.settings = settings
        self.client: OpenAI | None = None

    def _get_client(self) -> OpenAI:
        if self.client is None:
            self.client = OpenAI(
                api_key=self.settings.dashscope_api_key or "missing-api-key",
                base_url=self.settings.dashscope_base_url,
                timeout=self.settings.request_timeout_seconds,
                max_retries=0,
            )
        return self.client

    @staticmethod
    def _should_use_weather_tool(user_message: str) -> bool:
        normalized = (user_message or "").strip().lower()
        if not normalized:
            return False

        keywords = [
            "天气",
            "气温",
            "温度",
            "下雨",
            "下雪",
            "晴",
            "阴",
            "风力",
            "风速",
            "湿度",
            "冷不冷",
            "热不热",
            "weather",
            "temperature",
            "rain",
            "snow",
            "humidity",
            "wind",
        ]
        return any(keyword in normalized for keyword in keywords)

    def _resolve_model_for_image(self, requested_model: str | None) -> str:
        normalized = str(requested_model or "").strip()
        if not normalized:
            return self.settings.dashscope_vision_model

        lowered = normalized.lower()
        if "vl" in lowered or "vision" in lowered:
            return normalized

        return self.settings.dashscope_vision_model

    def create_chat_execution(
        self,
        role_prompt: str,
        history_messages: list[dict],
        user_message: str,
        stream: bool,
        model: str | None = None,
        temperature: float | None = None,
        top_p: float | None = None,
        max_tokens: int | None = None,
        presence_penalty: float | None = None,
        frequency_penalty: float | None = None,
        user: str | None = None,
        image_url: str | None = None,
        image_data_url: str | None = None,
        weather_city: str | None = None,
        weather_service: QWeatherService | None = None,
    ) -> ChatExecution:
        normalized_message = (user_message or "").strip()
        if not normalized_message and not (image_url or image_data_url):
            raise ChatServiceError("message 不能为空，或者至少提供一张图片。")

        explicit_weather_context = None
        if weather_city:
            if not weather_service or not weather_service.enabled:
                raise ChatServiceError("天气查询能力尚未配置完成。", status_code=500)
            try:
                explicit_weather_context = weather_service.get_current_weather(weather_city)
            except WeatherServiceError as exc:
                raise ChatServiceError(
                    exc.message,
                    status_code=502,
                    details=exc.details,
                ) from exc

        if image_url or image_data_url:
            messages = self._build_messages(
                role_prompt=role_prompt,
                history_messages=history_messages,
                user_message=normalized_message,
                image_url=image_url,
                image_data_url=image_data_url,
                weather_context=explicit_weather_context,
            )
            return self._direct_completion(
                messages=messages,
                stream=stream,
                model=self._resolve_model_for_image(model),
                temperature=temperature,
                top_p=top_p,
                max_tokens=max_tokens,
                presence_penalty=presence_penalty,
                frequency_penalty=frequency_penalty,
                user=user,
                has_image=True,
                weather_context=explicit_weather_context,
            )

        if explicit_weather_context is not None:
            messages = self._build_messages(
                role_prompt=role_prompt,
                history_messages=history_messages,
                user_message=normalized_message,
                weather_context=explicit_weather_context,
            )
            return self._direct_completion(
                messages=messages,
                stream=stream,
                model=model or self.settings.dashscope_text_model,
                temperature=temperature,
                top_p=top_p,
                max_tokens=max_tokens,
                presence_penalty=presence_penalty,
                frequency_penalty=frequency_penalty,
                user=user,
                has_image=False,
                weather_context=explicit_weather_context,
            )

        if (
            weather_service
            and weather_service.enabled
            and self._should_use_weather_tool(normalized_message)
        ):
            return self._completion_with_weather_tool(
                role_prompt=role_prompt,
                history_messages=history_messages,
                user_message=normalized_message,
                stream=stream,
                model=model or self.settings.dashscope_text_model,
                temperature=temperature,
                top_p=top_p,
                max_tokens=max_tokens,
                presence_penalty=presence_penalty,
                frequency_penalty=frequency_penalty,
                user=user,
                weather_service=weather_service,
            )

        messages = self._build_messages(
            role_prompt=role_prompt,
            history_messages=history_messages,
            user_message=normalized_message,
        )
        return self._direct_completion(
            messages=messages,
            stream=stream,
            model=model or self.settings.dashscope_text_model,
            temperature=temperature,
            top_p=top_p,
            max_tokens=max_tokens,
            presence_penalty=presence_penalty,
            frequency_penalty=frequency_penalty,
            user=user,
            has_image=False,
            weather_context=None,
        )

    def _direct_completion(
        self,
        messages: list[dict],
        stream: bool,
        model: str,
        temperature: float | None,
        top_p: float | None,
        max_tokens: int | None,
        presence_penalty: float | None,
        frequency_penalty: float | None,
        user: str | None,
        has_image: bool,
        weather_context: dict | None,
    ) -> ChatExecution:
        request_payload = self._build_request_payload(
            messages=messages,
            stream=stream,
            model=model,
            temperature=temperature,
            top_p=top_p,
            max_tokens=max_tokens,
            presence_penalty=presence_penalty,
            frequency_penalty=frequency_penalty,
            user=user,
        )

        try:
            completion = self._get_client().chat.completions.create(**request_payload)
        except APITimeoutError as exc:
            raise ChatServiceError("请求千问模型超时。", status_code=504) from exc
        except APIError as exc:
            raise ChatServiceError(
                "调用千问模型失败。",
                status_code=502,
                details={"error": str(exc)},
            ) from exc
        except Exception as exc:
            raise ChatServiceError(
                "调用千问模型时发生未知错误。",
                status_code=500,
                details={"error": str(exc)},
            ) from exc

        if stream:
            return ChatExecution(
                model=model,
                final_text="",
                provider_stream=completion,
                weather_context=weather_context,
                has_image=has_image,
            )

        response_dict = completion.model_dump()
        final_text = self._extract_text_from_response(response_dict)
        return ChatExecution(
            model=model,
            final_text=final_text,
            response_dict=response_dict,
            weather_context=weather_context,
            has_image=has_image,
            usage=response_dict.get("usage"),
        )

    def _completion_with_weather_tool(
        self,
        role_prompt: str,
        history_messages: list[dict],
        user_message: str,
        stream: bool,
        model: str,
        temperature: float | None,
        top_p: float | None,
        max_tokens: int | None,
        presence_penalty: float | None,
        frequency_penalty: float | None,
        user: str | None,
        weather_service: QWeatherService,
    ) -> ChatExecution:
        messages = self._build_messages(
            role_prompt=role_prompt,
            history_messages=history_messages,
            user_message=user_message,
        )
        tools = [
            {
                "type": "function",
                "function": {
                    "name": "get_current_weather",
                    "description": "当用户询问指定城市的实时天气时，查询该城市的实时天气。",
                    "parameters": {
                        "type": "object",
                        "properties": {
                            "city_name": {
                                "type": "string",
                                "description": "要查询天气的城市名称，例如杭州、北京、上海。",
                            }
                        },
                        "required": ["city_name"],
                    },
                },
            }
        ]

        latest_response_dict: dict | None = None
        weather_context = None

        for _ in range(3):
            request_payload = self._build_request_payload(
                messages=messages,
                stream=False,
                model=model,
                temperature=temperature,
                top_p=top_p,
                max_tokens=max_tokens,
                presence_penalty=presence_penalty,
                frequency_penalty=frequency_penalty,
                user=user,
            )
            request_payload["tools"] = tools

            try:
                completion = self._get_client().chat.completions.create(**request_payload)
            except APITimeoutError as exc:
                raise ChatServiceError("请求千问模型超时。", status_code=504) from exc
            except APIError as exc:
                raise ChatServiceError(
                    "调用千问模型失败。",
                    status_code=502,
                    details={"error": str(exc)},
                ) from exc
            except Exception as exc:
                raise ChatServiceError(
                    "调用千问模型时发生未知错误。",
                    status_code=500,
                    details={"error": str(exc)},
                ) from exc

            latest_response_dict = completion.model_dump()
            assistant_message = latest_response_dict["choices"][0]["message"]
            messages.append(
                {
                    "role": "assistant",
                    "content": assistant_message.get("content") or "",
                    "tool_calls": assistant_message.get("tool_calls"),
                }
            )

            tool_calls = assistant_message.get("tool_calls") or []
            if not tool_calls:
                final_text = self._extract_text_from_response(latest_response_dict)
                if stream:
                    synthetic_events = self._build_synthetic_stream_events(
                        text=final_text,
                        model=model,
                        usage=latest_response_dict.get("usage"),
                    )
                    return ChatExecution(
                        model=model,
                        final_text=final_text,
                        response_dict=latest_response_dict,
                        synthetic_stream_events=synthetic_events,
                        weather_context=weather_context,
                        used_tool_call=weather_context is not None,
                        usage=latest_response_dict.get("usage"),
                    )
                return ChatExecution(
                    model=model,
                    final_text=final_text,
                    response_dict=latest_response_dict,
                    weather_context=weather_context,
                    used_tool_call=weather_context is not None,
                    usage=latest_response_dict.get("usage"),
                )

            for tool_call in tool_calls:
                function_name = tool_call.get("function", {}).get("name", "")
                raw_arguments = tool_call.get("function", {}).get("arguments") or "{}"
                try:
                    arguments = json.loads(raw_arguments)
                except json.JSONDecodeError:
                    arguments = {}

                if function_name != "get_current_weather":
                    tool_payload = {
                        "success": False,
                        "message": f"暂不支持的工具调用: {function_name}",
                    }
                else:
                    city_name = str(
                        arguments.get("city_name")
                        or arguments.get("city")
                        or arguments.get("location")
                        or ""
                    ).strip()
                    if not city_name:
                        tool_payload = {
                            "success": False,
                            "message": "缺少 city_name 参数。",
                        }
                    else:
                        try:
                            weather_context = weather_service.get_current_weather(city_name)
                            tool_payload = {"success": True, "weather": weather_context}
                        except WeatherServiceError as exc:
                            tool_payload = {
                                "success": False,
                                "message": exc.message,
                                "details": exc.details,
                            }

                messages.append(
                    {
                        "role": "tool",
                        "tool_call_id": tool_call["id"],
                        "name": function_name,
                        "content": json.dumps(tool_payload, ensure_ascii=False),
                    }
                )

        raise ChatServiceError("天气工具调用轮次超限。", status_code=500)

    def _build_messages(
        self,
        role_prompt: str,
        history_messages: list[dict],
        user_message: str,
        image_url: str | None = None,
        image_data_url: str | None = None,
        weather_context: dict | None = None,
    ) -> list[dict]:
        messages: list[dict] = [{"role": "system", "content": role_prompt}]

        if weather_context:
            messages.append(
                {
                    "role": "system",
                    "content": (
                        "以下是系统通过和风天气 API 查询到的实时天气信息，请以此为准回答天气相关问题：\n"
                        + json.dumps(weather_context, ensure_ascii=False)
                    ),
                }
            )

        messages.extend(history_messages)

        normalized_image_url = image_data_url or image_url
        if normalized_image_url:
            parts = []
            if user_message:
                parts.append({"type": "text", "text": user_message})
            else:
                parts.append({"type": "text", "text": "请结合这张图片与上下文继续聊天。"})
            parts.append({"type": "image_url", "image_url": {"url": normalized_image_url}})
            messages.append({"role": "user", "content": parts})
        else:
            messages.append({"role": "user", "content": user_message})

        return messages

    def _build_request_payload(
        self,
        messages: list[dict],
        stream: bool,
        model: str,
        temperature: float | None,
        top_p: float | None,
        max_tokens: int | None,
        presence_penalty: float | None,
        frequency_penalty: float | None,
        user: str | None,
    ) -> dict:
        payload = {
            "model": model,
            "messages": messages,
            "temperature": (
                self.settings.default_temperature
                if temperature is None
                else temperature
            ),
            "stream": stream,
        }
        if top_p is not None:
            payload["top_p"] = top_p
        if max_tokens is not None:
            payload["max_tokens"] = max_tokens
        if presence_penalty is not None:
            payload["presence_penalty"] = presence_penalty
        if frequency_penalty is not None:
            payload["frequency_penalty"] = frequency_penalty
        if user:
            payload["user"] = user
        if stream:
            payload["stream_options"] = {"include_usage": True}
        return payload

    @staticmethod
    def _extract_text_from_response(response_dict: dict) -> str:
        choices = response_dict.get("choices") or []
        if not choices:
            return ""
        message = choices[0].get("message") or {}
        content = message.get("content")
        if isinstance(content, str):
            return content
        if isinstance(content, list):
            text_parts = []
            for item in content:
                if item.get("type") == "text":
                    text_parts.append(item.get("text") or "")
            return "".join(text_parts)
        return ""

    def _build_synthetic_stream_events(
        self,
        text: str,
        model: str,
        usage: dict | None = None,
    ) -> list[dict]:
        completion_id = f"chatcmpl-{uuid4().hex}"
        events = [
            {
                "id": completion_id,
                "object": "chat.completion.chunk",
                "created": 0,
                "model": model,
                "choices": [
                    {
                        "index": 0,
                        "delta": {"role": "assistant", "content": ""},
                        "finish_reason": None,
                    }
                ],
                "usage": None,
            }
        ]

        chunk_size = max(1, self.settings.synthetic_stream_chunk_size)
        for start in range(0, len(text), chunk_size):
            chunk = text[start : start + chunk_size]
            events.append(
                {
                    "id": completion_id,
                    "object": "chat.completion.chunk",
                    "created": 0,
                    "model": model,
                    "choices": [
                        {
                            "index": 0,
                            "delta": {"content": chunk},
                            "finish_reason": None,
                        }
                    ],
                    "usage": None,
                }
            )

        events.append(
            {
                "id": completion_id,
                "object": "chat.completion.chunk",
                "created": 0,
                "model": model,
                "choices": [
                    {
                        "index": 0,
                        "delta": {"content": ""},
                        "finish_reason": "stop",
                    }
                ],
                "usage": None,
            }
        )
        events.append(
            {
                "id": completion_id,
                "object": "chat.completion.chunk",
                "created": 0,
                "model": model,
                "choices": [],
                "usage": usage,
            }
        )
        return events
