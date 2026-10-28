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
import com.kourt.app.data.repository.remote.AuthRepository
import com.kourt.app.data.repository.remote.EventRepository
import com.kourt.app.ui.screens.event.addedit.AddEditEventScreenActions
import com.kourt.app.ui.screens.event.addedit.AddEditEventScreenUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Date
import java.util.UUID
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

    override fun onEndDateSelected(epochDay: Long) {
        uiState = uiState.copy(seriesEndEpochDay = epochDay, isEndDatePickerOpen = false, error = null)
    }

    override fun onEndDatePickerOpen() { uiState = uiState.copy(isEndDatePickerOpen = true) }
    override fun onEndDatePickerDismiss() { uiState = uiState.copy(isEndDatePickerOpen = false) }

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
            seriesEndEpochDay = if (!enabled) null else uiState.seriesEndEpochDay,
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
        if (eventId.isNotEmpty()) {
            uiState = uiState.copy(navigateToNominationEventId = eventId)
        }
    }

    override fun onNominationConsumed() {
        uiState = uiState.copy(navigateToNominationEventId = null)
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

        // A new repeating event covers Starts on … Ends on instead of a single date.
        val isNewSeries = !state.isEditMode && state.repeatDays.isNotEmpty()
        val occurrenceDays = if (isNewSeries) {
            val endDay = state.seriesEndEpochDay
            val seriesError = when {
                endDay == null -> R.string.error_event_date_required
                endDay < state.selectedEpochDay -> R.string.error_event_end_before_start
                endDay - state.selectedEpochDay > MAX_SERIES_SPAN_DAYS -> R.string.error_event_range_too_long
                else -> null
            }
            if (seriesError != null) {
                uiState = uiState.copy(error = seriesError)
                return
            }
            val days = calculateOccurrenceDates(state.selectedEpochDay, endDay!!, state.repeatDays)
            if (days.isEmpty()) {
                uiState = uiState.copy(error = R.string.error_event_no_occurrences)
                return
            }
            days
        } else {
            listOf(state.selectedEpochDay)
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
                    uiState = uiState.copy(isLoading = false, isSaved = true)
                } else {
                    val newSeriesId = if (isNewSeries) UUID.randomUUID().toString() else ""
                    val event = buildEvent(state, uid).copy(id = "", seriesId = newSeriesId)
                    var createdMatchId = ""
                    occurrenceDays.forEach { day ->
                        val newId = eventRepository.createEvent(event.copy(date = epochDayToTimestamp(day)))
                        if (state.eventType == "match") createdMatchId = newId
                        Log.d(TAG, "Event created: $newId")
                    }
                    if (createdMatchId.isNotEmpty()) {
                        uiState = uiState.copy(isLoading = false, navigateToNominationEventId = createdMatchId)
                    } else {
                        uiState = uiState.copy(isLoading = false, isSaved = true)
                    }
                }
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
                val following = eventRepository.getEventsBySeriesId(teamId, state.seriesId)
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
}

/** Longest allowed series, so a mis-tapped end date can't create hundreds of events. */
private const val MAX_SERIES_SPAN_DAYS = 366L

/**
 * Every day from [startEpochDay] to [endEpochDay] (both inclusive) whose ISO day of week
 * (1 = Monday … 7 = Sunday, as [java.time.DayOfWeek.getValue]) is in [repeatDays].
 */
internal fun calculateOccurrenceDates(
    startEpochDay: Long,
    endEpochDay: Long,
    repeatDays: Set<Int>,
): List<Long> {
    val endDate = LocalDate.ofEpochDay(endEpochDay)
    return generateSequence(LocalDate.ofEpochDay(startEpochDay)) { it.plusDays(1) }
        .takeWhile { !it.isAfter(endDate) }
        .filter { it.dayOfWeek.value in repeatDays }
        .map { it.toEpochDay() }
        .toList()
}
