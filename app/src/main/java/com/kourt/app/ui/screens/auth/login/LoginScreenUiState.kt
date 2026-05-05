package com.kourt.app.ui.screens.auth.login

import androidx.annotation.StringRes

enum class LoginDestination { NONE, SETUP, CLUB_MANAGEMENT, COACH_DASHBOARD, PLAYER_DASHBOARD }

data class LoginScreenUiState(
    val isLoading: Boolean = false,
    @StringRes val error: Int? = null,
    val destination: LoginDestination = LoginDestination.NONE,
    val passwordResetSent: Boolean = false,
)
