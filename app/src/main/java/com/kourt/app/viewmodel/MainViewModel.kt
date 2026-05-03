package com.kourt.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.kourt.app.data.repository.local.AppPreferencesRepository
import com.kourt.app.data.repository.remote.AuthRepository
import com.kourt.app.navigation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val appPreferences: AppPreferencesRepository,
) : ViewModel() {

    val startDestination: String
        get() = if (authRepository.isLoggedIn) {
            Destination.HomeScreen.route
        } else {
            Destination.LoginScreen.route
        }

    var isDarkTheme by mutableStateOf(appPreferences.isDarkTheme)
        private set

    var language by mutableStateOf(appPreferences.language)
        private set

    fun toggleTheme() {
        val newValue = !isDarkTheme
        appPreferences.isDarkTheme = newValue
        isDarkTheme = newValue
    }

    fun updateLanguage(code: String) {
        appPreferences.language = code
        language = code
    }
}