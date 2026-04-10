package cn.vectorpeak.AIchat_projects.ui.screen.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import cn.vectorpeak.AIchat_projects.data.model.LoadState
import cn.vectorpeak.AIchat_projects.data.model.MessageAuthor
import cn.vectorpeak.AIchat_projects.data.model.MessageStatus
import cn.vectorpeak.AIchat_projects.data.model.MessageUiModel
import cn.vectorpeak.AIchat_projects.ui.component.AmbientBackdrop
import cn.vectorpeak.AIchat_projects.ui.component.GlassSurface
import cn.vectorpeak.AIchat_projects.ui.component.LoadingPane
import cn.vectorpeak.AIchat_projects.ui.component.PrimaryGradientButton
import cn.vectorpeak.AIchat_projects.ui.component.StatePane
import cn.vectorpeak.AIchat_projects.ui.theme.ErrorRed
import cn.vectorpeak.AIchat_projects.ui.theme.TextPrimary
import cn.vectorpeak.AIchat_projects.ui.theme.TextSecondary
import cn.vectorpeak.AIchat_projects.ui.theme.rolePalette
import cn.vectorpeak.AIchat_projects.ui.theme.userBubbleBrush
import cn.vectorpeak.AIchat_projects.viewmodel.AppViewModelProvider
import cn.vectorpeak.AIchat_projects.viewmodel.ChatViewModel

