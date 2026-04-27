package com.kourt.app.ui.screens.auth.register

import androidx.annotation.StringRes

data class RegisterScreenUiState(
    val isLoading: Boolean = false,
    @StringRes val error: Int? = null,
    val isSuccess: Boolean = false,
)
