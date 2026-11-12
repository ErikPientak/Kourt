package com.kourt.app.ui.screens.event.detail

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable

/** Which events of a repeating series a cancel or delete applies to. */
enum class EventScope { THIS, FOLLOWING, ALL }

enum class EventActionType { CANCEL, DELETE }

@Immutable
data class NominatedPlayerUiItem(
    val userId: String,
    val displayName: String,
    val avatarUrl: String,
    val jerseyNumber: Int,
)

@Immutable
data class EventDetailUiItem(
    val eventId: String,
    val teamId: String,
    val title: String,
    val type: String,
    val status: String,
    val formattedDate: String,
    val location: String,
    val notes: String,
    val opponent: String = "",
    /** "home" / "away" for matches, "" otherwise. */
    val venueType: String = "",
)

@Immutable
data class RsvpMemberUiItem(
    val userId: String,
    val displayName: String,
    /** Raw team role ("player", …), shown for people who are coming. */
    val role: String,
    val avatarUrl: String,
    val rsvpStatus: String,
    /** Raw reason key for people who aren't coming ("" = none given); null for people who are. */
    val reason: String? = null,
)

@Immutable
data class EventDetailScreenUiState(
    val isLoading: Boolean = false,
    val event: EventDetailUiItem? = null,
    val myRsvpStatus: String? = null,
    val myRsvpReason: String = "",
    val rsvpYes: Int = 0,
    val rsvpLate: Int = 0,
    val rsvpNo: Int = 0,
    val goingMembers: List<RsvpMemberUiItem> = emptyList(),
    val notGoingMembers: List<RsvpMemberUiItem> = emptyList(),
    val isEditable: Boolean = false,
    val canRsvp: Boolean = false,
    val showAllGoing: Boolean = false,
    val showAllNotGoing: Boolean = false,
    val isRsvpSheetOpen: Boolean = false,
    val isRsvpReasonSheetOpen: Boolean = false,
    val selectedReason: String? = null,
    val reasonNote: String = "",
    val isRsvpSubmitting: Boolean = false,
    val nominatedPlayers: List<NominatedPlayerUiItem> = emptyList(),
    val hasSeries: Boolean = false,
    /** The event has ended, so RSVP is closed. */
    val isPast: Boolean = false,
    /** Cancel/delete dialog being shown; null = none. */
    val pendingAction: EventActionType? = null,
    val actionScope: EventScope = EventScope.THIS,
    val isActionRunning: Boolean = false,
    /** Delete refused because results were recorded; the dialog tells the coach to cancel instead. */
    val deleteBlocked: Boolean = false,
    /** Events of a series delete that were kept because they have recorded results. */
    val skippedCount: Int = 0,
    /** Set once the event is deleted so the screen can close. */
    val isClosed: Boolean = false,
    @StringRes val error: Int? = null,
)
