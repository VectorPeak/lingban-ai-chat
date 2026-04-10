package cn.vectorpeak.AIchat_projects.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cn.vectorpeak.AIchat_projects.data.model.AppStartDestination
import cn.vectorpeak.AIchat_projects.ui.screen.chat.ChatRoute
import cn.vectorpeak.AIchat_projects.ui.screen.entry.EntryRoute
import cn.vectorpeak.AIchat_projects.ui.screen.home.HomeRoute
import cn.vectorpeak.AIchat_projects.ui.screen.login.LoginRoute
import cn.vectorpeak.AIchat_projects.ui.screen.onboarding.OnboardingRoute
import cn.vectorpeak.AIchat_projects.viewmodel.AppViewModelProvider
import cn.vectorpeak.AIchat_projects.viewmodel.EntryViewModel

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val entryViewModel: EntryViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val entryDestination = entryViewModel.destination.collectAsStateWithLifecycle().value

    NavHost(
        navController = navController,
        startDestination = AppDestination.Entry,
        modifier = modifier,
    ) {
        composable(route = AppDestination.Entry) {
            EntryRoute(destination = entryDestination)
            LaunchedEffect(entryDestination) {
                when (entryDestination) {
                    AppStartDestination.Onboarding -> {
                        navController.navigate(AppDestination.Onboarding) {
                            popUpTo(AppDestination.Entry) { inclusive = true }
                        }
                    }

                    AppStartDestination.Login -> {
                        navController.navigate(AppDestination.login(roleKey = null)) {
                            popUpTo(AppDestination.Entry) { inclusive = true }
                        }
                    }

                    AppStartDestination.Home -> {
                        navController.navigate(AppDestination.Home) {
                            popUpTo(AppDestination.Entry) { inclusive = true }
                        }
                    }

                    null -> Unit
                }
            }
        }

        composable(route = AppDestination.Onboarding) {
            OnboardingRoute(
                onSelectRole = { roleKey ->
                    navController.navigate(AppDestination.login(roleKey)) {
                        popUpTo(AppDestination.Onboarding) { inclusive = true }
                    }
                },
            )
        }

        composable(
            route = AppDestination.Login,
            arguments = listOf(
                navArgument("roleKey") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = "chenge"
                },
            ),
        ) {
            LoginRoute(
                onNavigateHome = {
                    navController.navigate(AppDestination.Home) {
                        popUpTo(AppDestination.Login) { inclusive = true }
                    }
                },
                onBack = {
                    navController.popBackStack()
                },
            )
        }

        composable(route = AppDestination.Home) {
            HomeRoute(
                onOpenChat = { roleKey ->
                    navController.navigate(AppDestination.chat(roleKey))
                },
                onLogoutRequired = {
                    navController.navigate(AppDestination.login(roleKey = null)) {
                        popUpTo(AppDestination.Home) { inclusive = true }
                    }
                },
            )
        }

        composable(
            route = AppDestination.Chat,
            arguments = listOf(
                navArgument("roleKey") {
                    type = NavType.StringType
                },
            ),
        ) {
            ChatRoute(
                onNavigateBack = {
                    navController.navigate(AppDestination.Home) {
                        popUpTo(AppDestination.Home) { inclusive = true }
                    }
                },
                onLogoutRequired = {
                    navController.navigate(AppDestination.login(roleKey = null)) {
                        popUpTo(AppDestination.Home) { inclusive = false }
                    }
                },
            )
        }
    }
}
