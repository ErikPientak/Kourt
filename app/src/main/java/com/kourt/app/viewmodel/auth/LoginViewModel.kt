package com.kourt.app.viewmodel.auth

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.kourt.app.data.model.User
import com.kourt.app.data.repository.AuthRepository
import com.kourt.app.data.repository.UserRepository
import com.kourt.app.ui.screens.auth.login.LoginScreenActions
import com.kourt.app.ui.screens.auth.login.LoginScreenUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "LoginViewModel"

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
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
                val firebaseUser = result.getOrNull()!!
                Log.d(TAG, "Google sign-in successful: ${firebaseUser.email}")
                val existingUser = runCatching { userRepository.getUser(firebaseUser.uid) }.getOrNull()
                if (existingUser == null) {
                    runCatching {
                        userRepository.createUser(
                            User(
                                id = firebaseUser.uid,
                                displayName = firebaseUser.displayName.orEmpty(),
                                email = firebaseUser.email.orEmpty(),
                                photoURL = firebaseUser.photoUrl?.toString().orEmpty(),
                            )
                        )
                    }.onSuccess {
                        Log.d(TAG, "Firestore user document created for Google uid: ${firebaseUser.uid}")
                    }.onFailure {
                        Log.e(TAG, "Failed to create Firestore user document: ${it.message}")
                    }
                }
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
