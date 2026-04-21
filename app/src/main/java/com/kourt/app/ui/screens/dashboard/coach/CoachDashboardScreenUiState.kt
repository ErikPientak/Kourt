package com.kourt.app.ui.screens.dashboard.coach

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.kourt.app.ui.screens.club.management.ClubMemberUiItem
import com.kourt.app.ui.screens.club.management.MemberFilter
import java.util.Calendar
import java.util.concurrent.TimeUnit

@Immutable
data class TeamUiItem(
    val teamId: String,
    val teamName: String,
    val clubId: String,
)

@Immutable
data class EventUiItem(
    val eventId: String,
    val title: String,
    val type: String,
    val status: String,
    val dayOfWeek: String,
    val dayOfMonth: Int,
    val startTime: String,
    val location: String,
    val isToday: Boolean,
    val dateLabel: String,
    val epochDay: Long,
    val subtitle: String,
)

data class CoachDashboardScreenUiState(
    val isLoading: Boolean = false,
    val coachName: String = "",
    val teams: List<TeamUiItem> = emptyList(),
    val activeTeam: TeamUiItem? = null,
    val isTeamDropdownOpen: Boolean = false,
    val upNextEvent: EventUiItem? = null,
    val upcomingEvents: List<EventUiItem> = emptyList(),
    val allEvents: List<EventUiItem> = emptyList(),
    val members: List<ClubMemberUiItem> = emptyList(),
    val filteredMembers: List<ClubMemberUiItem> = emptyList(),
    val memberSearchQuery: String = "",
    val memberFilter: MemberFilter = MemberFilter.ALL,
    val isMembersLoading: Boolean = false,
    @StringRes val error: Int? = null,
    val calendarYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val calendarMonth: Int = Calendar.getInstance().get(Calendar.MONTH),
    val selectedEpochDay: Long = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis()),
    val eventDaysInView: Set<Long> = emptySet(),
    val navigateToAddEvent: Boolean = false,
)
