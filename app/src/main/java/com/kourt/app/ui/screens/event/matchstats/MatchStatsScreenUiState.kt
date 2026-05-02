package com.kourt.app.ui.screens.event.matchstats

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable

@Immutable
data class PlayerStatEntryUiItem(
    val userId: String,
    val teamMemberId: String,
    val displayName: String,
    val avatarUrl: String,
    val jerseyNumber: Int,
    val points: Int = 0,
    val rebounds: Int = 0,
    val assists: Int = 0,
    val fouls: Int = 0,
    val freeThrowsAttempted: Int = 0,
    val freeThrowsMade: Int = 0,
)

enum class StatField {
    POINTS, REBOUNDS, ASSISTS, FOULS, FREE_THROWS_ATTEMPTED, FREE_THROWS_MADE
}

@Immutable
data class MatchStatsScreenUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val eventTitle: String = "",
    val opponentScore: Int = 0,
    val players: List<PlayerStatEntryUiItem> = emptyList(),
    @StringRes val error: Int? = null,
) {
    val myTeamScore: Int get() = players.sumOf { it.points }
}
