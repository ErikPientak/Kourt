package com.kourt.app.viewmodel.auth

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kourt.app.R
import com.kourt.app.data.model.User
import com.kourt.app.data.repository.AuthRepository
import com.kourt.app.data.repository.UserRepository
import com.kourt.app.ui.screens.auth.register.RegisterScreenActions
import com.kourt.app.ui.screens.auth.register.RegisterScreenUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "RegisterViewModel"

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
) : ViewModel(), RegisterScreenActions {

    var uiState by mutableStateOf(RegisterScreenUiState())
        private set

    override fun onRegister(fullName: String, email: String, password: String, confirmPassword: String) {
        if (password != confirmPassword) {
            Log.w(TAG, "Registration failed: passwords do not match")
            uiState = uiState.copy(error = R.string.error_passwords_do_not_match)
            return
        }
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            val result = authRepository.signUpWithEmail(email, password)
            if (result.isSuccess) {
                val firebaseUser = result.getOrNull()!!
                Log.d(TAG, "Firebase Auth registration successful: ${firebaseUser.email}")
                runCatching {
                    userRepository.createUser(
                        User(
                            id = firebaseUser.uid,
                            displayName = fullName.trim(),
                            email = email.trim(),
                        )
                    )
                }.onSuccess {
                    Log.d(TAG, "Firestore user document created for uid: ${firebaseUser.uid}")
                }.onFailure {
                    Log.e(TAG, "Failed to create Firestore user document: ${it.message}")
                }
                uiState = uiState.copy(isLoading = false, isSuccess = true)
            } else {
                Log.e(TAG, "Registration failed: ${result.exceptionOrNull()?.message}")
                uiState = uiState.copy(isLoading = false, error = R.string.error_registration_failed)
            }
        }
    }

    override fun onClearError() {
        uiState = uiState.copy(error = null)
    }

    override fun onSaveConsumed() {
        uiState = uiState.copy(isSuccess = false)
    }
}
