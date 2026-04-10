package cn.vectorpeak.AIchat_projects.ui.screen.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
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
import cn.vectorpeak.AIchat_projects.ui.component.StatePane
import cn.vectorpeak.AIchat_projects.ui.theme.TextPrimary
import cn.vectorpeak.AIchat_projects.ui.theme.TextSecondary
import cn.vectorpeak.AIchat_projects.ui.theme.rolePalette
import cn.vectorpeak.AIchat_projects.viewmodel.AppViewModelProvider
import cn.vectorpeak.AIchat_projects.viewmodel.OnboardingViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingRoute(
    onSelectRole: (String) -> Unit,
    viewModel: OnboardingViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val rolesState = uiState.rolesState
    val selectedRoleKey = (rolesState as? LoadState.Content)?.value?.getOrNull(uiState.selectedPage)?.roleKey ?: "chenge"
    val palette = rolePalette(selectedRoleKey)

    Box(modifier = Modifier.fillMaxSize()) {
        AmbientBackdrop(palette = palette)
        when (rolesState) {
            LoadState.Loading -> LoadingPane()
            LoadState.Empty -> StatePane(
                title = "还没有可用角色",
                description = "请稍后再试。",
                actionText = "重新加载",
                onAction = viewModel::refreshRoles,
            )
            is LoadState.Error -> StatePane(
                title = "角色加载失败",
                description = rolesState.message,
                actionText = "重新加载",
                onAction = viewModel::refreshRoles,
            )
            is LoadState.Content -> {
                val roles = rolesState.value
                val pagerState = rememberPagerState(
                    initialPage = uiState.selectedPage.coerceIn(0, roles.lastIndex),
                    pageCount = { roles.size },
                )

                LaunchedEffect(pagerState.currentPage) {
                    viewModel.setSelectedPage(pagerState.currentPage)
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 56.dp, bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    GlassSurface(
                        palette = palette,
                        shape = RoundedCornerShape(22.dp),
                        modifier = Modifier.padding(horizontal = 24.dp),
                    ) {
                        Text(
                            text = "✦ 灵 伴 ✦",
                            modifier = Modifier.padding(horizontal = 22.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.titleMedium,
                            color = palette.accent,
                        )
                    }
                    Text(
                        text = "总有一个角色，会接住你",
                        modifier = Modifier.padding(top = 14.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                    )
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = 24.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        pageSpacing = 16.dp,
                    ) { index ->
                        val role = roles[index]
                        val rolePalette = rolePalette(role.roleKey)
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                        ) {
                            GlassSurface(
                                palette = rolePalette,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(30.dp),
                            ) {
                                Column(
                                    modifier = Modifier.padding(18.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    AsyncImage(
                                        model = role.backgroundUrl,
                                        contentDescription = role.nickname,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(196.dp)
                                            .clip(RoundedCornerShape(26.dp)),
                                        contentScale = ContentScale.Crop,
                                    )
                                    Box(
                                        modifier = Modifier
                                            .offset(y = (-36).dp)
                                            .size(88.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.7f))
                                            .padding(4.dp),
                                    ) {
                                        AsyncImage(
                                            model = role.avatarUrl,
                                            contentDescription = role.nickname,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Crop,
                                        )
                                    }
                                    Text(
                                        text = role.nickname,
                                        modifier = Modifier.padding(top = 12.dp),
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = TextPrimary,
                                    )
                                    SecondaryTag(
                                        text = role.archetype,
                                        palette = rolePalette,
                                        modifier = Modifier.padding(top = 8.dp),
                                    )
                                    Text(
                                        text = role.personaSummary,
                                        modifier = Modifier.padding(top = 14.dp),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary,
                                        textAlign = TextAlign.Center,
                                    )
                                    GlassSurface(
                                        palette = rolePalette,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 16.dp),
                                        shape = RoundedCornerShape(22.dp),
                                    ) {
                                        Text(
                                            text = "“${role.openingMessage}”",
                                            modifier = Modifier.padding(16.dp),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary,
                                        )
                                    }
                                    PrimaryGradientButton(
                                        text = "选择 TA 开始聊天",
                                        palette = rolePalette,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 18.dp),
                                        onClick = {
                                            viewModel.completeOnboarding()
                                            onSelectRole(role.roleKey)
                                        },
                                    )
                                }
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.padding(top = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        roles.forEachIndexed { index, role ->
                            val dotPalette = rolePalette(role.roleKey)
                            Box(
                                modifier = Modifier
                                    .size(width = if (index == pagerState.currentPage) 22.dp else 8.dp, height = 8.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(if (index == pagerState.currentPage) dotPalette.accent else Color.White.copy(alpha = 0.45f)),
                            )
                        }
                    }
                    Text(
                        text = "左右滑动探索更多角色",
                        modifier = Modifier.padding(top = 14.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
            }
        }
    }
}
