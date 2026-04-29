package com.kourt.app.ui.screens.setup.addchild

import androidx.annotation.StringRes

data class AddChildScreenUiState(
    val displayName: String = "",
    val avatarId: String = "",
    val childUid: String = "",
    val joinCode: String = "",
    val isLoading: Boolean = false,
    val showAvatarPicker: Boolean = false,
    val showJoinTeamDialog: Boolean = false,
    @StringRes val profileError: Int? = null,
    @StringRes val joinError: Int? = null,
    val isSuccess: Boolean = false,
)
