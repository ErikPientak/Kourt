package com.kourt.app.ui.screens.settings.profile

interface ProfileScreenActions {
    fun onEditAvatarClick()
    fun onMembershipClick(item: MembershipRowUiItem)
    fun onNavigationConsumed()
}
