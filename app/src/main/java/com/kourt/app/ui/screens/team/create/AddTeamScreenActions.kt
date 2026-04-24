package com.kourt.app.ui.screens.team.create

import androidx.compose.ui.graphics.Color
import com.kourt.app.data.model.User

interface AddTeamScreenActions {
    fun onTeamNameChange(value: String)
    fun onHeadCoachQueryChange(value: String)
    fun onHeadCoachSelected(user: User)
    fun onAssistantQueryChange(value: String)
    fun onAssistantAdd()
    fun onAssistantSelected(user: User)
    fun onAssistantRemoved(user: User)
    fun onCategoryChange(value: String)
    fun onToggleLocation()
    fun onLocationChange(value: String)
    fun onArenaChange(value: String)
    fun onAccentColorChanged(color: Color)
    fun onInitialsChanged(value: String)
    fun onSaveTeam()
    fun onDeleteTeam()
    fun onDeleteTeamClick()
    fun onDeleteTeamDismiss()
    fun onDeleteTeamConfirm()
    fun onSaveConsumed()
}
