package com.kourt.app.ui.screens.analytics.attendance

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.kourt.app.R

/** How reliably a player attends; [label] is what the card shows. */
enum class Reliability(@StringRes val label: Int) {
    EXCELLENT(R.string.reliability_excellent),
    CONSISTENT(R.string.reliability_consistent),
    NEEDS_ATTENTION(R.string.reliability_needs_attention),
}

@Immutable
data class WeeklyAttendanceBar(
    /** ISO week number, shown as "W15" (en) / "T15" (cs). */
    val weekNumber: Int,
    val rate: Float,           // 0f..1f
    val isCurrentWeek: Boolean = false,
)

@Immutable
data class PlayerAttendanceItem(
    val teamMemberId: String,
    val displayName: String,
    val avatarUrl: String,
    val attendanceRate: Float,   // 0f..1f
    val onTimeRate: Float,       // green progress segment
    val lateRate: Float,         // orange progress segment
    val absentRate: Float,       // red progress segment (excused + unexcused)
    val reliability: Reliability,
)

data class AttendanceAnalyticsScreenUiState(
    val isLoading: Boolean = true,
    @StringRes val error: Int? = null,
    val teamAttendanceRate: Float = 0f,
    val teamOnTimeRate: Float = 0f,
    val teamLateRate: Float = 0f,
    val teamAbsentRate: Float = 0f,
    val mostReliablePlayer: PlayerAttendanceItem? = null,
    val trendBars: List<WeeklyAttendanceBar> = emptyList(),
    val trendDelta: Float = 0f,
    val isTrendImproving: Boolean = true,
    val players: List<PlayerAttendanceItem> = emptyList(),
)
