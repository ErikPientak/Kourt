package com.kourt.app.ui.screens.club.management

interface ClubManagementScreenActions {
    // ── Teams tab ────────────────────────────────────────────────────────────
    fun onAddTeam()
    fun onTeamClick(teamId: String)
    fun onSettingsTap()

    // ── Members tab ──────────────────────────────────────────────────────────
    fun onMemberSearchQueryChange(query: String)
    fun onMemberFilterChange(filter: MemberFilter)
    /** Three-dot menu tapped — opens the action sheet for [memberId]. */
    fun onMemberMenuClick(memberId: String)
    fun onAddMember()

    // ── Member Action bottom sheet ────────────────────────────────────────────
    /** Sheet dismissed (drag-down or action tap). */
    fun onMemberActionDismiss()
    /** "Edit Member" row tapped. */
    fun onEditMember(memberId: String)
    /** "Change Role" row tapped. */
    fun onChangeRole(memberId: String)
    /** "Remove from Club" row tapped. */
    fun onRemoveFromClub(memberId: String)
}
