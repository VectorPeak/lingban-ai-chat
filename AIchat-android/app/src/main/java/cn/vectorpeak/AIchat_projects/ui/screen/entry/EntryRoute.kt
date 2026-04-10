package cn.vectorpeak.AIchat_projects.ui.screen.entry

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cn.vectorpeak.AIchat_projects.data.model.AppStartDestination
import cn.vectorpeak.AIchat_projects.ui.component.AmbientBackdrop
import cn.vectorpeak.AIchat_projects.ui.theme.rolePalette

@Composable
fun EntryRoute(
    destination: AppStartDestination?,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        AmbientBackdrop(palette = rolePalette("dongxuelian_like"))
        if (destination == null) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
