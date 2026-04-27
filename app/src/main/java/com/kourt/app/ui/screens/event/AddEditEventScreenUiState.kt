package com.kourt.app.ui.screens.event

import androidx.annotation.StringRes

data class AddEditEventScreenUiState(
    val isEditMode: Boolean = false,
    val eventType: String = "practice",
    val sessionName: String = "",
    val selectedEpochDay: Long? = null,
    val startTime: String = "",
    val endTime: String = "",
    val seriesId: String = "",
    val repeatDays: Set<Int> = emptySet(),
    val location: String = "",
    val notes: String = "",
    val opponent: String = "",
    val venueType: String = "home",
    val isDatePickerOpen: Boolean = false,
    val isStartTimePickerOpen: Boolean = false,
    val isEndTimePickerOpen: Boolean = false,
    val isRepeatSheetOpen: Boolean = false,
    val isEditScopeDialogOpen: Boolean = false,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    @StringRes val error: Int? = null,
)


