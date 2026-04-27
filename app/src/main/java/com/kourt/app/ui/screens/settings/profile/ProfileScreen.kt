package com.kourt.app.ui.screens.settings.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kourt.app.R
import com.kourt.app.navigation.INavigationRouter
import com.kourt.app.ui.components.AvatarPickerBottomSheet
import com.kourt.app.ui.components.BaseScreen
import com.kourt.app.ui.components.KourtTeamAvatar
import com.kourt.app.ui.components.avatarResId
import com.kourt.app.ui.components.roleBadgeColor
import com.kourt.app.ui.theme.KourtTheme
import com.kourt.app.ui.theme.RoleBadgeAdmin
import com.kourt.app.ui.theme.RoleBadgeAssistant
import com.kourt.app.ui.theme.RoleBadgeCaptain
import com.kourt.app.ui.theme.RoleBadgeCoach
import com.kourt.app.ui.theme.RoleBadgeParent
import com.kourt.app.ui.theme.RoleBadgePlayer
import com.kourt.app.viewmodel.settings.ProfileViewModel

@Composable
fun ProfileScreen(
    navigation: INavigationRouter,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState

    LaunchedEffect(uiState.navigateToClubManagement) {
        if (uiState.navigateToClubManagement) {
            navigation.navigateToClubManagementScreen()
            viewModel.onNavigationConsumed()
        }
    }

    LaunchedEffect(uiState.navigateToDashboard) {
        if (uiState.navigateToDashboard) {
            navigation.navigateToDashboard()
            viewModel.onNavigationConsumed()
        }
    }

    ProfileScreenContent(
        uiState = uiState,
        onBack = navigation::returnBack,
        onEditAvatarClick = viewModel::onEditAvatarClick,
        onAvatarSelected = viewModel::onAvatarSelected,
        onAvatarPickerDismiss = viewModel::onAvatarPickerDismiss,
        onMembershipClick = viewModel::onMembershipClick,
    )
}

@Composable
private fun ProfileScreenContent(
    uiState: ProfileScreenUiState,
    onBack: () -> Unit,
    onEditAvatarClick: () -> Unit,
    onAvatarSelected: (String) -> Unit = {},
    onAvatarPickerDismiss: () -> Unit = {},
    onMembershipClick: (MembershipRowUiItem) -> Unit = {},
) {
    if (uiState.showAvatarPicker) {
        AvatarPickerBottomSheet(
            currentAvatarId = uiState.avatarId,
            onAvatarSelected = onAvatarSelected,
            onDismiss = onAvatarPickerDismiss,
        )
    }

    BaseScreen(
        title = stringResource(R.string.profile_title),
        onBack = onBack,
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
                                onEditAvatarClick = onEditAvatarClick,
                            )
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
                                MembershipRow(item = item, onClick = { onMembershipClick(item) })
                            }
                        }
                    }
                }
            }
        }
    }
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

@Composable
private fun MembershipRow(
    item: MembershipRowUiItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val roleColor = roleBadgeColor(item.role)

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
            KourtTeamAvatar(
                initials = item.initials,
                accentColor = item.accentColor,
            )

            // Club name + subtitle
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

            // Role badge pill
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


