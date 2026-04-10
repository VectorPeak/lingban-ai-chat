package cn.vectorpeak.AIchat_projects.data.model

import kotlinx.serialization.Serializable

enum class AppStartDestination {
    Onboarding,
    Login,
    Home,
}

data class SessionSnapshot(
    val onboardingSeen: Boolean = false,
    val token: String? = null,
    val phoneNumber: String? = null,
    val countryCode: String = "86",
    val lastRoleKey: String? = null,
) {
    val isLoggedIn: Boolean
        get() = !token.isNullOrBlank()
}

data class AuthState(
    val isLoggedIn: Boolean = false,
    val phoneNumber: String? = null,
    val countryCode: String = "86",
)

@Serializable
data class SendCodeRequest(
    val phoneNumber: String,
    val countryCode: String = "86",
    val outId: String? = null,
)

@Serializable
data class SendCodeDataDto(
    val requestId: String,
    val bizId: String? = null,
    val outId: String? = null,
    val validTimeSeconds: Int = 300,
    val codeLength: Int = 6,
)

data class SendCodeResult(
    val requestId: String,
    val validTimeSeconds: Int,
    val codeLength: Int,
)

@Serializable
data class LoginRequest(
    val phoneNumber: String,
    val verifyCode: String,
    val countryCode: String = "86",
    val outId: String? = null,
)

@Serializable
data class LoginUserDto(
    val phoneNumber: String,
    val countryCode: String = "86",
)

@Serializable
data class LoginDataDto(
    val token: String,
    val tokenType: String = "Bearer",
    val tokenFormat: String = "JWT",
    val expiresIn: Long? = null,
    val expiresAt: String? = null,
    val user: LoginUserDto,
)

data class LoginResult(
    val token: String,
    val tokenType: String,
    val expiresIn: Long?,
    val expiresAt: String?,
    val user: LoginUserDto,
)
