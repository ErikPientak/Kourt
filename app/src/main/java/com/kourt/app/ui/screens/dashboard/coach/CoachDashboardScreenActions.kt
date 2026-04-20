package com.kourt.app.ui.screens.dashboard.coach

import com.kourt.app.ui.screens.club.management.MemberFilter

interface CoachDashboardScreenActions {
    fun onTeamDropdownToggle()
    fun onTeamSelected(teamId: String)
    fun onUpdateLineup()
    fun onRosterSearchQueryChange(query: String)
    fun onRosterFilterChange(filter: MemberFilter)
}
