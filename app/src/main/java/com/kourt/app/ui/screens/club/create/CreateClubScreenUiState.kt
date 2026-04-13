package com.kourt.app.ui.screens.club.create

data class CreateClubScreenUiState(
    val clubName: String = "",
    val shortName: String = "",
    val president: String = "",
    val technicalDirector: String = "",
    val country: String = "USA",
    val city: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
)
