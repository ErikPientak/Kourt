package com.kourt.app.ui.screens.team.create

import com.kourt.app.data.model.User

data class AddTeamScreenUiState(
    val teamName: String = "",
    // Head coach
    val headCoachQuery: String = "",
    val headCoachSuggestions: List<User> = emptyList(),
    val selectedHeadCoach: User? = null,
    // Assistant coaches
    val assistantQuery: String = "",
    val assistantSuggestions: List<User> = emptyList(),
    val selectedAssistants: List<User> = emptyList(),
    // Category: "men" | "women" | "children" | "seniors"
    val category: String = "men",
    // Location & Arena
    val isLocationExpanded: Boolean = false,
    val location: String = "",
    val arena: String = "",
    // Status
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
)
