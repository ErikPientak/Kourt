package com.kourt.app.ui.screens.settings

import androidx.annotation.StringRes

data class SettingsScreenUiState(
    val isLoading: Boolean = false,
    val isLoggedOut: Boolean = false,
    val showDeleteDialog: Boolean = false,
    @StringRes val error: Int? = null,
)
