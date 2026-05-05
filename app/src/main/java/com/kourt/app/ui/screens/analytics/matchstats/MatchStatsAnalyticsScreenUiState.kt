package com.kourt.app.ui.screens.analytics.matchstats

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable

@Immutable
data class PlayerMatchStatsItem(
    val teamMemberId: String,
    val displayName: String,
    val avatarUrl: String,
    val totalPoints: Int,
    val totalRebounds: Int,
    val totalAssists: Int,
    val totalFouls: Int,
    val freeThrowPct: Float?,   // null when totalFreeThrowsAttempted == 0
)

data class MatchStatsAnalyticsScreenUiState(
    val isLoading: Boolean = true,
    @StringRes val error: Int? = null,
    val wins: Int = 0,
    val losses: Int = 0,
    val draws: Int = 0,
    val matchesPlayed: Int = 0,
    val topScorer: PlayerMatchStatsItem? = null,
    val players: List<PlayerMatchStatsItem> = emptyList(),
)
