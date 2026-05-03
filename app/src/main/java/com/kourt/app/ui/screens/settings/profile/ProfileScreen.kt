package com.kourt.app.ui.screens.settings.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kourt.app.R
import com.kourt.app.navigation.INavigationRouter
import com.kourt.app.ui.components.BaseScreen
import com.kourt.app.ui.components.KourtAvatarLeading
import com.kourt.app.ui.components.KourtTeamAvatar
import com.kourt.app.ui.components.bottomSheets.AvatarPickerBottomSheet
import com.kourt.app.ui.components.bottomSheets.ChildActionBottomSheet
import com.kourt.app.ui.components.bottomSheets.avatarResId
import com.kourt.app.ui.components.roleBadgeColor
import com.kourt.app.ui.theme.White
import com.kourt.app.viewmodel.settings.ProfileViewModel

@Composable
fun ProfileScreen(
    navigation: INavigationRouter,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState

    LaunchedEffect(uiState.navigateToSetup) {
        if (uiState.navigateToSetup) {
            navigation.navigateToSetupScreen()
            viewModel.onNavigationConsumed()
        }
    }

    LaunchedEffect(uiState.navigateToClubManagement) {
        if (uiState.navigateToClubManagement) {
            navigation.navigateToClubManagementScreen()
            viewModel.onNavigationConsumed()
        }
    }

    LaunchedEffect(uiState.navigateToPlayerDashboard) {
        if (uiState.navigateToPlayerDashboard) {
            navigation.navigateToPlayerDashboard()
            viewModel.onNavigationConsumed()
        }
    }

    LaunchedEffect(uiState.navigateToCoachDashboard) {
        if (uiState.navigateToCoachDashboard) {
            navigation.navigateToDashboard()
            viewModel.onNavigationConsumed()
        }
    }

    ProfileScreenContent(
        uiState = uiState,
        actions = viewModel,
        navigation = navigation
    )
}

