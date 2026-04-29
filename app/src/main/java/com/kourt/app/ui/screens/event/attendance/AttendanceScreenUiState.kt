package com.kourt.app.ui.screens.event.attendance

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable

enum class AttendanceFilter { ALL, EXCUSED, UNEXCUSED }

@Immutable
data class AttendanceMemberUiItem(
    val userId: String,
    val displayName: String,
    val avatarUrl: String,
    val status: String?, // "on_time" | "late" | "excused" | "unexcused" | null
)

@Immutable
data class AttendanceScreenUiState(
    val isLoading: Boolean = false,
    val eventTitle: String = "",
    val eventDate: String = "",           // e.g. "Oct 17"
    val members: List<AttendanceMemberUiItem> = emptyList(),
    val filter: AttendanceFilter = AttendanceFilter.ALL,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    @StringRes val error: Int? = null,
)
