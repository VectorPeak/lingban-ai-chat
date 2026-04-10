package cn.vectorpeak.AIchat_projects.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatMessageDto(
    val id: Long? = null,
    val role: String,
    val content: String,
    val hasImage: Boolean = false,
    val model: String? = null,
    val createdAt: String? = null,
)

@Serializable
data class HistoryDataDto(
    val role: RoleDto,
    val conversationId: Long? = null,
    val messages: List<ChatMessageDto> = emptyList(),
)

data class HistoryResponse(
    val role: Role,
    val conversationId: Long?,
    val messages: List<ChatMessageDto>,
)

@Serializable
data class ClearChatDataDto(
    val role: RoleDto,
    val clearedConversationCount: Int = 0,
)

data class ClearChatResult(
    val role: Role,
    val clearedConversationCount: Int,
)

@Serializable
data class ChatSendRequest(
    val roleKey: String,
    val message: String? = null,
    val imageUrl: String? = null,
    val imageDataUrl: String? = null,
    val weatherCity: String? = null,
    val stream: Boolean = true,
)

@Serializable
data class ChatCompletionChoiceMessageDto(
    val role: String? = null,
    val content: String? = null,
)

@Serializable
data class ChatCompletionChoiceDto(
    val index: Int,
    val message: ChatCompletionChoiceMessageDto? = null,
    @SerialName("finish_reason")
    val finishReason: String? = null,
)

@Serializable
data class ChatUsageDto(
    @SerialName("prompt_tokens")
    val promptTokens: Int? = null,
    @SerialName("completion_tokens")
    val completionTokens: Int? = null,
    @SerialName("total_tokens")
    val totalTokens: Int? = null,
)

@Serializable
data class ChatCompletionDataDto(
    val id: String,
    val model: String? = null,
    val choices: List<ChatCompletionChoiceDto> = emptyList(),
    val usage: ChatUsageDto? = null,
)

@Serializable
data class ChatCompletionMetaDto(
    val role: RoleDto? = null,
    val conversationId: Long? = null,
    val assistantMessage: String? = null,
    val weather: String? = null,
    val usedToolCall: Boolean = false,
)

data class ChatCompletionResult(
    val assistantMessage: String,
    val role: Role?,
    val conversationId: Long?,
)

@Serializable
data class ChatStreamChunkDto(
    val choices: List<ChatStreamChoiceDto> = emptyList(),
    val usage: ChatUsageDto? = null,
)

@Serializable
data class ChatStreamChoiceDto(
    val delta: ChatStreamDeltaDto? = null,
)

@Serializable
data class ChatStreamDeltaDto(
    val role: String? = null,
    val content: String? = null,
)

sealed interface ChatStreamEvent {
    data class Delta(val text: String) : ChatStreamEvent
    data class Usage(val usage: ChatUsageDto?) : ChatStreamEvent
    data object Completed : ChatStreamEvent
}

enum class MessageAuthor {
    User,
    Assistant,
}

enum class MessageStatus {
    Sent,
    Streaming,
    Stopped,
    Failed,
}

data class MessageUiModel(
    val id: String,
    val role: MessageAuthor,
    val content: String,
    val timestamp: String,
    val status: MessageStatus = MessageStatus.Sent,
    val isOpeningMessage: Boolean = false,
)

fun ChatMessageDto.toUiModel(): MessageUiModel = MessageUiModel(
    id = id?.toString() ?: "${role}-${createdAt.orEmpty()}-${content.hashCode()}",
    role = if (role == "user") MessageAuthor.User else MessageAuthor.Assistant,
    content = content,
    timestamp = createdAt?.toDisplayTime() ?: "刚刚",
)
