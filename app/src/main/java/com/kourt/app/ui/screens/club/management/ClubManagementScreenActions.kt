package com.kourt.app.ui.screens.club.management

interface ClubManagementScreenActions {
    // ── Teams tab ────────────────────────────────────────────────────────────
    fun onAddTeam()
    fun onTeamClick(teamId: String)
    fun onSettingsTap()

    // ── Members tab ──────────────────────────────────────────────────────────
    fun onMemberSearchQueryChange(query: String)
    fun onMemberFilterChange(filter: MemberFilter)
    /** Stub — three-dot menu tapped for the given TeamMember document ID. */
    fun onMemberMenuClick(memberId: String)
    fun onAddMember()
}
