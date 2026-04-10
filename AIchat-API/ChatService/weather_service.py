from typing import Any

import requests

from config import Settings


class WeatherServiceError(Exception):
    def __init__(self, message: str, details: dict[str, Any] | None = None):
        super().__init__(message)
        self.message = message
        self.details = details or {}


class QWeatherService:
    def __init__(self, settings: Settings):
        self.settings = settings

    @property
    def enabled(self) -> bool:
        return self.settings.weather_configured

    @property
    def base_url(self) -> str:
        host = self.settings.qweather_api_host.strip().rstrip("/")
        if not host:
            return ""
        if host.startswith(("http://", "https://")):
            return host
        return f"https://{host}"

    def _request_json(self, path: str, params: dict[str, Any]) -> dict[str, Any]:
        if not self.enabled:
            raise WeatherServiceError("QWeather API is not configured.")

        url = f"{self.base_url}{path}"
        try:
            response = requests.get(
                url,
                params=params,
                headers={
                    "X-QW-Api-Key": self.settings.qweather_api_key,
                    "Accept": "application/json",
                    "Accept-Encoding": "gzip",
                },
                timeout=self.settings.qweather_timeout_seconds,
            )
            response.raise_for_status()
        except requests.RequestException as exc:
            raise WeatherServiceError(
                "Failed to request QWeather API.",
                details={"error": str(exc), "url": url, "params": params},
            ) from exc

        data = response.json()
        code = str(data.get("code", ""))
        if code != "200":
            raise WeatherServiceError(
                "QWeather API returned an unexpected response.",
                details={"code": code, "response": data},
            )
        return data

    def lookup_city(self, city_name: str) -> dict[str, Any]:
        data = self._request_json("/geo/v2/city/lookup", {"location": city_name})
        locations = data.get("location") or []
        if not locations:
            raise WeatherServiceError(
                "No matching city was found.",
                details={"cityName": city_name},
            )
        return locations[0]

    def get_current_weather(self, city_name: str) -> dict[str, Any]:
        location = self.lookup_city(city_name)
        weather_data = self._request_json(
            "/v7/weather/now",
            {"location": location["id"]},
        )
        now = weather_data.get("now") or {}
        return {
            "queryCity": city_name,
            "locationId": location.get("id"),
            "cityName": location.get("name"),
            "adm1": location.get("adm1"),
            "adm2": location.get("adm2"),
            "country": location.get("country"),
            "tz": location.get("tz"),
            "obsTime": now.get("obsTime"),
            "temp": now.get("temp"),
            "feelsLike": now.get("feelsLike"),
            "text": now.get("text"),
            "windDir": now.get("windDir"),
            "windScale": now.get("windScale"),
            "windSpeed": now.get("windSpeed"),
            "humidity": now.get("humidity"),
            "precip": now.get("precip"),
            "pressure": now.get("pressure"),
            "vis": now.get("vis"),
            "summary": self._build_summary(location, now),
        }

    @staticmethod
    def _build_summary(location: dict[str, Any], now: dict[str, Any]) -> str:
        city = location.get("name") or "当前城市"
        condition = now.get("text") or "未知天气"
        temp = now.get("temp") or "-"
        feels_like = now.get("feelsLike") or "-"
        humidity = now.get("humidity") or "-"
        wind_dir = now.get("windDir") or "-"
        wind_scale = now.get("windScale") or "-"
        return (
            f"{city}当前天气{condition}，气温{temp}℃，体感{feels_like}℃，"
            f"湿度{humidity}%，风向{wind_dir}，风力{wind_scale}级。"
        )
