package com.kourt.app.ui.screens.event.nomination

import androidx.annotation.StringRes

data class NominationPlayerItem(
    val userId: String,
    val displayName: String,
    val avatarUrl: String,
    val attendanceRate: Float,
    val isSelected: Boolean,
)

data class NominationScreenUiState(
    val isLoading: Boolean = false,
    val players: List<NominationPlayerItem> = emptyList(),
    val rosterLimit: Int = 12,
    val isSaved: Boolean = false,
    @StringRes val error: Int? = null,
) {
    val selectedCount: Int get() = players.count { it.isSelected }
}
