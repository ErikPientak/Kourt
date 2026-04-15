package com.kourt.app.ui.screens.club.management

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.kourt.app.data.model.Team

// ── Members tab — filter options ──────────────────────────────────────────────

enum class MemberFilter { ALL, COACH, PLAYER, PARENT }

// ── Members tab — UI projection ───────────────────────────────────────────────

/**
 * A flat, UI-ready projection of a club member.
 *
 * Assembled in the ViewModel by joining [TeamMember] + [User] + [Team] data.
 * [subtitle] is pre-formatted ("Team: X" for coaches/players, "Athlete: X"
 * for parents) so the composable layer stays logic-free.
 */
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
)

// ── Screen UI state ───────────────────────────────────────────────────────────

@Stable
data class ClubManagementScreenUiState(
    // ── Shared / club ────────────────────────────────────────────────────────
    val clubId: String = "",
    val clubName: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,

    // ── Teams tab ────────────────────────────────────────────────────────────
    val teams: List<Team> = emptyList(),

    // ── Members tab ──────────────────────────────────────────────────────────
    /** Full unfiltered list, loaded once from Firestore. */
    val members: List<ClubMemberUiItem> = emptyList(),
    /** Subset of [members] after applying [memberSearchQuery] and [memberFilter]. */
    val filteredMembers: List<ClubMemberUiItem> = emptyList(),
    val memberSearchQuery: String = "",
    val memberFilter: MemberFilter = MemberFilter.ALL,
    val isMembersLoading: Boolean = false,
    val membersError: String? = null,

    // ── Member Action bottom sheet ────────────────────────────────────────────
    /**
     * The TeamMember document ID of the member whose action sheet is open.
     * `null` means the sheet is dismissed.
     */
    val selectedMemberId: String? = null,
)
