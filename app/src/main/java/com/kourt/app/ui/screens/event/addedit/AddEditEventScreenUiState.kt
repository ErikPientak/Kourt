package com.kourt.app.ui.screens.event.addedit

import androidx.annotation.StringRes

data class AddEditEventScreenUiState(
    val isEditMode: Boolean = false,
    val eventType: String = "practice",
    val sessionName: String = "",
    /** The event's date, or the series' first day when creating a repeating event. */
    val selectedEpochDay: Long? = null,
    /** Last day (inclusive) of a repeating series being created. */
    val seriesEndEpochDay: Long? = null,
    val startTime: String = "",
    val endTime: String = "",
    val seriesId: String = "",
    /** ISO days of week: 1 = Monday … 7 = Sunday. */
    val repeatDays: Set<Int> = emptySet(),
    val location: String = "",
    val notes: String = "",
    val opponent: String = "",
    val venueType: String = "home",
    val isDatePickerOpen: Boolean = false,
    val isEndDatePickerOpen: Boolean = false,
    val isStartTimePickerOpen: Boolean = false,
    val isEndTimePickerOpen: Boolean = false,
    val isRepeatSheetOpen: Boolean = false,
    val isEditScopeDialogOpen: Boolean = false,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val navigateToNominationEventId: String? = null,
    @StringRes val error: Int? = null,
)


