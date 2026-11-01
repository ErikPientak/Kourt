package com.kourt.app.viewmodel.settings

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.kourt.app.R
import com.kourt.app.data.repository.local.AppPreferencesRepository
import com.kourt.app.data.repository.remote.AuthRepository
import com.kourt.app.data.repository.remote.UserRepository
import com.kourt.app.ui.screens.settings.SettingsScreenActions
import com.kourt.app.ui.screens.settings.SettingsScreenUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "SettingsViewModel"

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val appPreferences: AppPreferencesRepository,
) : ViewModel(), SettingsScreenActions {

    var uiState by mutableStateOf(SettingsScreenUiState())
        private set

    fun onErrorShown() {
        uiState = uiState.copy(error = null)
    }

    override fun onLogOutClick() {
        uiState = uiState.copy(showLogOutDialog = true)
    }

    override fun onConfirmLogOut() {
        authRepository.signOut()
        // The next account on this device must not inherit this one's club, team or child.
        appPreferences.clearSession()
        uiState = uiState.copy(isLoggedOut = true)
        Log.d(TAG, "User signed out")
    }

    override fun onDeleteAccountClick() {
        uiState = uiState.copy(showDeleteDialog = true)
    }

    override fun onConfirmDeleteAccount() {
        if (uiState.isLoading) return
        val uid = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, showDeleteDialog = false)

            // The profile doc must go first: once the auth account is deleted the rules no
            // longer let this user touch it. If the auth delete then fails, put it back.
            val profile = runCatching { userRepository.getUser(uid) }.getOrNull()
            val profileDeleted = profile != null && runCatching { userRepository.deleteUser(uid) }
                .onFailure { Log.e(TAG, "Failed to delete profile of $uid", it) }
                .isSuccess

            authRepository.deleteAccount()
                .onSuccess {
                    Log.d(TAG, "Account deleted successfully")
                    appPreferences.clearSession()
                    uiState = uiState.copy(isLoading = false, isLoggedOut = true)
                }
                .onFailure { e ->
                    Log.e(TAG, "Delete account failed", e)
                    if (profileDeleted && profile != null) {
                        runCatching { userRepository.createUser(profile) }
                            .onFailure { Log.e(TAG, "Failed to restore profile of $uid", it) }
                    }
                    uiState = uiState.copy(
                        isLoading = false,
                        // Firebase only deletes accounts that signed in recently.
                        error = if (e is FirebaseAuthRecentLoginRequiredException) R.string.error_recent_login_required
                                else R.string.error_generic,
                    )
                }
        }
    }

    override fun onDismissDeleteDialog() {
        uiState = uiState.copy(showDeleteDialog = false)
    }

    override fun onDismissLogOutDialog() {
        uiState = uiState.copy(showLogOutDialog = false)
    }

    override fun onLanguageClick() {
        uiState = uiState.copy(showLanguageDialog = true)
    }

    override fun onDismissLanguageDialog() {
        uiState = uiState.copy(showLanguageDialog = false)
    }

    override fun onSaveConsumed() {
        uiState = uiState.copy(isLoggedOut = false)
    }
}
