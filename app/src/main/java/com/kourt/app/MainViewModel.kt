package com.kourt.app

import androidx.lifecycle.ViewModel
import com.kourt.app.data.repository.AuthRepository
import com.kourt.app.navigation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    val startDestination: String
        get() = if (authRepository.isLoggedIn) {
            Destination.HomeScreen.route
        } else {
            Destination.LoginScreen.route
        }
}
