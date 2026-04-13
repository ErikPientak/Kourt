package com.kourt.app.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.kourt.app.data.repository.AuthRepository
import com.kourt.app.ui.screens.auth.login.LoginScreenActions
import com.kourt.app.ui.screens.auth.login.LoginScreenUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "LoginViewModel"

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel(), LoginScreenActions {

    var uiState by mutableStateOf(LoginScreenUiState())
        private set

    override fun onSignInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            val result = authRepository.signInWithEmail(email, password)
            if (result.isSuccess) {
                Log.d(TAG, "Email sign-in successful: ${result.getOrNull()?.email}")
                uiState = uiState.copy(isLoading = false, isSuccess = true)
            } else {
                val error = result.exceptionOrNull()?.message ?: "Login failed"
                Log.e(TAG, "Email sign-in failed: $error")
                uiState = uiState.copy(isLoading = false, error = error)
            }
        }
    }

    override fun onSignInWithGoogle(context: Context) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            val result = authRepository.signInWithGoogle(context)
            if (result.isSuccess) {
                Log.d(TAG, "Google sign-in successful: ${result.getOrNull()?.email}")
                uiState = uiState.copy(isLoading = false, isSuccess = true)
            } else {
                val error = result.exceptionOrNull()?.message ?: "Google sign-in failed"
                Log.e(TAG, "Google sign-in failed: $error")
                uiState = uiState.copy(isLoading = false, error = error)
            }
        }
    }

    override fun onClearError() {
        uiState = uiState.copy(error = null)
    }
}
