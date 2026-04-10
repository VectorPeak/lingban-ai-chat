package cn.vectorpeak.AIchat_projects.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.vectorpeak.AIchat_projects.data.model.ChatSendRequest
import cn.vectorpeak.AIchat_projects.data.model.ChatStreamEvent
import cn.vectorpeak.AIchat_projects.data.model.ChatUiState
import cn.vectorpeak.AIchat_projects.data.model.LoadState
import cn.vectorpeak.AIchat_projects.data.model.MessageAuthor
import cn.vectorpeak.AIchat_projects.data.model.MessageStatus
import cn.vectorpeak.AIchat_projects.data.model.MessageUiModel
import cn.vectorpeak.AIchat_projects.data.model.Role
import cn.vectorpeak.AIchat_projects.data.model.toUiModel
import cn.vectorpeak.AIchat_projects.data.repository.AuthRepository
import cn.vectorpeak.AIchat_projects.data.repository.ChatRepository
import cn.vectorpeak.AIchat_projects.data.remote.UnauthorizedException
import cn.vectorpeak.AIchat_projects.data.remote.toUserMessage
import java.text.BreakIterator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class ChatViewModel(
    savedStateHandle: SavedStateHandle,
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val roleKey: String = savedStateHandle["roleKey"] ?: "chenge"
    private var activeStreamJob: Job? = null

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch {
            _uiState.update { it.copy(messagesState = LoadState.Loading, inlineError = null) }
            runCatching {
                chatRepository.fetchHistory(roleKey = roleKey)
            }.onSuccess { history ->
                val role = history.role
                authRepository.saveLastRoleKey(role.roleKey)
                val messages = if (history.messages.isEmpty()) {
                    listOf(role.toOpeningMessage())
                } else {
                    history.messages.map { it.toUiModel() }
                }
                _uiState.update {
                    it.copy(
                        role = role,
                        messagesState = LoadState.Content(messages),
                        forceLogout = false,
                    )
                }
            }.onFailure { throwable ->
                if (throwable is UnauthorizedException) {
                    authRepository.clearAuth()
                    _uiState.update {
                        it.copy(
                            messagesState = LoadState.Error(throwable.toUserMessage()),
                            forceLogout = true,
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(messagesState = LoadState.Error(throwable.toUserMessage()))
                    }
                }
            }
        }
    }

    fun updateInput(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun showClearDialog(show: Boolean) {
        _uiState.update { it.copy(showClearDialog = show) }
    }

    fun clearForceLogout() {
        _uiState.update { it.copy(forceLogout = false) }
    }

    fun stopStreaming() {
        activeStreamJob?.cancel()
    }

    fun clearConversation() {
        viewModelScope.launch {
            val currentRole = _uiState.value.role ?: return@launch
            _uiState.update { it.copy(clearing = true, inlineError = null, showClearDialog = false) }
            runCatching {
                chatRepository.clearChat(currentRole.roleKey)
            }.onSuccess { result ->
                _uiState.update {
                    it.copy(
                        role = result.role,
                        messagesState = LoadState.Content(listOf(result.role.toOpeningMessage())),
                        clearing = false,
                    )
                }
            }.onFailure { throwable ->
                if (throwable is UnauthorizedException) {
                    authRepository.clearAuth()
                    _uiState.update {
                        it.copy(
                            clearing = false,
                            forceLogout = true,
                            inlineError = throwable.toUserMessage(),
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            clearing = false,
                            inlineError = throwable.toUserMessage(),
                        )
                    }
                }
            }
        }
    }

    fun sendMessage() {
        val text = _uiState.value.inputText.trim()
        val role = _uiState.value.role ?: return
        if (text.isEmpty() || _uiState.value.sending) return

        val currentMessages = (_uiState.value.messagesState as? LoadState.Content)?.value.orEmpty().toMutableList()
        val userMessage = MessageUiModel(
            id = "user-${System.currentTimeMillis()}",
            role = MessageAuthor.User,
            content = text,
            timestamp = nowTimeLabel(),
        )
        val streamingId = "assistant-stream-${Random.nextLong()}"
        currentMessages += userMessage
        currentMessages += MessageUiModel(
            id = streamingId,
            role = MessageAuthor.Assistant,
            content = "",
            timestamp = nowTimeLabel(),
            status = MessageStatus.Streaming,
        )

        _uiState.update {
            it.copy(
                inputText = "",
                sending = true,
                inlineError = null,
                messagesState = LoadState.Content(currentMessages),
            )
        }

        activeStreamJob = viewModelScope.launch {
            runCatching {
                chatRepository.streamChat(
                    ChatSendRequest(
                        roleKey = role.roleKey,
                        message = text,
                        stream = true,
                    ),
                ).collect { event ->
                    when (event) {
                        is ChatStreamEvent.Delta -> revealStreamingText(streamingId, event.text)
                        is ChatStreamEvent.Completed -> finishStreaming(streamingId)
                        is ChatStreamEvent.Usage -> Unit
                    }
                }
            }.onFailure { throwable ->
                when (throwable) {
                    is CancellationException -> markStreamingStopped(streamingId)
                    is UnauthorizedException -> {
                        authRepository.clearAuth()
                        _uiState.update {
                            it.copy(
                                sending = false,
                                forceLogout = true,
                                inlineError = throwable.toUserMessage(),
                            )
                        }
                    }

                    else -> {
                        markStreamingFailure(streamingId, throwable.toUserMessage())
                    }
                }
            }.onSuccess {
                _uiState.update { it.copy(sending = false) }
            }.also {
                activeStreamJob = null
            }
        }
    }

    private fun appendStreamingText(streamingId: String, delta: String) {
        val messages = (_uiState.value.messagesState as? LoadState.Content)?.value.orEmpty().map { message ->
            if (message.id == streamingId) {
                message.copy(content = message.content + delta)
            } else {
                message
            }
        }
        _uiState.update {
            it.copy(messagesState = LoadState.Content(messages))
        }
    }

    private suspend fun revealStreamingText(streamingId: String, delta: String) {
        val chunks = delta.toStreamingUnits()
        if (chunks.isEmpty()) {
            return
        }

        chunks.forEachIndexed { index, unit ->
            appendStreamingText(streamingId, unit)
            delay(
                when {
                    unit == "\n" -> STREAM_NEWLINE_DELAY_MS
                    index == chunks.lastIndex -> STREAM_CHUNK_EDGE_DELAY_MS
                    else -> STREAM_CHARACTER_DELAY_MS
                },
            )
        }
    }

    private fun finishStreaming(streamingId: String) {
        val messages = (_uiState.value.messagesState as? LoadState.Content)?.value.orEmpty().map { message ->
            if (message.id == streamingId) {
                message.copy(
                    content = message.content.ifBlank { "抱歉，这次没有收到完整回复。" },
                    status = MessageStatus.Sent,
                )
            } else {
                message
            }
        }
        _uiState.update {
            it.copy(
                messagesState = LoadState.Content(messages),
                sending = false,
            )
        }
    }

    private fun markStreamingStopped(streamingId: String) {
        val currentMessages = (_uiState.value.messagesState as? LoadState.Content)?.value.orEmpty()
        val updatedMessages = currentMessages.mapNotNull { message ->
            if (message.id != streamingId) {
                message
            } else if (message.content.isBlank()) {
                null
            } else {
                message.copy(status = MessageStatus.Stopped)
            }
        }
        _uiState.update {
            it.copy(
                messagesState = LoadState.Content(updatedMessages),
                sending = false,
                inlineError = "已停止生成",
            )
        }
    }

    private fun markStreamingFailure(streamingId: String, errorMessage: String) {
        val messages = (_uiState.value.messagesState as? LoadState.Content)?.value.orEmpty().map { message ->
            if (message.id == streamingId) {
                message.copy(
                    content = message.content.ifBlank { "消息发送失败，请稍后重试。" },
                    status = MessageStatus.Failed,
                )
            } else {
                message
            }
        }
        _uiState.update {
            it.copy(
                messagesState = LoadState.Content(messages),
                sending = false,
                inlineError = errorMessage,
            )
        }
    }

    private fun nowTimeLabel(): String {
        return SimpleDateFormat("HH:mm", Locale.CHINA).format(Date())
    }

    private fun Role.toOpeningMessage(): MessageUiModel {
        return MessageUiModel(
            id = "opening-${roleKey}",
            role = MessageAuthor.Assistant,
            content = openingMessage,
            timestamp = "刚刚",
            isOpeningMessage = true,
        )
    }

    private fun String.toStreamingUnits(): List<String> {
        val iterator = BreakIterator.getCharacterInstance(Locale.CHINA)
        iterator.setText(this)
        val units = mutableListOf<String>()
        var start = iterator.first()
        var end = iterator.next()
        while (end != BreakIterator.DONE) {
            units += substring(start, end)
            start = end
            end = iterator.next()
        }
        return units
    }

    private companion object {
        const val STREAM_CHARACTER_DELAY_MS = 20L
        const val STREAM_NEWLINE_DELAY_MS = 45L
        const val STREAM_CHUNK_EDGE_DELAY_MS = 70L
    }
}
