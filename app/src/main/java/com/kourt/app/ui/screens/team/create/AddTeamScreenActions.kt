package com.kourt.app.ui.screens.team.create

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
    fun onLogoUploadTap()
    fun onSaveTeam()
    fun onDeleteTeam()
    fun onDeleteTeamClick()
    fun onDeleteTeamDismiss()
    fun onDeleteTeamConfirm()
    fun onSuccessConsumed()
}
