package com.kourt.app.ui.screens.club.management

interface ClubManagementScreenActions {
    // ── Teams tab ────────────────────────────────────────────────────────────
    fun onAddTeam()
    fun onTeamClick(teamId: String)
    fun onSettingsTap()

    // ── Members tab ──────────────────────────────────────────────────────────
    fun onMemberSearchQueryChange(query: String)
    fun onMemberFilterChange(filter: MemberFilter)
    fun onMemberMenuClick(memberId: String)
    fun onAddMember()

    // ── Member Action bottom sheet ────────────────────────────────────────────
    fun onMemberActionDismiss()

    // ── Assign to Team dialog ─────────────────────────────────────────────────
    fun onAssignToTeam(memberId: String)
    fun onAssignTeamSelected(memberId: String, teamId: String)
    fun onAssignTeamDismiss()

    // ── Make Admin dialog ─────────────────────────────────────────────────────
    fun onMakeAdmin(memberId: String)
    fun onMakeAdminConfirm(memberId: String)
    fun onMakeAdminDismiss()

    // ── Remove Member dialog ──────────────────────────────────────────────────
    fun onRemoveMember(memberId: String)
    fun onRemoveConfirm(memberId: String)
    fun onRemoveDismiss()
}
