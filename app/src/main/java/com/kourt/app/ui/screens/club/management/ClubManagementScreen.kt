package com.kourt.app.ui.screens.club.management

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.compose.currentBackStackEntryAsState
import com.kourt.app.navigation.Destination
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kourt.app.data.model.Team
import com.kourt.app.navigation.INavigationRouter
import com.kourt.app.ui.components.BaseScreenWithBottomNav
import com.kourt.app.ui.components.bottomSheets.GrowYourTeamBottomSheet
import com.kourt.app.ui.components.bottomSheets.MemberActionBottomSheet
import com.kourt.app.ui.components.bottomSheets.TeamActionBottomSheet
import com.kourt.app.ui.components.TeamCard
import com.kourt.app.ui.Tabs.EventsTab
import com.kourt.app.ui.Tabs.MembersTab
import com.kourt.app.ui.Tabs.TeamsTab
import com.kourt.app.ui.components.bottomSheets.AssignTeamBottomSheet
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

    val navBackStackEntry by navigation.getNavController().currentBackStackEntryAsState()
    LaunchedEffect(navBackStackEntry) {
        if (navBackStackEntry?.destination?.route == Destination.ClubManagementScreen.route) {
            viewModel.refreshTeams()
        }
    }

    ClubManagementScreenContent(
        uiState = uiState,
        actions = viewModel,
        navigation = navigation
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClubManagementScreenContent(
    uiState: ClubManagementScreenUiState,
    actions: ClubManagementScreenActions,
    navigation: INavigationRouter

) {
    var selectedTab by rememberSaveable { mutableIntStateOf(TAB_TEAMS) }

    // Member Action bottom sheet
    val selectedMember = uiState.selectedMember
    if (selectedMember != null) {
        MemberActionBottomSheet(
            memberId = selectedMember.memberId,
            userId = selectedMember.userId,
            onDismiss = { actions.onMemberActionDismiss() },
            onViewProfile = { navigation.navigateToUserProfile(selectedMember.userId) },
            onAssignToTeam = { actions.onAssignToTeam(selectedMember.memberId) },
            onMakeAdmin = { actions.onMakeAdmin(selectedMember.memberId) },
            onRemoveMember = { actions.onRemoveMember(selectedMember.memberId) },
        )
    }

    // Assign to Team bottom sheet
    val memberForAssign = uiState.memberForAssignTeam
    if (memberForAssign != null) {
        AssignTeamBottomSheet(
            teams = uiState.teams,
            onTeamSelected = { teamId -> actions.onAssignTeamSelected(memberForAssign.memberId, teamId) },
            onDismiss = { actions.onAssignTeamDismiss() },
        )
    }

    // Make Admin confirmation dialog
    val memberForAdmin = uiState.memberForMakeAdmin
    if (memberForAdmin != null) {
        AlertDialog(
            onDismissRequest = { actions.onMakeAdminDismiss() },
            title = { Text(stringResource(com.kourt.app.R.string.make_admin_title)) },
            text = {
                Text(
                    stringResource(com.kourt.app.R.string.make_admin_message, memberForAdmin.displayName)
                )
            },
            confirmButton = {
                TextButton(onClick = { actions.onMakeAdminConfirm(memberForAdmin.memberId) }) {
                    Text(stringResource(com.kourt.app.R.string.make_admin_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { actions.onMakeAdminDismiss() }) {
                    Text(stringResource(com.kourt.app.R.string.action_cancel))
                }
            },
        )
    }

    // Remove Member confirmation dialog
    val memberForRemove = uiState.memberForRemove
    if (memberForRemove != null) {
        AlertDialog(
            onDismissRequest = { actions.onRemoveDismiss() },
            title = { Text(stringResource(com.kourt.app.R.string.remove_member_title)) },
            text = { Text(stringResource(com.kourt.app.R.string.remove_member_message)) },
            confirmButton = {
                TextButton(onClick = { actions.onRemoveConfirm(memberForRemove.memberId) }) {
                    Text(
                        text = stringResource(com.kourt.app.R.string.remove_member_confirm),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { actions.onRemoveDismiss() }) {
                    Text(stringResource(com.kourt.app.R.string.action_cancel))
                }
            },
        )
    }

    // Team Action bottom sheet
    val selectedTeam = uiState.selectedTeam
    if (selectedTeam != null) {
        TeamActionBottomSheet(
            teamName = selectedTeam.name,
            onDismiss = { actions.onTeamActionDismiss() },
            onEditTeam = { navigation.navigateToEditTeamScreen(selectedTeam.id) },
            onShareCode = { actions.onShareTeamCode() },
            onDeleteTeam = { actions.onDeleteTeamClick() },
        )
    }

    // Grow Your Team bottom sheet (share join code)
    val teamForCode = uiState.teamForCode
    if (uiState.showTeamCodeSheet && teamForCode != null) {
        GrowYourTeamBottomSheet(
            joinCode = teamForCode.joinCode,
            teamName = teamForCode.name,
            onDismiss = { actions.onTeamCodeSheetDismiss() },
        )
    }

    // Delete Team confirmation dialog
    val teamForDelete = uiState.teamForDelete
    if (teamForDelete != null) {
        AlertDialog(
            onDismissRequest = { actions.onDeleteTeamDismiss() },
            title = { Text(stringResource(com.kourt.app.R.string.team_delete_confirm_title)) },
            text = { Text(stringResource(com.kourt.app.R.string.team_delete_confirm_message)) },
            confirmButton = {
                TextButton(onClick = { actions.onDeleteTeamConfirm() }) {
                    Text(
                        text = stringResource(com.kourt.app.R.string.team_delete_confirm_button),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { actions.onDeleteTeamDismiss() }) {
                    Text(stringResource(com.kourt.app.R.string.action_cancel))
                }
            },
        )
    }

    BaseScreenWithBottomNav(
        topBar = {
            ClubManagementTopBar(
                title = uiState.clubName.ifBlank { stringResource(com.kourt.app.R.string.club_management_title) },
                onSettingsTap = { navigation.navigateToSettingsScreen() },
            )
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
                    onClick = { navigation.navigateToAddTeamScreen(uiState.clubId) },
                    shape = RoundedCornerShape(16.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(com.kourt.app.R.string.club_management_add_team_cd),
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
                    onTeamMenuClick = { actions.onTeamMenuClick(it) },
                )
                TAB_MEMBERS -> MembersTab(
                    uiState = uiState,
                    onSearchQueryChange = { actions.onMemberSearchQueryChange(it) },
                    onFilterChange = { actions.onMemberFilterChange(it) },
                    onMenuClick = { actions.onMemberMenuClick(it) },
                )
                TAB_EVENTS -> EventsTab(
                    uiState = uiState,
                    actions = actions,
                    onEditEvent = { navigation.navigateToEditEvent(uiState.selectedEvent?.eventId ?: "",
                        uiState.selectedTeam?.id ?: ""
                    ) },
                )
            }
        }
    }
}

// ── Top app bar ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClubManagementTopBar(
    title: String,
    onSettingsTap: () -> Unit,
) {
    val outlineColor = MaterialTheme.colorScheme.surface
    CenterAlignedTopAppBar(
        modifier = Modifier.drawWithContent {
            drawContent()
            val thickness = 1.dp.toPx()
            val y = size.height - (thickness / 2)
            drawLine(
                color = outlineColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = thickness,
            )
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            )
        },
        actions = {
            IconButton(onClick = onSettingsTap) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = stringResource(com.kourt.app.R.string.club_management_settings_cd),
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            actionIconContentColor = MaterialTheme.colorScheme.primary,
        ),
    )
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
        BottomNavItem(stringResource(com.kourt.app.R.string.club_management_tab_members), painterResource(com.kourt.app.R.drawable.person_search), TAB_MEMBERS),
        BottomNavItem(stringResource(com.kourt.app.R.string.club_management_tab_events), painterResource(com.kourt.app.R.drawable.event), TAB_EVENTS),
    )
    val outlineColor = MaterialTheme.colorScheme.surface


    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
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

