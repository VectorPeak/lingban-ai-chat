package cn.vectorpeak.AIchat_projects.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AIchatLightColorScheme = lightColorScheme(
    primary = Color(0xFF6E7FD8),
    secondary = Color(0xFFF0EDF8),
    tertiary = Color(0xFFFAD9E8),
    background = MistWhite,
    surface = FogSurface,
    surfaceVariant = Color(0x66FFFFFF),
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    error = ErrorRed,
)

@Composable
fun AIchat_app_projectsTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = AIchatLightColorScheme,
        typography = Typography,
        content = content,
    )
}
