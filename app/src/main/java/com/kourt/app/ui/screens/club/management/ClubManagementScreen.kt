package com.kourt.app.ui.screens.club.management

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kourt.app.navigation.INavigationRouter
import com.kourt.app.ui.components.BaseScreenWithBottomNav
import com.kourt.app.ui.components.MemberActionBottomSheet
import com.kourt.app.ui.screens.club.management.Tabs.MembersTab
import com.kourt.app.ui.screens.club.management.Tabs.TeamsTab
import com.kourt.app.viewmodel.club.ClubManagementViewModel

private const val TAB_TEAMS = 0
private const val TAB_MEMBERS = 1
private const val TAB_EVENTS = 2

@Composable
fun ClubManagementScreen(
    navigation: INavigationRouter,
    viewModel: ClubManagementViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState

    ClubManagementScreenContent(
        uiState = uiState,
        onAddTeam = {
            navigation.navigateToAddTeamScreen(uiState.clubId)
            viewModel.onAddTeam()
        },
        onTeamClick = { teamId -> navigation.navigateToEditTeamScreen(teamId) },
        onSettingsTap = { navigation.navigateToSettingsScreen() },
        onMemberSearchQueryChange = viewModel::onMemberSearchQueryChange,
        onMemberFilterChange = viewModel::onMemberFilterChange,
        onMemberMenuClick = viewModel::onMemberMenuClick,
        onAddMember = viewModel::onAddMember,
        onMemberActionDismiss = viewModel::onMemberActionDismiss,
        onEditMember = viewModel::onEditMember,
        onChangeRole = viewModel::onChangeRole,
        onRemoveFromClub = viewModel::onRemoveFromClub,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClubManagementScreenContent(
    uiState: ClubManagementScreenUiState,
    onAddTeam: () -> Unit,
    onTeamClick: (String) -> Unit,
    onSettingsTap: () -> Unit,
    onMemberSearchQueryChange: (String) -> Unit,
    onMemberFilterChange: (MemberFilter) -> Unit,
    onMemberMenuClick: (String) -> Unit,
    onAddMember: () -> Unit,
    onMemberActionDismiss: () -> Unit,
    onEditMember: (String) -> Unit,
    onChangeRole: (String) -> Unit,
    onRemoveFromClub: (String) -> Unit,
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(TAB_TEAMS) }

    // Member Action bottom sheet — driven entirely by uiState.selectedMemberId
    val selectedMemberId = uiState.selectedMemberId
    if (selectedMemberId != null) {
        MemberActionBottomSheet(
            memberId = selectedMemberId,
            onDismiss = onMemberActionDismiss,
            onEditMember = onEditMember,
            onChangeRole = onChangeRole,
            onRemoveFromClub = onRemoveFromClub,
        )
    }

    BaseScreenWithBottomNav(
        title = uiState.clubName.ifBlank { stringResource(com.kourt.app.R.string.club_management_title) },
        trailingIcon = {
            IconButton(onClick = onSettingsTap) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(com.kourt.app.R.string.club_management_settings_cd),
                )
            }
        },
        bottomBar = {
            ClubManagementBottomNav(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
            )
        },
        floatingActionButton = {
            when (selectedTab) {
                TAB_TEAMS -> FloatingActionButton(
                    onClick = onAddTeam,
                    shape = RoundedCornerShape(16.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(com.kourt.app.R.string.club_management_add_team_cd),
                    )
                }
                TAB_MEMBERS -> FloatingActionButton(
                    onClick = onAddMember,
                    shape = RoundedCornerShape(16.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(com.kourt.app.R.string.members_add_member_cd),
                    )
                }
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (selectedTab) {
                TAB_TEAMS -> TeamsTab(
                    uiState = uiState,
                    onTeamClick = onTeamClick,
                )
                TAB_MEMBERS -> MembersTab(
                    uiState = uiState,
                    onSearchQueryChange = onMemberSearchQueryChange,
                    onFilterChange = onMemberFilterChange,
                    onMenuClick = onMemberMenuClick,
                )
                TAB_EVENTS -> ComingSoonTab()
            }
        }
    }
}

// ── Bottom navigation ─────────────────────────────────────────────────────────

private data class BottomNavItem(
    val label: String,
    val icon: Painter,
    val tab: Int,
)

@Composable
private fun ClubManagementBottomNav(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
) {
    val items = listOf(
        BottomNavItem(stringResource(com.kourt.app.R.string.club_management_tab_teams), painterResource(com.kourt.app.R.drawable.group), TAB_TEAMS),
        BottomNavItem(stringResource(com.kourt.app.R.string.club_management_tab_members), painterResource(com.kourt.app.R.drawable.person_edit), TAB_MEMBERS),
        BottomNavItem(stringResource(com.kourt.app.R.string.club_management_tab_events), painterResource(com.kourt.app.R.drawable.event), TAB_EVENTS),
    )
    val outlineColor = MaterialTheme.colorScheme.surface


    Surface(
        modifier = Modifier.fillMaxWidth()
            .drawWithContent {
                drawContent()
                val thickness = 1.dp.toPx()
                val y = thickness / 2
                drawLine(
                    color = outlineColor,
                    start = Offset(x = 0f, y = y),
                    end = Offset(x = size.width, y = y),
                    strokeWidth = thickness
                )
            },
        color = MaterialTheme.colorScheme.background,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEach { item ->
                val isSelected = selectedTab == item.tab
                val tint = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onTabSelected(item.tab) }
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        painter = item.icon,
                        contentDescription = item.label,
                        tint = tint,
                        modifier = Modifier.size(24.dp),
                    )
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = tint,
                    )
                }
            }
        }
    }
}

@Composable
private fun ComingSoonTab() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(com.kourt.app.R.string.coming_soon),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        )
    }
}