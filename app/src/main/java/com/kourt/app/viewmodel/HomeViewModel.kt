package com.kourt.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kourt.app.data.repository.remote.AuthRepository
import com.kourt.app.data.repository.remote.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "HomeViewModel"

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    fun deleteUser(onComplete: () -> Unit) {
        val uid = authRepository.currentUser?.uid ?: run {
            Log.w(TAG, "deleteUser called but no user is signed in")
            return
        }
        viewModelScope.launch {
            runCatching { userRepository.deleteUser(uid) }
                .onSuccess { Log.d(TAG, "Firestore user document deleted for uid: $uid") }
                .onFailure { Log.e(TAG, "Failed to delete Firestore user document: ${it.message}") }

            authRepository.deleteAccount()
                .onSuccess {
                    Log.d(TAG, "Firebase Auth account deleted successfully")
                    authRepository.signOut()
                    onComplete()
                }
                .onFailure {
                    Log.e(TAG, "Failed to delete Firebase Auth account: ${it.message}")
                }
        }
    }
}
