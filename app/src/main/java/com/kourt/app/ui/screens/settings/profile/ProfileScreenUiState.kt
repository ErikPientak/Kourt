package com.kourt.app.ui.screens.settings.profile

import androidx.annotation.StringRes

data class ProfileScreenUiState(
    val displayName: String = "",
    val email: String = "",
    val avatarId: String = "",
    val memberships: List<MembershipRowUiItem> = emptyList(),
    val isLoading: Boolean = true,
    @StringRes val error: Int? = null,
    val navigateToClubManagement: Boolean = false,
    val navigateToDashboard: Boolean = false,
    val isReadOnly: Boolean = false,
    val showAvatarPicker: Boolean = false,
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
