package com.kourt.app.ui.screens.auth.login

import androidx.annotation.StringRes

enum class LoginDestination { NONE, SETUP, SETTINGS, CLUB_MANAGEMENT }

data class LoginScreenUiState(
    val isLoading: Boolean = false,
    @StringRes val error: Int? = null,
    val destination: LoginDestination = LoginDestination.NONE,
)
