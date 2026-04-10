package cn.vectorpeak.AIchat_projects.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Immutable
data class RolePalette(
    val background: List<Color>,
    val ambientTop: Color,
    val ambientBottom: Color,
    val accent: Color,
    val accentSecondary: Color,
    val tagColor: Color,
    val tagBackground: Color,
    val cardBackground: List<Color>,
    val navGlass: Color,
    val userBubble: List<Color>,
    val border: Color,
)

fun rolePalette(roleKey: String): RolePalette {
    return when (roleKey) {
        "taffy_like" -> RolePalette(
            background = listOf(Color(0xFFFCF0F8), Color(0xFFF8EEFA), Color(0xFFFDF5FB)),
            ambientTop = Color(0x80F8A8D0),
            ambientBottom = Color(0x66E8B0F8),
            accent = Color(0xFFD060A0),
            accentSecondary = Color(0xFFB83888),
            tagColor = Color(0xFFA03080),
            tagBackground = Color(0x26D060A0),
            cardBackground = listOf(Color(0x80FABADC), Color(0x59EEC6FF), Color(0x40FFEEFC)),
            navGlass = Color(0xE0FCF0FC),
            userBubble = listOf(Color(0xFFE890C8), Color(0xFFD060A0)),
            border = Color(0x40D060A0),
        )

        "jiaran_like" -> RolePalette(
            background = listOf(Color(0xFFFDF5F0), Color(0xFFFFF2EA), Color(0xFFFFFBF5)),
            ambientTop = Color(0x80FFCCA0),
            ambientBottom = Color(0x66FFD8B8),
            accent = Color(0xFFE07048),
            accentSecondary = Color(0xFFC85030),
            tagColor = Color(0xFFB04828),
            tagBackground = Color(0x26E07048),
            cardBackground = listOf(Color(0x80FFCEB2), Color(0x59FFE4D0), Color(0x40FFF4EB)),
            navGlass = Color(0xE0FDF5F0),
            userBubble = listOf(Color(0xFFF09870), Color(0xFFE07048)),
            border = Color(0x40E07048),
        )

        "dongxuelian_like" -> RolePalette(
            background = listOf(Color(0xFFEEF2FA), Color(0xFFF0EEF8), Color(0xFFF4F0FA)),
            ambientTop = Color(0x80ACCCE8),
            ambientBottom = Color(0x66C0B8E8),
            accent = Color(0xFF5868A8),
            accentSecondary = Color(0xFF404898),
            tagColor = Color(0xFF404898),
            tagBackground = Color(0x265868A8),
            cardBackground = listOf(Color(0x80B2C8EE), Color(0x59CCC0F8), Color(0x40E4DEF8)),
            navGlass = Color(0xE0EEF2FA),
            userBubble = listOf(Color(0xFF8898C8), Color(0xFF5868A8)),
            border = Color(0x405868A8),
        )

        else -> RolePalette(
            background = listOf(Color(0xFFEBF2F9), Color(0xFFF0F5FC), Color(0xFFF5F8FA)),
            ambientTop = Color(0x80A8C8E8),
            ambientBottom = Color(0x66C0D8F0),
            accent = Color(0xFF5B8DB4),
            accentSecondary = Color(0xFF4A7EA0),
            tagColor = Color(0xFF3A6D94),
            tagBackground = Color(0x265B8DB4),
            cardBackground = listOf(Color(0x80C4DCF2), Color(0x59DAEAF8), Color(0x40EEF6FC)),
            navGlass = Color(0xE0EBF4FC),
            userBubble = listOf(Color(0xFF7AAFD0), Color(0xFF5B8DB4)),
            border = Color(0x405B8DB4),
        )
    }
}

fun RolePalette.backgroundBrush(): Brush = Brush.linearGradient(background)

fun RolePalette.cardBrush(): Brush = Brush.linearGradient(cardBackground)

fun RolePalette.userBubbleBrush(): Brush = Brush.linearGradient(userBubble)
