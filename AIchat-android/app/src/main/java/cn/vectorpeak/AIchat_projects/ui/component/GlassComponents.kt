package cn.vectorpeak.AIchat_projects.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import cn.vectorpeak.AIchat_projects.ui.theme.FrostBorder
import cn.vectorpeak.AIchat_projects.ui.theme.FrostOverlay
import cn.vectorpeak.AIchat_projects.ui.theme.RolePalette
import cn.vectorpeak.AIchat_projects.ui.theme.TextMuted
import cn.vectorpeak.AIchat_projects.ui.theme.TextPrimary
import cn.vectorpeak.AIchat_projects.ui.theme.TextSecondary
import cn.vectorpeak.AIchat_projects.ui.theme.backgroundBrush
import cn.vectorpeak.AIchat_projects.ui.theme.cardBrush
import cn.vectorpeak.AIchat_projects.ui.theme.userBubbleBrush

@Composable
fun AmbientBackdrop(
    palette: RolePalette,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.backgroundBrush()),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(280.dp)
                .background(palette.ambientTop, CircleShape),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .size(220.dp)
                .background(palette.ambientBottom, CircleShape),
        )
    }
}

@Composable
fun GlassSurface(
    palette: RolePalette,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(24.dp),
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = FrostOverlay,
        border = androidx.compose.foundation.BorderStroke(1.dp, palette.border),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        content = content,
    )
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    palette: RolePalette,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(palette.cardBrush())
            .border(width = 1.dp, color = palette.border, shape = RoundedCornerShape(28.dp))
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        content = content,
    )
}

@Composable
fun PrimaryGradientButton(
    text: String,
    palette: RolePalette,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier,
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(22.dp),
        color = Color.Transparent,
        border = null,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier
                .background(
                    if (enabled) palette.userBubbleBrush() else Brush.linearGradient(
                        listOf(Color(0x66C8CBD8), Color(0x66B7BBC9)),
                    ),
                )
                .padding(vertical = 15.dp, horizontal = 18.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun SecondaryTag(
    text: String,
    palette: RolePalette,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(palette.tagBackground)
            .border(1.dp, palette.border, RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = text,
            color = palette.tagColor,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
fun NetworkAvatar(
    imageUrl: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    AsyncImage(
        model = imageUrl,
        contentDescription = contentDescription,
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.6f)),
    )
}

@Composable
fun LoadingPane(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun StatePane(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(28.dp),
            color = FrostOverlay,
            border = androidx.compose.foundation.BorderStroke(1.dp, FrostBorder),
        ) {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(text = title, style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                Text(text = description, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                if (actionText != null && onAction != null) {
                    Surface(
                        onClick = onAction,
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.primary,
                    ) {
                        Text(
                            text = actionText,
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                            color = Color.White,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.labelMedium,
        color = TextMuted,
        fontWeight = FontWeight.SemiBold,
    )
}
