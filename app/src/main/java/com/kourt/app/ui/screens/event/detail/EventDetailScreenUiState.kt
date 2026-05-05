package com.kourt.app.ui.screens.event.detail

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable

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
)

@Immutable
data class RsvpMemberUiItem(
    val userId: String,
    val displayName: String,
    val subtitle: String,
    val avatarUrl: String,
    val rsvpStatus: String,
)

@Immutable
data class EventDetailScreenUiState(
    val isLoading: Boolean = false,
    val event: EventDetailUiItem? = null,
    val myRsvpStatus: String? = null,
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
    @StringRes val error: Int? = null,
)
