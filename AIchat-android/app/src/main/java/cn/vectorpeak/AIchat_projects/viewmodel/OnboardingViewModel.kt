package cn.vectorpeak.AIchat_projects.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.vectorpeak.AIchat_projects.data.model.LoadState
import cn.vectorpeak.AIchat_projects.data.model.OnboardingUiState
import cn.vectorpeak.AIchat_projects.data.repository.AuthRepository
import cn.vectorpeak.AIchat_projects.data.repository.ChatRepository
import cn.vectorpeak.AIchat_projects.data.remote.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val authRepository: AuthRepository,
    private val chatRepository: ChatRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        refreshRoles()
    }

    fun setSelectedPage(index: Int) {
        _uiState.update { it.copy(selectedPage = index) }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            authRepository.saveOnboardingSeen()
        }
    }

    fun refreshRoles() {
        viewModelScope.launch {
            _uiState.update { it.copy(rolesState = LoadState.Loading, pageError = null) }
            runCatching {
                chatRepository.fetchRoles()
            }.onSuccess { roles ->
                _uiState.update {
                    it.copy(
                        rolesState = if (roles.isEmpty()) LoadState.Empty else LoadState.Content(roles),
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        rolesState = LoadState.Error(throwable.toUserMessage()),
                        pageError = throwable.toUserMessage(),
                    )
                }
            }
        }
    }
}