@Composable
private fun ProfileScreenContent(
    uiState: ProfileScreenUiState,
    actions: ProfileScreenActions,
    navigation: INavigationRouter,
) {
    if (uiState.showAvatarPicker) {
        AvatarPickerBottomSheet(
            currentAvatarId = uiState.avatarId,
            onAvatarSelected = actions::onAvatarSelected,
            onDismiss = actions::onAvatarPickerDismiss,
        )
    }

    if (uiState.showChildActionSheet && uiState.selectedChild != null) {
        ChildActionBottomSheet(
            child = uiState.selectedChild,
            onSwitchToChild = { actions.onSwitchToChild(uiState.selectedChild.userId) },
            onJoinTeam = actions::onChildJoinTeamDialogShow,
            onLeaveTeam = actions::onChildLeaveTeam,
            onRemoveChild = actions::onRemoveChild,
            onDismiss = actions::onChildActionSheetDismiss,
        )
    }

    if (uiState.showRemoveChildConfirm && uiState.childToRemove != null) {
        AlertDialog(
            onDismissRequest = actions::onRemoveChildDismiss,
            title = {
                Text(
                    text = stringResource(R.string.child_remove_confirm_title, uiState.childToRemove.displayName),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.child_remove_confirm_body, uiState.childToRemove.displayName),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
            },
            confirmButton = {
                TextButton(onClick = actions::onRemoveChildConfirm) {
                    Text(
                        text = stringResource(R.string.child_remove_confirm_button),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = actions::onRemoveChildDismiss) {
                    Text(stringResource(android.R.string.cancel))
                }
            },
            shape = RoundedCornerShape(24.dp),
        )
    }

    if (uiState.showLeaveTeamConfirm && uiState.membershipToLeave != null) {
        AlertDialog(
            onDismissRequest = actions::onLeaveTeamDismiss,
            title = {
                Text(
                    text = stringResource(R.string.leave_team_confirm_title, uiState.membershipToLeave.subtitle),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.leave_team_confirm_body, uiState.membershipToLeave.subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
            },
            confirmButton = {
                TextButton(onClick = actions::onLeaveTeamConfirm) {
                    Text(
                        text = stringResource(R.string.leave_team_confirm_button),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = actions::onLeaveTeamDismiss) {
                    Text(stringResource(android.R.string.cancel))
                }
            },
            shape = RoundedCornerShape(24.dp),
        )
    }

    if (uiState.showJoinTeamForChildDialog && uiState.selectedChild != null) {
        JoinTeamForChildDialog(
            childName = uiState.selectedChild.displayName,
            joinCode = uiState.joinCodeForChild,
            joinError = uiState.joinCodeForChildError,
            isLoading = uiState.isLoading,
            onJoinCodeChange = actions::onChildJoinCodeChange,
            onConfirm = actions::onChildJoinTeam,
            onDismiss = actions::onChildJoinTeamDialogDismiss,
        )
    }

    BaseScreen(
        title = stringResource(R.string.profile_title),
        onBack = { navigation.returnBack() },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                uiState.error != null -> {
                    Text(
                        text = stringResource(uiState.error),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 24.dp),
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        item {
                            ProfileHeader(
                                displayName = uiState.displayName,
                                email = uiState.email,
                                avatarId = uiState.avatarId,
                                isReadOnly = uiState.isReadOnly,
                                onEditAvatarClick = actions::onEditAvatarClick,
                            )
                        }

                        if (uiState.managedChildren.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(R.string.profile_my_children),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onBackground,
                                )
                            }
                            items(uiState.managedChildren) { child ->
                                ChildRow(item = child, onClick = { actions.onChildRowClick(child) })
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.profile_my_teams),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                        }

                        if (uiState.memberships.isEmpty()) {
                            item {
                                Text(
                                    text = stringResource(R.string.club_management_no_teams),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    modifier = Modifier.padding(top = 8.dp),
                                )
                            }
                        } else {
                            items(uiState.memberships) { item ->
                                MembershipRow(
                                    item = item,
                                    onClick = { actions.onMembershipClick(item) },
                                    onLongClick = if (!uiState.isReadOnly && item.teamId != null) {
                                        { actions.onLeaveTeamLongPress(item) }
                                    } else null,
                                )
                            }
                        }

                        item {
                            TextButton(
                                onClick = actions::onJoinOrCreateTeam,
                                modifier = Modifier.padding(top = 4.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.profile_join_or_create),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Join team for child dialog ─────────────────────────────────────────────────

@Composable
private fun JoinTeamForChildDialog(
    childName: String,
    joinCode: String,
    joinError: Int?,
    isLoading: Boolean,
    onJoinCodeChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.add_child_step2_title),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.child_join_team_dialog_subtitle, childName),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
                OutlinedTextField(
                    value = joinCode,
                    onValueChange = onJoinCodeChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = White,
                        unfocusedContainerColor = White,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedBorderColor = if (joinError != null) MaterialTheme.colorScheme.error else Color.Transparent,
                        unfocusedBorderColor = if (joinError != null) MaterialTheme.colorScheme.error else Color.Transparent,
                    ),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.setup_join_code_placeholder),
                            style = MaterialTheme.typography.labelLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                            color = Color.Black.copy(alpha = 0.4f),
                        )
                    },
                    textStyle = MaterialTheme.typography.labelLarge.copy(
                        textAlign = TextAlign.Center,
                        color = Color.Black,
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        capitalization = KeyboardCapitalization.Characters,
                        imeAction = ImeAction.Done,
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(50.dp),
                    enabled = !isLoading,
                )
                if (joinError != null) {
                    Text(
                        text = stringResource(joinError),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                )
            } else {
                TextButton(onClick = onConfirm) {
                    Text(stringResource(R.string.setup_join_button))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text(
                    text = stringResource(android.R.string.cancel),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
            }
        },
        shape = RoundedCornerShape(24.dp),
    )
}

// ── Profile header ─────────────────────────────────────────────────────────────

@Composable
private fun ProfileHeader(
    displayName: String,
    email: String,
    avatarId: String,
    isReadOnly: Boolean,
    onEditAvatarClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(modifier = Modifier.size(96.dp)) {
            val resId = avatarResId(avatarId)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
            ) {
                if (resId != null) {
                    Image(
                        painter = painterResource(resId),
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.person_search),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.size(48.dp),
                    )
                }
            }

            if (!isReadOnly) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(28.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable(onClick = onEditAvatarClick),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.person_edit),
                        contentDescription = stringResource(R.string.profile_edit_avatar_cd),
                        tint = Color.White,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        }

        if (displayName.isNotEmpty()) {
            Text(
                text = displayName,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        if (email.isNotEmpty()) {
            Text(
                text = email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
            )
        }
    }
}

// ── Membership row ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MembershipRow(
    item: MembershipRowUiItem,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val roleColor = roleBadgeColor(item.role)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = RoundedCornerShape(52.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            KourtTeamAvatar(
                initials = item.initials,
                accentColor = item.accentColor,
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = item.clubName,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                )
            }

            RoleBadgePill(role = item.role, color = roleColor)
        }
    }
}

@Composable
private fun RoleBadgePill(
    role: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(50.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = role.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = color,
        )
    }
}

// ── Child row ──────────────────────────────────────────────────────────────────

@Composable
private fun ChildRow(
    item: ManagedChildUiItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(52.dp),
        color = MaterialTheme.colorScheme.surface,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            KourtAvatarLeading(
                fallbackText = item.displayName,
                size = 44.dp,
                photoUrl = item.avatarId,
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = item.displayName,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (item.teams.isNotEmpty()) {
                    Text(
                        text = item.teams.joinToString(" · ") { it.teamName },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    )
                }
            }

            if (item.isActive) {
                Icon(
                    painter = painterResource(R.drawable.check),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}
