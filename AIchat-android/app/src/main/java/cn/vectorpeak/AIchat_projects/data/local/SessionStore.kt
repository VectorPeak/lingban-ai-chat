package cn.vectorpeak.AIchat_projects.data.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import cn.vectorpeak.AIchat_projects.data.model.SessionSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

interface SessionStore {
    val snapshotFlow: Flow<SessionSnapshot>

    suspend fun snapshot(): SessionSnapshot

    suspend fun setOnboardingSeen(seen: Boolean)

    suspend fun saveAuth(
        token: String,
        phoneNumber: String,
        countryCode: String,
    )

    suspend fun saveLastRoleKey(roleKey: String)

    suspend fun clearAuth()
}

private val Context.aichatDataStore by preferencesDataStore(name = "aichat_session")

class AndroidSessionStore(
    private val context: Context,
) : SessionStore {
    override val snapshotFlow: Flow<SessionSnapshot> =
        context.aichatDataStore.data.map { prefs -> prefs.toSnapshot() }

    override suspend fun snapshot(): SessionSnapshot = snapshotFlow.first()

    override suspend fun setOnboardingSeen(seen: Boolean) {
        context.aichatDataStore.edit { prefs ->
            prefs[Keys.ONBOARDING_SEEN] = seen.toString()
        }
    }

    override suspend fun saveAuth(
        token: String,
        phoneNumber: String,
        countryCode: String,
    ) {
        context.aichatDataStore.edit { prefs ->
            prefs[Keys.TOKEN] = token
            prefs[Keys.PHONE_NUMBER] = phoneNumber
            prefs[Keys.COUNTRY_CODE] = countryCode
        }
    }

    override suspend fun saveLastRoleKey(roleKey: String) {
        context.aichatDataStore.edit { prefs ->
            prefs[Keys.LAST_ROLE_KEY] = roleKey
        }
    }

    override suspend fun clearAuth() {
        context.aichatDataStore.edit { prefs ->
            prefs.remove(Keys.TOKEN)
            prefs.remove(Keys.PHONE_NUMBER)
            prefs.remove(Keys.COUNTRY_CODE)
        }
    }

    private fun Preferences.toSnapshot(): SessionSnapshot {
        return SessionSnapshot(
            onboardingSeen = this[Keys.ONBOARDING_SEEN]?.toBooleanStrictOrNull() ?: false,
            token = this[Keys.TOKEN],
            phoneNumber = this[Keys.PHONE_NUMBER],
            countryCode = this[Keys.COUNTRY_CODE] ?: "86",
            lastRoleKey = this[Keys.LAST_ROLE_KEY],
        )
    }

    private object Keys {
        val ONBOARDING_SEEN = stringPreferencesKey("onboarding_seen")
        val TOKEN = stringPreferencesKey("token")
        val PHONE_NUMBER = stringPreferencesKey("phone_number")
        val COUNTRY_CODE = stringPreferencesKey("country_code")
        val LAST_ROLE_KEY = stringPreferencesKey("last_role_key")
    }
}
