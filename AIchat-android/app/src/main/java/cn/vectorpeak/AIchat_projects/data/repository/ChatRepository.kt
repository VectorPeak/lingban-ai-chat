package cn.vectorpeak.AIchat_projects.data.repository

import cn.vectorpeak.AIchat_projects.data.local.SessionStore
import cn.vectorpeak.AIchat_projects.data.model.ApiEnvelope
import cn.vectorpeak.AIchat_projects.data.model.ChatCompletionDataDto
import cn.vectorpeak.AIchat_projects.data.model.ChatCompletionMetaDto
import cn.vectorpeak.AIchat_projects.data.model.ChatCompletionResult
import cn.vectorpeak.AIchat_projects.data.model.ChatMessageDto
import cn.vectorpeak.AIchat_projects.data.model.ChatSendRequest
import cn.vectorpeak.AIchat_projects.data.model.ChatStreamEvent
import cn.vectorpeak.AIchat_projects.data.model.ClearChatDataDto
import cn.vectorpeak.AIchat_projects.data.model.ClearChatResult
import cn.vectorpeak.AIchat_projects.data.model.HistoryDataDto
import cn.vectorpeak.AIchat_projects.data.model.HistoryResponse
import cn.vectorpeak.AIchat_projects.data.model.Role
import cn.vectorpeak.AIchat_projects.data.model.RoleDto
import cn.vectorpeak.AIchat_projects.data.model.toDomain
import cn.vectorpeak.AIchat_projects.data.remote.ChatApi
import cn.vectorpeak.AIchat_projects.data.remote.ChatStreamParser
import cn.vectorpeak.AIchat_projects.data.remote.NetworkConfig
import cn.vectorpeak.AIchat_projects.data.remote.UnauthorizedException
import cn.vectorpeak.AIchat_projects.data.remote.readBodyString
import cn.vectorpeak.AIchat_projects.data.remote.throwApiException
import cn.vectorpeak.AIchat_projects.data.remote.toRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody

class ChatRepository(
    private val api: ChatApi,
    private val httpClient: OkHttpClient,
    private val json: Json,
    private val sessionStore: SessionStore,
) {
    suspend fun fetchRoles(): List<Role> {
        val response = api.getRoles()
        if (!response.isSuccessful) {
            response.throwApiException(json)
        }
        val envelope = json.decodeRolesEnvelope(response.readBodyString())
        return envelope.map(RoleDto::toDomain)
    }

    suspend fun fetchHistory(
        roleKey: String,
        limit: Int = 100,
    ): HistoryResponse {
        val authorization = bearerToken()
        val response = api.getHistory(
            authorization = authorization,
            roleKey = roleKey,
            limit = limit,
        )
        if (!response.isSuccessful) {
            handleProtectedError(response)
        }
        val envelope = json.decodeFromString<ApiEnvelope<HistoryDataDto>>(response.readBodyString())
        val data = requireNotNull(envelope.data) { "聊天历史响应缺少 data。" }
        sessionStore.saveLastRoleKey(roleKey)
        return HistoryResponse(
            role = data.role.toDomain(),
            conversationId = data.conversationId,
            messages = data.messages,
        )
    }

    suspend fun clearChat(roleKey: String): ClearChatResult {
        val authorization = bearerToken()
        val response = api.clearChat(
            authorization = authorization,
            body = json.toRequestBody(json.encodeToString(mapOf("roleKey" to roleKey))),
        )
        if (!response.isSuccessful) {
            handleProtectedError(response)
        }
        val envelope = json.decodeFromString<ApiEnvelope<ClearChatDataDto>>(response.readBodyString())
        val data = requireNotNull(envelope.data) { "清空聊天响应缺少 data。" }
        return ClearChatResult(
            role = data.role.toDomain(),
            clearedConversationCount = data.clearedConversationCount,
        )
    }

    suspend fun sendCompletion(request: ChatSendRequest): ChatCompletionResult {
        val authorization = bearerToken()
        val response = api.sendCompletion(
            authorization = authorization,
            body = json.toRequestBody(json.encodeToString(request)),
        )
        if (!response.isSuccessful) {
            handleProtectedError(response)
        }
        val raw = response.readBodyString()
        val dataEnvelope = json.decodeFromString<ApiEnvelope<ChatCompletionDataDto>>(raw)
        val meta = runCatching {
            json.decodeFromString<ApiEnvelope<ChatCompletionMetaDto>>(raw)
        }.getOrNull()
        val assistantMessage = meta?.data?.assistantMessage
            ?: dataEnvelope.data?.choices?.firstOrNull()?.message?.content
            ?: ""
        return ChatCompletionResult(
            assistantMessage = assistantMessage,
            role = meta?.data?.role?.toDomain(),
            conversationId = meta?.data?.conversationId,
        )
    }

    fun streamChat(request: ChatSendRequest): Flow<ChatStreamEvent> = callbackFlow {
        val token = sessionStore.snapshot().token ?: throw UnauthorizedException()
        val httpRequest = Request.Builder()
            .url(NetworkConfig.CHAT_BASE_URL.toHttpUrl().newBuilder().addPathSegments("api/chat/stream").build())
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "text/event-stream")
            .addHeader("Cache-Control", "no-cache")
            .addHeader("Connection", "keep-alive")
            .post(json.toRequestBody(json.encodeToString(request)))
            .build()

        val call = httpClient.newCall(httpRequest)
        val streamJob = launch(Dispatchers.IO) {
            call.execute().use { response ->
                if (!response.isSuccessful) {
                    if (response.code == 401) {
                        sessionStore.clearAuth()
                        close(UnauthorizedException())
                        return@use
                    }
                    val message = response.body?.string().orEmpty().ifBlank { "流式聊天失败，请稍后再试。" }
                    close(cn.vectorpeak.AIchat_projects.data.remote.ApiException(message, response.code))
                    return@use
                }

                sessionStore.saveLastRoleKey(request.roleKey)

                val source = requireNotNull(response.body).source()
                var buffer = ""
                while (!source.exhausted()) {
                    val line = source.readUtf8Line() ?: continue
                    buffer += line + "\n"
                    if (!buffer.contains("\n\n")) {
                        continue
                    }
                    val (frames, remaining) = ChatStreamParser.splitFrames(buffer)
                    buffer = remaining
                    frames.forEach { frame ->
                        ChatStreamParser.parseFrame(frame, json).forEach { event ->
                            trySend(event).isSuccess
                        }
                    }
                }
                if (buffer.isNotBlank()) {
                    ChatStreamParser.parseFrame(buffer.trim(), json).forEach { event ->
                        trySend(event).isSuccess
                    }
                }
                close()
            }
        }

        awaitClose {
            streamJob.cancel()
            call.cancel()
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun bearerToken(): String {
        val token = sessionStore.snapshot().token ?: throw UnauthorizedException()
        return "Bearer $token"
    }

    private suspend fun handleProtectedError(response: retrofit2.Response<ResponseBody>): Nothing {
        if (response.code() == 401) {
            sessionStore.clearAuth()
        }
        response.throwApiException(json)
    }

    private fun Json.decodeRolesEnvelope(raw: String): List<RoleDto> {
        val envelope = decodeFromString<ApiEnvelope<List<RoleDto>>>(raw)
        return envelope.data.orEmpty()
    }
}
