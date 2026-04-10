package cn.vectorpeak.AIchat_projects.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.vectorpeak.AIchat_projects.data.model.HomeUiState
import cn.vectorpeak.AIchat_projects.data.model.LoadState
import cn.vectorpeak.AIchat_projects.data.repository.AuthRepository
import cn.vectorpeak.AIchat_projects.data.repository.ChatRepository
import cn.vectorpeak.AIchat_projects.data.remote.UnauthorizedException
import cn.vectorpeak.AIchat_projects.data.remote.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val snapshot = authRepository.snapshot()
            _uiState.update { it.copy(rolesState = LoadState.Loading, currentRoleKey = snapshot.lastRoleKey) }
            runCatching {
                chatRepository.fetchRoles()
            }.onSuccess { roles ->
                val currentRoleKey = snapshot.lastRoleKey ?: roles.firstOrNull()?.roleKey
                _uiState.update {
                    it.copy(
                        rolesState = if (roles.isEmpty()) LoadState.Empty else LoadState.Content(roles),
                        currentRoleKey = currentRoleKey,
                        pageError = null,
                        forceLogout = false,
                    )
                }
            }.onFailure { throwable ->
                if (throwable is UnauthorizedException) {
                    authRepository.clearAuth()
                    _uiState.update {
                        it.copy(
                            rolesState = LoadState.Error(throwable.toUserMessage()),
                            forceLogout = true,
                        )
                    }
                } else {
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

    fun setCurrentRole(roleKey: String) {
        _uiState.update { it.copy(currentRoleKey = roleKey) }
        viewModelScope.launch {
            authRepository.saveLastRoleKey(roleKey)
        }
    }

    fun clearForceLogout() {
        _uiState.update { it.copy(forceLogout = false) }
    }
}
