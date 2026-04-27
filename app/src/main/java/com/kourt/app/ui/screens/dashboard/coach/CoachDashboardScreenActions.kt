package com.kourt.app.ui.screens.dashboard.coach

import com.kourt.app.ui.screens.club.management.MemberFilter

interface CoachDashboardScreenActions {
    fun onTeamDropdownToggle()
    fun onTeamSelected(teamId: String)
    fun onUpdateLineup()
    fun onRosterSearchQueryChange(query: String)
    fun onRosterFilterChange(filter: MemberFilter)

    // Schedule tab
    fun onPreviousMonth()
    fun onNextMonth()
    fun onDateSelected(epochDay: Long)
    fun onLogAttendance(eventId: String)
    fun onLogStatistics(eventId: String)
    fun onAddEvent()
    fun onCardClicked(eventId: String)
}
