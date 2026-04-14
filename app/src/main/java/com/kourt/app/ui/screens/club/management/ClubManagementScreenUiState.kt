package com.kourt.app.ui.screens.club.management

import com.kourt.app.data.model.Team

data class ClubManagementScreenUiState(
    val clubId: String = "",
    val clubName: String = "",
    val teams: List<Team> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)
