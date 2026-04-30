package com.kourt.app.ui.screens.settings.profile

interface ProfileScreenActions {
    fun onEditAvatarClick()
    fun onAvatarSelected(avatarId: String)
    fun onAvatarPickerDismiss()
    fun onMembershipClick(item: MembershipRowUiItem)
    fun onChildRowClick(child: ManagedChildUiItem)
    fun onSwitchToChild(childId: String)
    fun onChildActionSheetDismiss()
    fun onChildJoinTeamDialogShow()
    fun onChildJoinTeamDialogDismiss()
    fun onChildJoinCodeChange(code: String)
    fun onChildJoinTeam()
    fun onChildLeaveTeam(membershipId: String)
    fun onRemoveChild()
    fun onRemoveChildConfirm()
    fun onRemoveChildDismiss()
    fun onJoinOrCreateTeam()
    fun onNavigationConsumed()
}
