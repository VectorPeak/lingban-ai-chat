package cn.vectorpeak.AIchat_projects.data.model

sealed interface LoadState<out T> {
    data object Loading : LoadState<Nothing>
    data object Empty : LoadState<Nothing>
    data class Error(val message: String) : LoadState<Nothing>
    data class Content<T>(val value: T) : LoadState<T>
}

data class LoginUiState(
    val countryCode: String = "86",
    val phoneNumber: String = "",
    val verifyCode: String = "",
    val agreedPolicy: Boolean = false,
    val sendCodeLoading: Boolean = false,
    val loginLoading: Boolean = false,
    val sendCodeCooldownSeconds: Int = 0,
    val phoneError: String? = null,
    val verifyCodeError: String? = null,
    val pageError: String? = null,
    val selectedRole: Role? = null,
    val navigateToHome: Boolean = false,
)

data class OnboardingUiState(
    val rolesState: LoadState<List<Role>> = LoadState.Loading,
    val selectedPage: Int = 0,
    val pageError: String? = null,
)

data class HomeUiState(
    val rolesState: LoadState<List<Role>> = LoadState.Loading,
    val currentRoleKey: String? = null,
    val pageError: String? = null,
    val forceLogout: Boolean = false,
)

data class ChatUiState(
    val role: Role? = null,
    val messagesState: LoadState<List<MessageUiModel>> = LoadState.Loading,
    val inputText: String = "",
    val sending: Boolean = false,
    val clearing: Boolean = false,
    val inlineError: String? = null,
    val showClearDialog: Boolean = false,
    val forceLogout: Boolean = false,
)