@Composable
fun ChatRoute(
    onNavigateBack: () -> Unit,
    onLogoutRequired: () -> Unit,
    viewModel: ChatViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val role = uiState.role
    val palette = rolePalette(role?.roleKey ?: "chenge")
    val listState = rememberLazyListState()
    val bottomBringIntoViewRequester = remember { BringIntoViewRequester() }

    LaunchedEffect(uiState.forceLogout) {
        if (uiState.forceLogout) {
            viewModel.clearForceLogout()
            onLogoutRequired()
        }
    }

    LaunchedEffect(uiState.messagesState) {
        val messages = (uiState.messagesState as? LoadState.Content)?.value.orEmpty()
        if (messages.isNotEmpty()) {
            bottomBringIntoViewRequester.bringIntoView()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AmbientBackdrop(palette = palette)
        Column(modifier = Modifier.fillMaxSize()) {
            GlassSurface(
                palette = palette,
                shape = RoundedCornerShape(bottomStart = 26.dp, bottomEnd = 26.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 18.dp, vertical = 16.dp)
                        .padding(top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        onClick = onNavigateBack,
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.4f),
                    ) {
                        Text(
                            text = "‹",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            color = palette.accent,
                            style = MaterialTheme.typography.titleLarge,
                        )
                    }
                    role?.let { currentRole ->
                        AsyncImage(
                            model = currentRole.avatarUrl,
                            contentDescription = currentRole.nickname,
                            modifier = Modifier
                                .padding(start = 12.dp)
                                .size(42.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop,
                        )
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 12.dp),
                        ) {
                            Text(
                                text = currentRole.nickname,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                            )
                            Text(
                                text = "陪伴中 · ${currentRole.archetype}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                            )
                        }
                    }
                    Surface(
                        onClick = { viewModel.showClearDialog(true) },
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.4f),
                    ) {
                        Text(
                            text = "···",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                            color = palette.accent,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
            }

            when (val messagesState = uiState.messagesState) {
                LoadState.Loading -> LoadingPane(modifier = Modifier.weight(1f))
                is LoadState.Error -> StatePane(
                    title = "聊天记录加载失败",
                    description = messagesState.message,
                    modifier = Modifier.weight(1f),
                    actionText = "重新加载",
                    onAction = viewModel::loadHistory,
                )
                LoadState.Empty -> StatePane(
                    title = "还没有聊天记录",
                    description = "你可以发送第一条消息开始对话。",
                    modifier = Modifier.weight(1f),
                )
                is LoadState.Content -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp),
                        contentPadding = PaddingValues(top = 18.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center,
                            ) {
                                GlassSurface(
                                    palette = palette,
                                    shape = RoundedCornerShape(999.dp),
                                ) {
                                    Text(
                                        text = "今天",
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary,
                                    )
                                }
                            }
                        }
                        items(messagesState.value, key = { it.id }) { message ->
                            MessageBubble(
                                message = message,
                                avatarUrl = role?.avatarUrl.orEmpty(),
                                roleName = role?.nickname.orEmpty(),
                                userBubbleBrush = palette.userBubbleBrush(),
                            )
                        }
                        item(key = "bottom-anchor") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .size(1.dp)
                                    .bringIntoViewRequester(bottomBringIntoViewRequester),
                            )
                        }
                    }
                }
            }

            GlassSurface(
                palette = palette,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 16.dp)
                        .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    uiState.inlineError?.let {
                        Text(
                            text = it,
                            color = ErrorRed,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    OutlinedTextField(
                        value = uiState.inputText,
                        onValueChange = viewModel::updateInput,
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 1,
                        maxLines = 4,
                        placeholder = { Text("和 ${role?.nickname ?: "TA"} 说点什么…") },
                        trailingIcon = {
                            if (uiState.sending) {
                                Surface(
                                    onClick = viewModel::stopStreaming,
                                    modifier = Modifier.size(28.dp),
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.92f),
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(palette.accent),
                                        )
                                    }
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { viewModel.sendMessage() }),
                        shape = RoundedCornerShape(22.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = palette.accent,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.45f),
                            focusedContainerColor = Color.White.copy(alpha = 0.36f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.28f),
                            cursorColor = palette.accent,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedPlaceholderColor = TextSecondary,
                            unfocusedPlaceholderColor = TextSecondary,
                        ),
                    )
                    if (uiState.sending) {
                        PrimaryGradientButton(
                            text = "生成中…",
                            palette = palette,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = false,
                            onClick = {},
                        )
                    } else {
                        PrimaryGradientButton(
                            text = "发送消息",
                            palette = palette,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = uiState.inputText.isNotBlank(),
                            onClick = viewModel::sendMessage,
                        )
                    }
                }
            }
        }

        if (uiState.showClearDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.24f)),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(28.dp),
                    color = palette.navGlass.copy(alpha = 1f),
                    shadowElevation = 0.dp,
                    tonalElevation = 0.dp,
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Text(
                            text = "清空当前聊天？",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                        )
                        Text(
                            text = "清空后将移除你与当前角色本轮会话中的聊天内容，但不会影响角色信息。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Surface(
                                onClick = { viewModel.showClearDialog(false) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(20.dp),
                                color = Color.White.copy(alpha = 0.9f),
                            ) {
                                Text(
                                    text = "取消",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 14.dp),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center,
                                )
                            }
                            PrimaryGradientButton(
                                text = if (uiState.clearing) "清空中…" else "确认清空",
                                palette = palette,
                                modifier = Modifier.weight(1f),
                                enabled = !uiState.clearing,
                                onClick = viewModel::clearConversation,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: MessageUiModel,
    avatarUrl: String,
    roleName: String,
    userBubbleBrush: Brush,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.role == MessageAuthor.User) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
    ) {
        if (message.role == MessageAuthor.Assistant) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = roleName,
                modifier = Modifier
                    .padding(end = 10.dp)
                    .size(34.dp)
                    .clip(RoundedCornerShape(14.dp)),
                contentScale = ContentScale.Crop,
            )
        }
        Column(
            horizontalAlignment = if (message.role == MessageAuthor.User) Alignment.End else Alignment.Start,
        ) {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 22.dp,
                            topEnd = 22.dp,
                            bottomEnd = if (message.role == MessageAuthor.User) 8.dp else 22.dp,
                            bottomStart = if (message.role == MessageAuthor.Assistant) 8.dp else 22.dp,
                        ),
                    )
                    .background(
                        if (message.role == MessageAuthor.User) {
                            userBubbleBrush
                        } else {
                            Brush.linearGradient(listOf(Color.White.copy(alpha = 0.52f), Color.White.copy(alpha = 0.34f)))
                        },
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                if (message.status == MessageStatus.Streaming && message.content.isBlank()) {
                    StreamingDots(
                        dotColor = if (message.role == MessageAuthor.User) Color.White else TextSecondary,
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = message.content.ifBlank { "…" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (message.role == MessageAuthor.User) Color.White else TextPrimary,
                        )
                        if (message.status == MessageStatus.Streaming) {
                            StreamingDots(
                                dotColor = if (message.role == MessageAuthor.User) Color.White.copy(alpha = 0.9f) else TextSecondary,
                            )
                        }
                    }
                }
            }
            Text(
                text = when (message.status) {
                    MessageStatus.Sent -> message.timestamp
                    MessageStatus.Streaming -> "生成中…"
                    MessageStatus.Stopped -> "已停止"
                    MessageStatus.Failed -> "发送失败"
                },
                modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                color = if (message.status == MessageStatus.Failed) ErrorRed else TextSecondary,
            )
        }
    }
}

@Composable
private fun StreamingDots(
    dotColor: Color,
) {
    val transition = rememberInfiniteTransition(label = "streamingDots")
    val firstDotAlpha by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, delayMillis = 0),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "firstDotAlpha",
    )
    val secondDotAlpha by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, delayMillis = 120),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "secondDotAlpha",
    )
    val thirdDotAlpha by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, delayMillis = 240),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "thirdDotAlpha",
    )

    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        listOf(firstDotAlpha, secondDotAlpha, thirdDotAlpha).forEach { alpha ->
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(dotColor.copy(alpha = alpha)),
            )
        }
    }
}
