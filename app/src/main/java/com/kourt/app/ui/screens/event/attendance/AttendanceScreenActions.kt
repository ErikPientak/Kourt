package com.kourt.app.ui.screens.event.attendance

interface AttendanceScreenActions {
    fun onStatusSelected(userId: String, status: String)  // toggles: if already selected, clears to null
    fun onFilterChanged(filter: AttendanceFilter)
    fun onSave()
}
