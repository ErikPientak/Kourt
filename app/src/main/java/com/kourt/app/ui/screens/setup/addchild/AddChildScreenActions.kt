package com.kourt.app.ui.screens.setup.addchild

interface AddChildScreenActions {
    fun onDisplayNameChange(name: String)
    fun onAvatarPickerShow()
    fun onAvatarSelected(avatarId: String)
    fun onAvatarPickerDismiss()
    fun onSubmit()
    fun onJoinCodeChange(code: String)
    fun onJoinTeam()
    fun onDismissJoinTeamDialog()
    fun onSuccessConsumed()
}
