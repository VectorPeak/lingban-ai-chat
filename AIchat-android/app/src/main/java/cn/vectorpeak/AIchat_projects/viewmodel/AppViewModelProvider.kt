package cn.vectorpeak.AIchat_projects.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.InitializerViewModelFactoryBuilder
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cn.vectorpeak.AIchat_projects.AIchatApplication

object AppViewModelProvider {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            EntryViewModel(
                sessionStore = aichatApplication().container.sessionStore,
            )
        }
        initializer {
            OnboardingViewModel(
                authRepository = aichatApplication().container.authRepository,
                chatRepository = aichatApplication().container.chatRepository,
            )
        }
        initializer {
            LoginViewModel(
                savedStateHandle = createSavedStateHandle(),
                authRepository = aichatApplication().container.authRepository,
                chatRepository = aichatApplication().container.chatRepository,
            )
        }
        initializer {
            HomeViewModel(
                chatRepository = aichatApplication().container.chatRepository,
                authRepository = aichatApplication().container.authRepository,
            )
        }
        initializer {
            ChatViewModel(
                savedStateHandle = createSavedStateHandle(),
                chatRepository = aichatApplication().container.chatRepository,
                authRepository = aichatApplication().container.authRepository,
            )
        }
    }
}

private fun CreationExtras.aichatApplication(): AIchatApplication {
    return this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AIchatApplication
}
