package com.kourt.app.viewmodel.event

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.identity.util.UUID
import com.google.firebase.Timestamp
import com.kourt.app.R
import com.kourt.app.data.model.Event
import com.kourt.app.data.repository.AuthRepository
import com.kourt.app.data.repository.EventRepository
import com.kourt.app.ui.screens.event.AddEditEventScreenActions
import com.kourt.app.ui.screens.event.AddEditEventScreenUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
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
    private var originalEpochDay: Long? = null

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
                originalEpochDay = epochDay
                uiState = uiState.copy(
                    isEditMode = true,
                    isLoading = false,
                    eventType = event.type,
                    sessionName = event.title,
                    seriesId = event.seriesId,
                    repeatDays = event.repeatDays.toSet(),
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
            repeatDays = if (!enabled) emptySet() else uiState.repeatDays,
        )
    }

    override fun onRepeatSheetOpen() { uiState = uiState.copy(isRepeatSheetOpen = true) }
    override fun onRepeatSheetDismiss() { uiState = uiState.copy(isRepeatSheetOpen = false) }

    override fun onRepeatDaysChanged(days: Set<Int>) {
        uiState = uiState.copy(
            repeatDays = days,
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

        if (state.isEditMode && state.seriesId.isNotBlank()) {
            uiState = uiState.copy(isEditScopeDialogOpen = true)
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            runCatching {
                if (state.isEditMode) {
                    eventRepository.updateEvent(buildEvent(state, uid))
                    Log.d(TAG, "Event updated: $eventId")
                } else {
                    val newSeriesId = if (state.repeatDays.isNotEmpty()) UUID.randomUUID().toString() else ""
                    val occurrenceDays = if (state.repeatDays.isNotEmpty()) {
                        calculateOccurrenceDates(state.selectedEpochDay, state.repeatDays)
                    } else {
                        listOf(state.selectedEpochDay)
                    }
                    val event = buildEvent(state, uid).copy(id = "", seriesId = newSeriesId)
                    occurrenceDays.forEach { day ->
                        val newId = eventRepository.createEvent(event.copy(date = epochDayToTimestamp(day)))
                        Log.d(TAG, "Event created: $newId")
                    }
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

    override fun onSaveThisOnly() {
        val state = uiState
        val uid = authRepository.currentUser?.uid ?: return
        uiState = uiState.copy(isEditScopeDialogOpen = false)
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            runCatching {
                eventRepository.updateEvent(buildEvent(state, uid))
                Log.d(TAG, "Event updated (this only): $eventId")
                uiState = uiState.copy(isLoading = false, isSaved = true)
            }.onFailure { e ->
                Log.e(TAG, "Failed to save event", e)
                uiState = uiState.copy(isLoading = false, error = R.string.error_load_failed)
            }
        }
    }

    override fun onSaveThisAndFollowing() {
        val state = uiState
        val uid = authRepository.currentUser?.uid ?: return
        val origDay = originalEpochDay ?: return
        uiState = uiState.copy(isEditScopeDialogOpen = false)
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            runCatching {
                eventRepository.updateEvent(buildEvent(state, uid))
                val following = eventRepository.getEventsBySeriesId(state.seriesId)
                    .filter {
                        TimeUnit.MILLISECONDS.toDays(it.date.toDate().time) > origDay &&
                        it.id != eventId
                    }
                following.forEach { existing ->
                    eventRepository.updateEvent(
                        existing.copy(
                            title = state.sessionName.trim(),
                            type = state.eventType,
                            venueType = if (state.eventType == "match") state.venueType else "",
                            opponent = if (state.eventType == "match") state.opponent.trim() else "",
                            startTime = state.startTime,
                            endTime = state.endTime,
                            location = state.location.trim(),
                            notes = state.notes.trim(),
                        )
                    )
                }
                Log.d(TAG, "Updated ${following.size + 1} events in series ${state.seriesId}")
                uiState = uiState.copy(isLoading = false, isSaved = true)
            }.onFailure { e ->
                Log.e(TAG, "Failed to save events in series", e)
                uiState = uiState.copy(isLoading = false, error = R.string.error_load_failed)
            }
        }
    }

    override fun onEditScopeDialogDismiss() {
        uiState = uiState.copy(isEditScopeDialogOpen = false)
    }

    private fun buildEvent(state: AddEditEventScreenUiState, uid: String) = Event(
        id = eventId,
        seriesId = state.seriesId,
        repeatDays = state.repeatDays.toList(),
        teamId = teamId,
        title = state.sessionName.trim(),
        type = state.eventType,
        venueType = if (state.eventType == "match") state.venueType else "",
        opponent = if (state.eventType == "match") state.opponent.trim() else "",
        status = "upcoming",
        date = epochDayToTimestamp(state.selectedEpochDay!!),
        startTime = state.startTime,
        endTime = state.endTime,
        location = state.location.trim(),
        notes = state.notes.trim(),
        createdBy = uid,
    )

    override fun onCancel() {
        if (eventId.isEmpty()) return
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            runCatching {
                eventRepository.cancelEvent(eventId)
                Log.d(TAG, "Event cancelled: $eventId")
                uiState = uiState.copy(isLoading = false, isSaved = true)
            }.onFailure { e ->
                Log.e(TAG, "Failed to cancel event $eventId", e)
                uiState = uiState.copy(isLoading = false, error = R.string.error_load_failed)
            }
        }
    }

    private fun epochDayToTimestamp(epochDay: Long): Timestamp =
        Timestamp(Date(TimeUnit.DAYS.toMillis(epochDay)))

    private fun calculateOccurrenceDates(
        startEpochDay: Long,
        repeatDays: Set<Int>,
        weeksToCreate: Int = 12
    ): List<Long> {
        val dates = mutableListOf<Long>()
        val startDate = LocalDate.ofEpochDay(startEpochDay)
        val endLimit = startDate.plusWeeks(weeksToCreate.toLong())

        var currentDate = startDate

        // We loop day-by-day until we hit the 12-week limit
        while (currentDate.isBefore(endLimit) || currentDate.isEqual(endLimit)) {

            // java.time.DayOfWeek: 1 (Mon) ... 7 (Sun)
            // Ensure your UI/Picker also uses 1-7!
            val currentDayOfWeek = currentDate.dayOfWeek.value

            if (repeatDays.contains(currentDayOfWeek)) {
                dates.add(currentDate.toEpochDay())
            }

            // Increment by exactly 1 day every time
            currentDate = currentDate.plusDays(1)
        }
        return dates
    }
}
