package com.kourt.app.ui.screens.event.aftermatch

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable

@Immutable
data class PlayerStatRowItem(
    val teamMemberId: String,
    val displayName: String,
    val avatarUrl: String,
    val points: Int,
    val rebounds: Int,
    val assists: Int,
    val fouls: Int,
    val freeThrowsAttempted: Int,
    val freeThrowsMade: Int,
) {
    val score get() = points + rebounds + assists
}

@Immutable
data class AfterMatchStatsScreenUiState(
    val isLoading: Boolean = true,
    @StringRes val error: Int? = null,
    val eventTitle: String = "",
    val opponentName: String = "",
    val eventDate: String = "",
    val myTeamScore: Int = 0,
    val opponentScore: Int = 0,
    val isWin: Boolean = false,
    val mvpItem: PlayerStatRowItem? = null,
    val players: List<PlayerStatRowItem> = emptyList(),
)
