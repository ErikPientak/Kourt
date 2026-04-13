package com.kourt.app.ui.screens.auth.login

data class LoginScreenUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
)
