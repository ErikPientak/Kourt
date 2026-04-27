package com.kourt.app.ui.screens.settings.profile

interface ProfileScreenActions {
    fun onEditAvatarClick()
    fun onAvatarSelected(avatarId: String)
    fun onAvatarPickerDismiss()
    fun onMembershipClick(item: MembershipRowUiItem)
    fun onNavigationConsumed()
}
