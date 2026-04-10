package cn.vectorpeak.AIchat_projects

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import cn.vectorpeak.AIchat_projects.ui.navigation.AppNavigation
import cn.vectorpeak.AIchat_projects.ui.theme.AIchat_app_projectsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AIchat_app_projectsTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = androidx.compose.ui.graphics.Color.Transparent,
                    ) { _ ->
                        AppNavigation(modifier = Modifier.fillMaxSize())
                    }
                }
            }
        }
    }
}
