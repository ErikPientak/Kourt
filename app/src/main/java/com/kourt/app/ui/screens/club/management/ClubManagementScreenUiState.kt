package com.kourt.app.ui.screens.club.management

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.kourt.app.data.model.Team
import com.kourt.app.ui.screens.dashboard.coach.EventUiItem

// ── Members tab — filter options ──────────────────────────────────────────────

enum class MemberFilter { ALL, COACH, PLAYER, PARENT, ASSISTANT }

// ── Events tab — filter options ───────────────────────────────────────────────

enum class EventStatusFilter { ALL, UPCOMING, PAST, CANCELLED }

// ── Members tab — UI projection ───────────────────────────────────────────────

@Immutable
data class ClubMemberUiItem(
    /** Firestore document ID of the TeamMember record — used as the list key. */
    val memberId: String,
    val userId: String,
    val displayName: String,
    /** Raw lowercase role string ("player", "coach", "parent", "admin", …). */
    val role: String,
    /** Pre-formatted subtitle line shown below the name on the card. */
    val subtitle: String,
    val avatarUrl: String = "",
    val jerseyNumber: Int = 0,
)

// ── Screen UI state ───────────────────────────────────────────────────────────

@Stable
data class ClubManagementScreenUiState(
    // ── Shared / club ────────────────────────────────────────────────────────
    val clubId: String = "",
    val clubName: String = "",
    val isLoading: Boolean = false,
    @StringRes val error: Int? = null,

    // ── Teams tab ────────────────────────────────────────────────────────────
    val teams: List<Team> = emptyList(),
    val selectedTeam: Team? = null,
    val teamForDelete: Team? = null,
    val teamForCode: Team? = null,
    val showTeamCodeSheet: Boolean = false,

    // ── Members tab ──────────────────────────────────────────────────────────
    /** Full unfiltered list, loaded once from Firestore. */
    val members: List<ClubMemberUiItem> = emptyList(),
    /** Subset of [members] after applying [memberSearchQuery] and [memberFilter]. */
    val filteredMembers: List<ClubMemberUiItem> = emptyList(),
    val memberSearchQuery: String = "",
    val memberFilter: MemberFilter = MemberFilter.ALL,
    val isMembersLoading: Boolean = false,
    @StringRes val membersError: Int? = null,

    // ── Member Action bottom sheet ────────────────────────────────────────────
    /** The member whose action sheet is open. `null` means the sheet is dismissed. */
    val selectedMember: ClubMemberUiItem? = null,

    // ── Action dialogs ────────────────────────────────────────────────────────
    val memberForAssignTeam: ClubMemberUiItem? = null,
    val memberForMakeAdmin: ClubMemberUiItem? = null,
    val memberForRemove: ClubMemberUiItem? = null,

    // ── Events tab ────────────────────────────────────────────────────────────
    val events: List<EventUiItem> = emptyList(),
    val filteredEvents: List<EventUiItem> = emptyList(),
    val eventStatusFilter: EventStatusFilter = EventStatusFilter.ALL,
    val isEventsLoading: Boolean = false,
    val selectedEvent: EventUiItem? = null,
    val eventForCancel: EventUiItem? = null,
    val eventForDelete: EventUiItem? = null,
)
