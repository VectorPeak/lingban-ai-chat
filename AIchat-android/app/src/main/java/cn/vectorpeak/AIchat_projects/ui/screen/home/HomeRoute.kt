package cn.vectorpeak.AIchat_projects.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import cn.vectorpeak.AIchat_projects.data.model.LoadState
import cn.vectorpeak.AIchat_projects.ui.component.AmbientBackdrop
import cn.vectorpeak.AIchat_projects.ui.component.GlassSurface
import cn.vectorpeak.AIchat_projects.ui.component.LoadingPane
import cn.vectorpeak.AIchat_projects.ui.component.PrimaryGradientButton
import cn.vectorpeak.AIchat_projects.ui.component.SecondaryTag
import cn.vectorpeak.AIchat_projects.ui.component.SectionLabel
import cn.vectorpeak.AIchat_projects.ui.component.StatePane
import cn.vectorpeak.AIchat_projects.ui.theme.SuccessGreen
import cn.vectorpeak.AIchat_projects.ui.theme.TextPrimary
import cn.vectorpeak.AIchat_projects.ui.theme.TextSecondary
import cn.vectorpeak.AIchat_projects.ui.theme.rolePalette
import cn.vectorpeak.AIchat_projects.viewmodel.AppViewModelProvider
import cn.vectorpeak.AIchat_projects.viewmodel.HomeViewModel

@Composable
fun HomeRoute(
    onOpenChat: (String) -> Unit,
    onLogoutRequired: () -> Unit,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val rolesState = uiState.rolesState
    val palette = rolePalette(uiState.currentRoleKey ?: "chenge")

    LaunchedEffect(uiState.forceLogout) {
        if (uiState.forceLogout) {
            viewModel.clearForceLogout()
            onLogoutRequired()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AmbientBackdrop(palette = palette)
        when (rolesState) {
            LoadState.Loading -> LoadingPane()
            LoadState.Empty -> StatePane(
                title = "当前还没有角色",
                description = "请稍后再试。",
                actionText = "重新加载",
                onAction = viewModel::refresh,
            )
            is LoadState.Error -> StatePane(
                title = "角色大厅加载失败",
                description = rolesState.message,
                actionText = "重新加载",
                onAction = viewModel::refresh,
            )
            is LoadState.Content -> {
                val roles = rolesState.value
                val currentRole = roles.firstOrNull { it.roleKey == uiState.currentRoleKey } ?: roles.first()
                val otherRoles = roles.filterNot { it.roleKey == currentRole.roleKey }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 60.dp, bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    item {
                        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column {
                                    Text(
                                        text = "陪伴宇宙 ✦",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = TextPrimary,
                                    )
                                    Text(
                                        text = "继续和你喜欢的角色保持连接",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary,
                                    )
                                }
                                GlassSurface(palette = palette) {
                                    Text(
                                        text = "灵伴",
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = palette.accent,
                                    )
                                }
                            }

                            SectionLabel(
                                text = "当前陪伴角色",
                                modifier = Modifier.padding(top = 28.dp, bottom = 12.dp),
                            )

                            GlassSurface(
                                palette = palette,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Column {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(136.dp),
                                    ) {
                                        AsyncImage(
                                            model = currentRole.backgroundUrl,
                                            contentDescription = currentRole.nickname,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop,
                                        )
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomCenter)
                                                .fillMaxWidth()
                                                .height(34.dp)
                                                .blur(14.dp)
                                                .background(
                                                    Brush.verticalGradient(
                                                        colors = listOf(
                                                            Color.Transparent,
                                                            palette.navGlass.copy(alpha = 0.34f),
                                                            palette.navGlass.copy(alpha = 0.82f),
                                                        ),
                                                    ),
                                                ),
                                        )
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomCenter)
                                                .fillMaxWidth()
                                                .height(20.dp)
                                                .background(
                                                    Brush.verticalGradient(
                                                        colors = listOf(
                                                            Color.Transparent,
                                                            palette.navGlass.copy(alpha = 0.96f),
                                                        ),
                                                    ),
                                                ),
                                        )
                                    }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .offset(y = (-10).dp)
                                            .padding(horizontal = 18.dp)
                                            .padding(bottom = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        AsyncImage(
                                            model = currentRole.avatarUrl,
                                            contentDescription = currentRole.nickname,
                                            modifier = Modifier
                                                .size(60.dp)
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Crop,
                                        )
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(start = 14.dp),
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            ) {
                                                Text(
                                                    text = currentRole.nickname,
                                                    style = MaterialTheme.typography.titleLarge,
                                                    color = TextPrimary,
                                                )
                                                SecondaryTag(text = currentRole.archetype, palette = palette)
                                            }
                                            Text(
                                                text = currentRole.personaSummary,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextSecondary,
                                            )
                                        }
                                    }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 18.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(SuccessGreen),
                                        )
                                        Text(
                                            text = "陪伴中",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary,
                                        )
                                    }
                                    PrimaryGradientButton(
                                        text = "继续聊天",
                                        palette = palette,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 18.dp, vertical = 18.dp),
                                        onClick = {
                                            viewModel.setCurrentRole(currentRole.roleKey)
                                            onOpenChat(currentRole.roleKey)
                                        },
                                    )
                                }
                            }

                            SectionLabel(
                                text = "其他角色",
                                modifier = Modifier.padding(top = 28.dp, bottom = 12.dp),
                            )
                        }
                    }

                    items(otherRoles) { role ->
                        val roleItemPalette = rolePalette(role.roleKey)
                        GlassSurface(
                            palette = roleItemPalette,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                                .clickable {
                                    viewModel.setCurrentRole(role.roleKey)
                                    onOpenChat(role.roleKey)
                                },
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                AsyncImage(
                                    model = role.avatarUrl,
                                    contentDescription = role.nickname,
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(RoundedCornerShape(18.dp)),
                                    contentScale = ContentScale.Crop,
                                )
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 14.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Text(
                                            text = role.nickname,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = TextPrimary,
                                        )
                                        SecondaryTag(text = role.archetype, palette = roleItemPalette)
                                    }
                                    Text(
                                        text = role.personaSummary,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary,
                                    )
                                }
                                Text(
                                    text = "开始聊天 ›",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = roleItemPalette.accent,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
