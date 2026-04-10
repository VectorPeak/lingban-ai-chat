package cn.vectorpeak.AIchat_projects.data

import cn.vectorpeak.AIchat_projects.data.local.SessionStore
import cn.vectorpeak.AIchat_projects.data.model.SessionSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeSessionStore(
    initial: SessionSnapshot = SessionSnapshot(),
) : SessionStore {
    private val state = MutableStateFlow(initial)

    override val snapshotFlow: Flow<SessionSnapshot> = state.asStateFlow()

    override suspend fun snapshot(): SessionSnapshot = state.value

    override suspend fun setOnboardingSeen(seen: Boolean) {
        state.value = state.value.copy(onboardingSeen = seen)
    }

    override suspend fun saveAuth(
        token: String,
        phoneNumber: String,
        countryCode: String,
    ) {
        state.value = state.value.copy(
            token = token,
            phoneNumber = phoneNumber,
            countryCode = countryCode,
        )
    }

    override suspend fun saveLastRoleKey(roleKey: String) {
        state.value = state.value.copy(lastRoleKey = roleKey)
    }

    override suspend fun clearAuth() {
        state.value = state.value.copy(token = null, phoneNumber = null, countryCode = "86")
    }
}
