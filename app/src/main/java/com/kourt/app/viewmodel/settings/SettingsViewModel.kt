package com.kourt.app.viewmodel.settings

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kourt.app.data.repository.AuthRepository
import com.kourt.app.ui.screens.settings.SettingsScreenActions
import com.kourt.app.ui.screens.settings.SettingsScreenUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "SettingsViewModel"

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel(), SettingsScreenActions {

    var uiState by mutableStateOf(SettingsScreenUiState())
        private set

    override fun onLogOutClick() {
        uiState = uiState.copy(showLogOutDialog = true)
    }

    override fun onConfirmLogOut() {
        authRepository.signOut()
        uiState = uiState.copy(isLoggedOut = true)
        Log.d(TAG, "User signed out")
    }

    override fun onDeleteAccountClick() {
        uiState = uiState.copy(showDeleteDialog = true)
    }

    override fun onConfirmDeleteAccount() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, showDeleteDialog = false)
            authRepository.deleteAccount()
                .onSuccess {
                    Log.d(TAG, "Account deleted successfully")
                    uiState = uiState.copy(isLoading = false, isLoggedOut = true)
                }
                .onFailure { e ->
                    Log.e(TAG, "Delete account failed", e)
                    uiState = uiState.copy(isLoading = false)
                }
        }
    }

    override fun onDismissDeleteDialog() {
        uiState = uiState.copy(showDeleteDialog = false)
    }

    override fun onDismissLogOutDialog() {
        uiState = uiState.copy(showLogOutDialog = false)
    }

    override fun onSaveConsumed() {
        uiState = uiState.copy(isLoggedOut = false)
    }
}
