package com.kourt.app.viewmodel.event

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.kourt.app.R
import com.kourt.app.data.model.Event
import com.kourt.app.data.repository.AuthRepository
import com.kourt.app.data.repository.EventRepository
import com.kourt.app.ui.screens.event.AddEditEventScreenActions
import com.kourt.app.ui.screens.event.AddEditEventScreenUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val TAG = "AddEditEventViewModel"

@HiltViewModel
class AddEditEventViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
    private val eventRepository: EventRepository,
) : ViewModel(), AddEditEventScreenActions {

    private val teamId: String = savedStateHandle["teamId"] ?: ""
    private val eventId: String = savedStateHandle["eventId"] ?: ""

    var uiState by mutableStateOf(AddEditEventScreenUiState())
        private set

    init {
        if (eventId.isNotEmpty()) loadEvent(eventId)
    }

    private fun loadEvent(id: String) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            runCatching {
                val event = eventRepository.getEvent(id) ?: return@launch
                val epochDay = TimeUnit.MILLISECONDS.toDays(event.date.toDate().time)
                uiState = uiState.copy(
                    isEditMode = true,
                    isLoading = false,
                    eventType = event.type,
                    sessionName = event.title,
                    selectedEpochDay = epochDay,
                    startTime = event.startTime,
                    endTime = event.endTime,
                    location = event.location,
                    notes = event.notes,
                    opponent = event.opponent,
                    venueType = event.venueType.ifBlank { "home" },
                )
            }.onFailure { e ->
                Log.e(TAG, "Failed to load event $id", e)
                uiState = uiState.copy(isLoading = false, error = R.string.error_load_failed)
            }
        }
    }

    override fun onEventTypeChange(type: String) {
        uiState = uiState.copy(eventType = type)
    }

    override fun onSessionNameChange(name: String) {
        uiState = uiState.copy(sessionName = name, error = null)
    }

    override fun onDateSelected(epochDay: Long) {
        uiState = uiState.copy(selectedEpochDay = epochDay, isDatePickerOpen = false, error = null)
    }

    override fun onDatePickerOpen() { uiState = uiState.copy(isDatePickerOpen = true) }
    override fun onDatePickerDismiss() { uiState = uiState.copy(isDatePickerOpen = false) }

    override fun onStartTimeSelected(time: String) {
        uiState = uiState.copy(startTime = time, isStartTimePickerOpen = false, error = null)
    }

    override fun onStartTimePickerOpen() { uiState = uiState.copy(isStartTimePickerOpen = true) }
    override fun onStartTimePickerDismiss() { uiState = uiState.copy(isStartTimePickerOpen = false) }

    override fun onEndTimeSelected(time: String) {
        uiState = uiState.copy(endTime = time, isEndTimePickerOpen = false, error = null)
    }

    override fun onEndTimePickerOpen() { uiState = uiState.copy(isEndTimePickerOpen = true) }
    override fun onEndTimePickerDismiss() { uiState = uiState.copy(isEndTimePickerOpen = false) }

    override fun onRepeatToggle(enabled: Boolean) {
        uiState = uiState.copy(
            isRepeatEnabled = enabled,
            repeatDays = if (!enabled) emptySet() else uiState.repeatDays,
        )
    }

    override fun onRepeatSheetOpen() { uiState = uiState.copy(isRepeatSheetOpen = true) }
    override fun onRepeatSheetDismiss() { uiState = uiState.copy(isRepeatSheetOpen = false) }

    override fun onRepeatDaysChanged(days: Set<Int>) {
        uiState = uiState.copy(
            repeatDays = days,
            isRepeatEnabled = days.isNotEmpty(),
            isRepeatSheetOpen = false,
        )
    }

    override fun onLocationChange(location: String) { uiState = uiState.copy(location = location) }
    override fun onNotesChange(notes: String) { uiState = uiState.copy(notes = notes) }
    override fun onOpponentChange(opponent: String) { uiState = uiState.copy(opponent = opponent) }
    override fun onVenueTypeChange(type: String) { uiState = uiState.copy(venueType = type) }

    override fun onNominationClick() {
        Log.d(TAG, "onNominationClick — not yet implemented")
    }

    override fun onSave() {
        val state = uiState
        if (state.sessionName.isBlank()) {
            uiState = uiState.copy(error = R.string.error_event_name_required)
            return
        }
        if (state.selectedEpochDay == null) {
            uiState = uiState.copy(error = R.string.error_event_date_required)
            return
        }
        if (state.startTime.isBlank() || state.endTime.isBlank()) {
            uiState = uiState.copy(error = R.string.error_event_time_required)
            return
        }

        val uid = authRepository.currentUser?.uid ?: run {
            uiState = uiState.copy(error = R.string.error_not_signed_in)
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            runCatching {
                val event = Event(
                    id = if (state.isEditMode) eventId else "",
                    teamId = teamId,
                    title = state.sessionName.trim(),
                    type = state.eventType,
                    venueType = if (state.eventType == "match") state.venueType else "",
                    opponent = if (state.eventType == "match") state.opponent.trim() else "",
                    status = "upcoming",
                    date = epochDayToTimestamp(state.selectedEpochDay),
                    startTime = state.startTime,
                    endTime = state.endTime,
                    location = state.location.trim(),
                    notes = state.notes.trim(),
                    createdBy = uid,
                )
                if (state.isEditMode) {
                    eventRepository.updateEvent(event)
                    Log.d(TAG, "Event updated: $eventId")
                } else {
                    val newId = eventRepository.createEvent(event)
                    Log.d(TAG, "Event created: $newId")
                }
                uiState = uiState.copy(isLoading = false, isSaved = true)
            }.onFailure { e ->
                Log.e(TAG, "Failed to save event", e)
                uiState = uiState.copy(isLoading = false, error = R.string.error_load_failed)
            }
        }
    }

    override fun onSaveConsumed() {
        uiState = uiState.copy(isSaved = false)
    }

    private fun epochDayToTimestamp(epochDay: Long): Timestamp =
        Timestamp(Date(TimeUnit.DAYS.toMillis(epochDay)))
}
