package com.kourt.app.ui.screens.auth.login

enum class LoginDestination { NONE, SETUP, SETTINGS, CLUB_MANAGEMENT }

data class LoginScreenUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val destination: LoginDestination = LoginDestination.NONE,
)
