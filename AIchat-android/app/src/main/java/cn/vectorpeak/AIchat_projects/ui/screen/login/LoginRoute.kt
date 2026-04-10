package cn.vectorpeak.AIchat_projects.ui.screen.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import cn.vectorpeak.AIchat_projects.ui.component.AmbientBackdrop
import cn.vectorpeak.AIchat_projects.ui.component.GlassSurface
import cn.vectorpeak.AIchat_projects.ui.component.PrimaryGradientButton
import cn.vectorpeak.AIchat_projects.ui.component.SecondaryTag
import cn.vectorpeak.AIchat_projects.ui.theme.ErrorRed
import cn.vectorpeak.AIchat_projects.ui.theme.TextPrimary
import cn.vectorpeak.AIchat_projects.ui.theme.TextSecondary
import cn.vectorpeak.AIchat_projects.ui.theme.rolePalette
import cn.vectorpeak.AIchat_projects.viewmodel.AppViewModelProvider
import cn.vectorpeak.AIchat_projects.viewmodel.LoginViewModel

@Composable
fun LoginRoute(
    onNavigateHome: () -> Unit,
    onBack: () -> Unit,
    viewModel: LoginViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val role = uiState.selectedRole
    val palette = rolePalette(role?.roleKey ?: "chenge")

    LaunchedEffect(uiState.navigateToHome) {
        if (uiState.navigateToHome) {
            viewModel.clearNavigation()
            onNavigateHome()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AmbientBackdrop(palette = palette)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .imePadding()
                .padding(horizontal = 24.dp),
        ) {
            Row(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onBack)
                    .background(Color.White.copy(alpha = 0.36f))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(text = "‹", color = palette.accent, style = MaterialTheme.typography.titleMedium)
                Text(text = "返回角色选择", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            }

            role?.let { selectedRole ->
                GlassSurface(
                    palette = palette,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        AsyncImage(
                            model = selectedRole.avatarUrl,
                            contentDescription = selectedRole.nickname,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape),
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = selectedRole.nickname,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary,
                                )
                                SecondaryTag(text = selectedRole.archetype, palette = palette)
                            }
                            Text(
                                text = "登录后即可与 ${selectedRole.nickname} 开始专属聊天",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                            )
                        }
                    }
                }
            }

            Text(
                text = "手机号登录",
                modifier = Modifier.padding(top = 28.dp),
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
            )
            Text(
                text = "登录后即可进入陪伴宇宙，继续你的对话。",
                modifier = Modifier.padding(top = 6.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )

            GlassSurface(
                palette = palette,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 22.dp),
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        FieldGroup(
                            title = "国家 / 地区码",
                            modifier = Modifier.weight(0.34f),
                        ) {
                            FrostedField(
                                value = uiState.countryCode,
                                onValueChange = viewModel::updateCountryCode,
                                placeholder = "+86",
                                keyboardType = KeyboardType.Phone,
                            )
                        }
                        FieldGroup(
                            title = "手机号",
                            modifier = Modifier.weight(0.66f),
                        ) {
                            FrostedField(
                                value = uiState.phoneNumber,
                                onValueChange = viewModel::updatePhoneNumber,
                                placeholder = "请输入手机号",
                                keyboardType = KeyboardType.Phone,
                                isError = uiState.phoneError != null,
                            )
                        }
                    }
                    uiState.phoneError?.let {
                        Text(text = it, color = ErrorRed, style = MaterialTheme.typography.bodySmall)
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        FieldGroup(
                            title = "验证码",
                            modifier = Modifier.weight(1f),
                        ) {
                            FrostedField(
                                value = uiState.verifyCode,
                                onValueChange = viewModel::updateVerifyCode,
                                placeholder = "请输入验证码",
                                keyboardType = KeyboardType.Number,
                                isError = uiState.verifyCodeError != null,
                            )
                        }
                        PrimaryGradientButton(
                            text = if (uiState.sendCodeCooldownSeconds > 0) "${uiState.sendCodeCooldownSeconds}s" else "获取验证码",
                            palette = palette,
                            modifier = Modifier
                                .weight(0.52f)
                                .defaultMinSize(minHeight = 56.dp),
                            enabled = !uiState.sendCodeLoading && uiState.sendCodeCooldownSeconds == 0,
                            onClick = viewModel::sendCode,
                        )
                    }
                    uiState.verifyCodeError?.let {
                        Text(text = it, color = ErrorRed, style = MaterialTheme.typography.bodySmall)
                    }
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(if (uiState.agreedPolicy) palette.accent else Color.White.copy(alpha = 0.45f))
                                .clickable { viewModel.updateAgreedPolicy(!uiState.agreedPolicy) },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (uiState.agreedPolicy) {
                                Text(text = "✓", color = Color.White, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        Text(
                            text = "我已阅读并同意《用户协议》和《隐私政策》",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                        )
                    }
                    uiState.pageError?.let {
                        Text(text = it, color = ErrorRed, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            PrimaryGradientButton(
                text = if (uiState.loginLoading) "登录中…" else "登录并进入首页",
                palette = palette,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp),
                enabled = !uiState.loginLoading,
                onClick = viewModel::login,
            )

            Text(
                text = "数据安全加密，隐私全程保护",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 18.dp),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )
        }
    }
}

@Composable
private fun FrostedField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType,
    isError: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = placeholder,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(18.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = if (isError) ErrorRed else MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
            focusedContainerColor = Color.White.copy(alpha = 0.38f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.26f),
            focusedLabelColor = TextSecondary,
            unfocusedLabelColor = TextSecondary,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            errorBorderColor = ErrorRed,
        ),
    )
}

@Composable
private fun FieldGroup(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        content()
    }
}
