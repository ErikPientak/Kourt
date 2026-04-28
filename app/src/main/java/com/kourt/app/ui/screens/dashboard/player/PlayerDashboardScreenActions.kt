package com.kourt.app.ui.screens.dashboard.player

import com.kourt.app.ui.screens.club.management.MemberFilter

interface PlayerDashboardScreenActions {
    fun onTeamDropdownToggle()
    fun onTeamSelected(teamId: String)
    fun onRosterSearchQueryChange(query: String)
    fun onRosterFilterChange(filter: MemberFilter)

    // Schedule tab
    fun onPreviousMonth()
    fun onNextMonth()
    fun onDateSelected(epochDay: Long)
    fun onCardClicked(eventId: String)

    // Player RSVP
    fun onUpdateAttendance()
    fun onUpdateAttendanceForEvent(eventId: String)
    fun onRsvpSelected(status: String)
    fun onDismissRsvpSheet()
    fun onReasonSelected(reason: String)
    fun onReasonNoteChanged(note: String)
    fun onSubmitReason()
    fun onDismissReasonSheet()
}
