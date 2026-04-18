package com.kourt.app.ui.screens.settings.profile

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kourt.app.R
import com.kourt.app.navigation.INavigationRouter
import com.kourt.app.ui.components.BaseScreen
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
        onMembershipClick = viewModel::onMembershipClick,
    )
}

@Composable
private fun ProfileScreenContent(
    uiState: ProfileScreenUiState,
    onBack: () -> Unit,
    onEditAvatarClick: () -> Unit,
    onMembershipClick: (MembershipRowUiItem) -> Unit = {},
) {
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
    onEditAvatarClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Circular avatar with edit badge
        Box(
            modifier = Modifier.size(96.dp),
        ) {
            // Avatar circle
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.size(48.dp),
                )
            }

            // Edit pencil badge at bottom-right
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
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(R.string.profile_edit_avatar_cd),
                    tint = Color.White,
                    modifier = Modifier.size(14.dp),
                )
            }
        }

        // Display name
        if (displayName.isNotEmpty()) {
            Text(
                text = displayName,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        // Email
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
            // Initials avatar — background = role badge color
            InitialsAvatar(
                text = item.clubName,
                backgroundColor = roleColor,
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
private fun InitialsAvatar(
    text: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
) {
    val initials = text
        .trim()
        .split(" ")
        .filter { it.isNotEmpty() }
        .take(2)
        .joinToString("") { it.first().uppercaseChar().toString() }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(backgroundColor.copy(alpha = 0.20f)),
    ) {
        Text(
            text = initials.ifEmpty { "?" },
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = backgroundColor,
        )
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


