package com.kourt.app.ui.screens.settings.profile

import androidx.annotation.StringRes

data class ProfileScreenUiState(
    val displayName: String = "",
    val email: String = "",
    val avatarId: String = "",
    val memberships: List<MembershipRowUiItem> = emptyList(),
    val managedChildren: List<ManagedChildUiItem> = emptyList(),
    val activeChildId: String = "",
    val selectedChild: ManagedChildUiItem? = null,
    val showChildActionSheet: Boolean = false,
    val showJoinTeamForChildDialog: Boolean = false,
    val showRemoveChildConfirm: Boolean = false,
    val childToRemove: ManagedChildUiItem? = null,
    val joinCodeForChild: String = "",
    @StringRes val joinCodeForChildError: Int? = null,
    val isLoading: Boolean = true,
    @StringRes val error: Int? = null,
    val navigateToClubManagement: Boolean = false,
    val navigateToPlayerDashboard: Boolean = false,
    val navigateToCoachDashboard: Boolean = false,
    val navigateToSetup: Boolean = false,
    val isReadOnly: Boolean = false,
    val showAvatarPicker: Boolean = false,
)

data class ChildTeamUiItem(
    val membershipId: String,
    val teamId: String,
    val teamName: String,
)

data class ManagedChildUiItem(
    val userId: String,
    val displayName: String,
    val avatarId: String,
    val teams: List<ChildTeamUiItem>,
    val isActive: Boolean,
)

data class MembershipRowUiItem(
    val clubId: String,
    val teamId: String?,
    val clubName: String,
    val subtitle: String,
    val role: String,
    val accentColor: String = "",
    val initials: String = "",
)
