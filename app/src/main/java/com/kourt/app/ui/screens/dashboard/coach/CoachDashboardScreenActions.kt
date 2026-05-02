package com.kourt.app.ui.screens.dashboard.coach

import com.kourt.app.ui.screens.club.management.MemberFilter

interface CoachDashboardScreenActions {
    fun onTeamDropdownToggle()
    fun onTeamSelected(teamId: String)
    fun onUpdateLineup(eventId: String)
    // Roster Tab
    fun onRosterSearchQueryChange(query: String)
    fun onRosterFilterChange(filter: MemberFilter)
    fun onRosterMenuClick(memberId: String)
    fun onRosterMenuDismiss()
    fun onRemoveMemberConfirmShow()
    fun onRemoveMemberConfirmDismiss()
    fun onChangeRoleClicked()
    fun onRoleMenuDismiss()
    fun onRoleSelected(role: String)
    fun onChangeRole(memberId: String, newRole: String)
    fun onRemoveMember(memberId: String)
    fun onChangeJerseyNumberClick(memberId: String)
    fun onChangeJerseyNumber(memberId: String, newJerseyNumber: Int)
    // Schedule tab
    fun onPreviousMonth()
    fun onNextMonth()
    fun onDateSelected(epochDay: Long)
    fun onLogAttendance(eventId: String)
    fun onLogStatistics(eventId: String)
    fun onAddEvent()
    fun onCardClicked(eventId: String)
    fun onGrowTeamClicked()
    fun onGrowTeamDismissed()
}
