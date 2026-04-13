package com.kourt.app.ui.screens.auth.register

data class RegisterScreenUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
)
