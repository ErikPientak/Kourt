package com.kourt.app.ui.screens.event.matchstats

interface MatchStatsScreenActions {
    fun onOpponentScoreChanged(delta: Int)
    fun onStatChanged(teamMemberId: String, stat: StatField, delta: Int)
    fun onSave()
    fun onSavedConsumed()
}
