package com.kourt.app.ui.screens.club.review

import androidx.annotation.StringRes

data class ReviewConfirmScreenUiState(
    val clubName: String = "",
    val shortName: String = "",
    val president: String = "",
    val technicalDirector: String = "",
    val country: String = "",
    val city: String = "",
    val accentColor: String = "#9CA3AF",
    val initials: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    @StringRes val error: Int? = null,
)
