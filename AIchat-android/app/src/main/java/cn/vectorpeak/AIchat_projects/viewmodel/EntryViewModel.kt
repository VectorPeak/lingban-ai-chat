package cn.vectorpeak.AIchat_projects.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.vectorpeak.AIchat_projects.data.local.SessionStore
import cn.vectorpeak.AIchat_projects.data.model.AppStartDestination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EntryViewModel(
    private val sessionStore: SessionStore,
) : ViewModel() {
    private val _destination = MutableStateFlow<AppStartDestination?>(null)
    val destination: StateFlow<AppStartDestination?> = _destination.asStateFlow()

    init {
        viewModelScope.launch {
            val snapshot = sessionStore.snapshot()
            _destination.value = when {
                !snapshot.onboardingSeen -> AppStartDestination.Onboarding
                snapshot.isLoggedIn -> AppStartDestination.Home
                else -> AppStartDestination.Login
            }
        }
    }
}
