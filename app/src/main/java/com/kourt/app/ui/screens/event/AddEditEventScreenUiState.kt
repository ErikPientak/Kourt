package com.kourt.app.ui.screens.event

import androidx.annotation.StringRes

data class AddEditEventScreenUiState(
    val isEditMode: Boolean = false,
    val eventType: String = "practice",
    val sessionName: String = "",
    val selectedEpochDay: Long? = null,
    val startTime: String = "",
    val endTime: String = "",
    val isRepeatEnabled: Boolean = false,
    val repeatDays: Set<Int> = emptySet(),
    val location: String = "",
    val notes: String = "",
    val opponent: String = "",
    val venueType: String = "home",
    val isDatePickerOpen: Boolean = false,
    val isStartTimePickerOpen: Boolean = false,
    val isEndTimePickerOpen: Boolean = false,
    val isRepeatSheetOpen: Boolean = false,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    @StringRes val error: Int? = null,
)

interface AddEditEventScreenActions {
    fun onEventTypeChange(type: String)
    fun onSessionNameChange(name: String)
    fun onDateSelected(epochDay: Long)
    fun onDatePickerOpen()
    fun onDatePickerDismiss()
    fun onStartTimeSelected(time: String)
    fun onStartTimePickerOpen()
    fun onStartTimePickerDismiss()
    fun onEndTimeSelected(time: String)
    fun onEndTimePickerOpen()
    fun onEndTimePickerDismiss()
    fun onRepeatToggle(enabled: Boolean)
    fun onRepeatSheetOpen()
    fun onRepeatSheetDismiss()
    fun onRepeatDaysChanged(days: Set<Int>)
    fun onLocationChange(location: String)
    fun onNotesChange(notes: String)
    fun onOpponentChange(opponent: String)
    fun onVenueTypeChange(type: String)
    fun onNominationClick()
    fun onSave()
    fun onSaveConsumed()
}
