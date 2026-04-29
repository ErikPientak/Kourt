package com.kourt.app.ui.screens.event.detail

interface EventDetailScreenActions {
    fun onUpdateRsvp()
    fun onRsvpSelected(status: String)
    fun onRsvpReasonSelected(reason: String)
    fun onRsvpNoteChanged(note: String)
    fun onSubmitRsvp()
    fun onDismissRsvpSheet()
    fun onDismissReasonSheet()
    fun onToggleShowAllGoing()
    fun onToggleShowAllNotGoing()
}
