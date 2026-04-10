package cn.vectorpeak.AIchat_projects.data.remote

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import cn.vectorpeak.AIchat_projects.data.model.ApiErrorEnvelope

open class ApiException(
    override val message: String,
    val statusCode: Int? = null,
) : Exception(message)

class UnauthorizedException(
    message: String = "登录凭证无效。",
) : ApiException(message = message, statusCode = 401)

fun Json.toRequestBody(jsonString: String): RequestBody {
    return jsonString.toRequestBody("application/json; charset=utf-8".toMediaType())
}

fun Response<ResponseBody>.readBodyString(): String {
    return body()?.string().orEmpty()
}

fun Response<ResponseBody>.throwApiException(json: Json): Nothing {
    val statusCode = code()
    val rawMessage = errorBody()?.string().orEmpty()
    val parsedMessage = runCatching {
        json.decodeFromString<ApiErrorEnvelope>(rawMessage).message
    }.getOrNull()
    val message = parsedMessage
        ?: if (statusCode == 401) "登录凭证无效。"
        else rawMessage.ifBlank { "网络请求失败，请稍后再试。" }
    if (statusCode == 401) {
        throw UnauthorizedException(message)
    }
    throw ApiException(message = message, statusCode = statusCode)
}

fun Throwable.toUserMessage(): String {
    return when (this) {
        is UnauthorizedException -> message
        is ApiException -> message
        is SerializationException -> "服务返回格式异常，请稍后再试。"
        else -> message ?: "网络异常，请稍后再试。"
    }
}
