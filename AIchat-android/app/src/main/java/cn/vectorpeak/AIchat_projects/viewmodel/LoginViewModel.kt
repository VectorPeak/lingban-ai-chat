package cn.vectorpeak.AIchat_projects.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.vectorpeak.AIchat_projects.data.model.LoadState
import cn.vectorpeak.AIchat_projects.data.model.LoginRequest
import cn.vectorpeak.AIchat_projects.data.model.LoginUiState
import cn.vectorpeak.AIchat_projects.data.model.Role
import cn.vectorpeak.AIchat_projects.data.repository.AuthRepository
import cn.vectorpeak.AIchat_projects.data.repository.ChatRepository
import cn.vectorpeak.AIchat_projects.data.remote.toUserMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
    private val chatRepository: ChatRepository,
) : ViewModel() {
    private val selectedRoleKey: String = savedStateHandle["roleKey"] ?: "chenge"

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        loadRole()
    }

    fun bindRole(role: Role) {
        _uiState.update { current ->
            if (current.selectedRole?.roleKey == role.roleKey) current else current.copy(selectedRole = role)
        }
    }

    fun updateCountryCode(countryCode: String) {
        _uiState.update { it.copy(countryCode = countryCode, phoneError = null, pageError = null) }
    }

    fun updatePhoneNumber(phoneNumber: String) {
        val digitsOnly = phoneNumber.filter(Char::isDigit).take(15)
        _uiState.update { it.copy(phoneNumber = digitsOnly, phoneError = null, pageError = null) }
    }

    fun updateVerifyCode(code: String) {
        val digitsOnly = code.filter(Char::isDigit).take(6)
        _uiState.update { it.copy(verifyCode = digitsOnly, verifyCodeError = null, pageError = null) }
    }

    fun updateAgreedPolicy(agreed: Boolean) {
        _uiState.update { it.copy(agreedPolicy = agreed, phoneError = null, pageError = null) }
    }

    fun clearNavigation() {
        _uiState.update { it.copy(navigateToHome = false) }
    }

    fun sendCode() {
        val state = _uiState.value
        val phoneError = validatePhoneNumber(state.phoneNumber)
        if (phoneError != null) {
            _uiState.update { it.copy(phoneError = phoneError) }
            return
        }
        if (!state.agreedPolicy) {
            _uiState.update { it.copy(phoneError = "请先同意用户协议和隐私政策。") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(sendCodeLoading = true, pageError = null) }
            runCatching {
                authRepository.sendCode(
                    cn.vectorpeak.AIchat_projects.data.model.SendCodeRequest(
                        phoneNumber = state.phoneNumber,
                        countryCode = state.countryCode,
                    ),
                )
            }.onSuccess {
                _uiState.update { current ->
                    current.copy(
                        sendCodeLoading = false,
                        sendCodeCooldownSeconds = 60,
                    )
                }
                countdown()
            }.onFailure { throwable ->
                _uiState.update { current ->
                    current.copy(
                        sendCodeLoading = false,
                        pageError = throwable.toUserMessage(),
                    )
                }
            }
        }
    }

    fun login() {
        val state = _uiState.value
        val phoneError = validatePhoneNumber(state.phoneNumber)
        val codeError = validateVerifyCode(state.verifyCode)
        when {
            phoneError != null -> {
                _uiState.update { it.copy(phoneError = phoneError) }
                return
            }

            codeError != null -> {
                _uiState.update { it.copy(verifyCodeError = codeError) }
                return
            }

            !state.agreedPolicy -> {
                _uiState.update { it.copy(phoneError = "请先同意用户协议和隐私政策。") }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(loginLoading = true, pageError = null) }
            runCatching {
                authRepository.login(
                    LoginRequest(
                        phoneNumber = state.phoneNumber,
                        verifyCode = state.verifyCode,
                        countryCode = state.countryCode,
                    ),
                )
                authRepository.saveLastRoleKey(selectedRoleKey)
            }.onSuccess {
                _uiState.update { current ->
                    current.copy(
                        loginLoading = false,
                        navigateToHome = true,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update { current ->
                    current.copy(
                        loginLoading = false,
                        pageError = throwable.toUserMessage(),
                    )
                }
            }
        }
    }

    private suspend fun countdown() {
        while (_uiState.value.sendCodeCooldownSeconds > 0) {
            delay(1_000)
            _uiState.update { state ->
                state.copy(sendCodeCooldownSeconds = (state.sendCodeCooldownSeconds - 1).coerceAtLeast(0))
            }
        }
    }

    private fun validatePhoneNumber(phoneNumber: String): String? {
        if (phoneNumber.isBlank()) return "请输入手机号。"
        return if (phoneNumber.length !in 6..15) "手机号格式不正确，必须是 6 到 15 位数字。" else null
    }

    private fun validateVerifyCode(code: String): String? {
        if (code.isBlank()) return "请输入验证码。"
        return if (code.length < 4) "验证码长度不正确。" else null
    }

    private fun loadRole() {
        viewModelScope.launch {
            runCatching {
                chatRepository.fetchRoles().firstOrNull { it.roleKey == selectedRoleKey }
            }.onSuccess { role ->
                if (role != null) {
                    bindRole(role)
                } else {
                    _uiState.update { it.copy(pageError = "未找到当前角色，请返回重新选择。") }
                }
            }.onFailure { throwable ->
                _uiState.update { it.copy(pageError = throwable.toUserMessage()) }
            }
        }
    }
}
