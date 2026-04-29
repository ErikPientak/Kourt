package com.kourt.app.ui.screens.dashboard.player

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.kourt.app.ui.screens.club.management.ClubMemberUiItem
import com.kourt.app.ui.screens.club.management.MemberFilter
import com.kourt.app.ui.screens.dashboard.coach.EventUiItem
import com.kourt.app.ui.screens.dashboard.coach.TeamUiItem
import java.util.Calendar
import java.util.concurrent.TimeUnit

@Immutable
data class PlayerDashboardScreenUiState(
    val isLoading: Boolean = false,
    val playerName: String = "",
    val teams: List<TeamUiItem> = emptyList(),
    val activeTeam: TeamUiItem? = null,
    val isTeamDropdownOpen: Boolean = false,
    val upNextEvent: EventUiItem? = null,
    val upNextAttendees: List<ClubMemberUiItem> = emptyList(),
    /** The current player's RSVP for upNextEvent. Raw Firestore value: "yes" / "late" / "no" / null. */
    val myRsvpStatus: String? = null,
    val upcomingEvents: List<EventUiItem> = emptyList(),
    val allEvents: List<EventUiItem> = emptyList(),
    val members: List<ClubMemberUiItem> = emptyList(),
    val filteredMembers: List<ClubMemberUiItem> = emptyList(),
    val memberSearchQuery: String = "",
    val memberFilter: MemberFilter = MemberFilter.ALL,
    val isMembersLoading: Boolean = false,
    @StringRes val error: Int? = null,
    val isRsvpSelectionSheetOpen: Boolean = false,
    val isRsvpReasonSheetOpen: Boolean = false,
    val rsvpTargetEventId: String? = null,
    val selectedReason: String? = null,
    val reasonNote: String = "",
    val isRsvpSubmitting: Boolean = false,
    val calendarYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val calendarMonth: Int = Calendar.getInstance().get(Calendar.MONTH),
    val selectedEpochDay: Long = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis()),
    val eventDaysInView: Set<Long> = emptySet(),
    val navigateToEventDetailId: String? = null,
    )
