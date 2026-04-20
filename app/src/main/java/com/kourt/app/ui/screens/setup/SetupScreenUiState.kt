package com.kourt.app.ui.screens.setup

import androidx.annotation.StringRes

data class SetupScreenUiState(
    val joinCode: String = "",
    val isLoading: Boolean = false,
    @StringRes val error: Int? = null,
    val isSuccess: Boolean = false,
)
