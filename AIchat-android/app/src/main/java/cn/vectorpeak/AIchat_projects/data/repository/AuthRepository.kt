package cn.vectorpeak.AIchat_projects.data.repository

import cn.vectorpeak.AIchat_projects.data.local.SessionStore
import cn.vectorpeak.AIchat_projects.data.model.ApiEnvelope
import cn.vectorpeak.AIchat_projects.data.model.AuthState
import cn.vectorpeak.AIchat_projects.data.model.LoginDataDto
import cn.vectorpeak.AIchat_projects.data.model.LoginRequest
import cn.vectorpeak.AIchat_projects.data.model.LoginResult
import cn.vectorpeak.AIchat_projects.data.model.SendCodeDataDto
import cn.vectorpeak.AIchat_projects.data.model.SendCodeRequest
import cn.vectorpeak.AIchat_projects.data.model.SendCodeResult
import cn.vectorpeak.AIchat_projects.data.remote.AuthApi
import cn.vectorpeak.AIchat_projects.data.remote.readBodyString
import cn.vectorpeak.AIchat_projects.data.remote.throwApiException
import cn.vectorpeak.AIchat_projects.data.remote.toRequestBody
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AuthRepository(
    private val api: AuthApi,
    private val json: Json,
    private val sessionStore: SessionStore,
) {
    val authState: Flow<AuthState> = sessionStore.snapshotFlow.map { snapshot ->
        AuthState(
            isLoggedIn = snapshot.isLoggedIn,
            phoneNumber = snapshot.phoneNumber,
            countryCode = snapshot.countryCode,
        )
    }

    suspend fun sendCode(request: SendCodeRequest): SendCodeResult {
        val response = api.sendCode(
            body = json.toRequestBody(json.encodeToString(request)),
        )
        if (!response.isSuccessful) {
            response.throwApiException(json)
        }
        val envelope = json.decodeFromString<ApiEnvelope<SendCodeDataDto>>(response.readBodyString())
        val data = requireNotNull(envelope.data) { "验证码发送响应缺少 data。" }
        return SendCodeResult(
            requestId = data.requestId,
            validTimeSeconds = data.validTimeSeconds,
            codeLength = data.codeLength,
        )
    }

    suspend fun login(request: LoginRequest): LoginResult {
        val response = api.login(
            body = json.toRequestBody(json.encodeToString(request)),
        )
        if (!response.isSuccessful) {
            response.throwApiException(json)
        }
        val envelope = json.decodeFromString<ApiEnvelope<LoginDataDto>>(response.readBodyString())
        val data = requireNotNull(envelope.data) { "登录响应缺少 data。" }
        sessionStore.saveAuth(
            token = data.token,
            phoneNumber = data.user.phoneNumber,
            countryCode = data.user.countryCode,
        )
        return LoginResult(
            token = data.token,
            tokenType = data.tokenType,
            expiresIn = data.expiresIn,
            expiresAt = data.expiresAt,
            user = data.user,
        )
    }

    suspend fun snapshot() = sessionStore.snapshot()

    suspend fun saveOnboardingSeen() = sessionStore.setOnboardingSeen(true)

    suspend fun saveLastRoleKey(roleKey: String) = sessionStore.saveLastRoleKey(roleKey)

    suspend fun clearAuth() = sessionStore.clearAuth()
}
