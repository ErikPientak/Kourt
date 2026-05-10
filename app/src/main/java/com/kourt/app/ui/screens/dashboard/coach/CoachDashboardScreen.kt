package com.kourt.app.ui.screens.dashboard.coach

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import com.kourt.app.ui.components.BaseScreenWithBottomNav
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.navigation.compose.currentBackStackEntryAsState
import com.kourt.app.navigation.Destination
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kourt.app.R
import com.kourt.app.navigation.INavigationRouter
import com.kourt.app.ui.components.EventCard
import com.kourt.app.ui.components.KourtAvatarLeading
import com.kourt.app.ui.components.KourtTeamAvatar
import com.kourt.app.ui.screens.club.management.ClubManagementScreenUiState
import com.kourt.app.ui.screens.club.management.MemberFilter
import com.kourt.app.ui.Tabs.MembersTab
import com.kourt.app.ui.Tabs.ScheduleTab
import com.kourt.app.ui.components.UpNextCard
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.input.KeyboardType
import com.kourt.app.ui.Tabs.CoachHomeTab
import com.kourt.app.ui.components.bottomSheets.ChangeRoleBottomSheet
import com.kourt.app.ui.components.bottomSheets.GrowYourTeamBottomSheet
import com.kourt.app.ui.components.bottomSheets.RosterActionBottomSheet
import com.kourt.app.viewmodel.dashboard.CoachDashboardViewModel

private const val TAB_HOME = 0
private const val TAB_SCHEDULE = 1
private const val TAB_ROSTER = 2

@Composable
fun CoachDashboardScreen(
    navigation: INavigationRouter,
    viewModel: CoachDashboardViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState

    val navBackStackEntry by navigation.getNavController().currentBackStackEntryAsState()
    LaunchedEffect(navBackStackEntry) {
        if (navBackStackEntry?.destination?.route == Destination.CoachDashboardScreen.route) {
            viewModel.refreshEvents()
        }
    }

    LaunchedEffect(uiState.navigateToAddEvent) {
        if (uiState.navigateToAddEvent) {
            val teamId = uiState.activeTeam?.teamId ?: return@LaunchedEffect
            navigation.navigateToAddEvent(teamId)
            viewModel.onAddEventNavigated()
        }
    }

    LaunchedEffect(uiState.navigateToEventDetailId) {
        val id = uiState.navigateToEventDetailId ?: return@LaunchedEffect
        navigation.navigateToEventDetail(id)
        viewModel.onEventDetailNavigated()
    }

    LaunchedEffect(uiState.navigateToAttendanceEventId) {
        val id = uiState.navigateToAttendanceEventId ?: return@LaunchedEffect
        navigation.navigateToAttendance(id)
        viewModel.onAttendanceNavigated()
    }

    LaunchedEffect(uiState.navigateToMatchStatsEventId) {
        val id = uiState.navigateToMatchStatsEventId ?: return@LaunchedEffect
        navigation.navigateToMatchStats(id)
        viewModel.onMatchStatsNavigated()
    }

    LaunchedEffect(uiState.navigateToAfterMatchStatsEventId) {
        val id = uiState.navigateToAfterMatchStatsEventId ?: return@LaunchedEffect
        navigation.navigateToAfterMatchStats(id)
        viewModel.onAfterMatchStatsNavigated()
    }

    LaunchedEffect(uiState.navigateToAttendanceAnalytics) {
        if (uiState.navigateToAttendanceAnalytics) {
            navigation.navigateToAttendanceAnalytics()
            viewModel.onAttendanceAnalyticsNavigated()
        }
    }

    CoachDashboardContent(
        uiState = uiState,
        actions = viewModel,
        navigation = navigation
    )
}

@Composable
private fun CoachDashboardContent(
    uiState: CoachDashboardScreenUiState,
    actions: CoachDashboardScreenActions,
    navigation: INavigationRouter,
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(TAB_HOME) }

    if (uiState.showGrowTeamSheet) {
        GrowYourTeamBottomSheet(
            joinCode = uiState.activeTeam?.joinCode.orEmpty(),
            teamName = uiState.activeTeam?.teamName.orEmpty(),
            onDismiss = { actions.onGrowTeamDismissed() },
        )
    }
    //ROLE CHANGE
    val selectedMemberId = uiState.selectedMember

    if (selectedMemberId != null && uiState.showChangeRoleSheet) {
        ChangeRoleBottomSheet(
            selectedMember = selectedMemberId,
            selectedRole = uiState.selectedRole,
            onRoleSelected = { actions.onRoleSelected(it) },
            onUpdateRole = { actions.onChangeRole(selectedMemberId, uiState.selectedRole) },
            onDismiss = { actions.onRoleMenuDismiss() }
        )
    }

    // ACTION MENU
    if (selectedMemberId != null && !uiState.showRemoveMemberConfirm && !uiState.showChangeRoleSheet && !uiState.showJerseyNumberDialog) {
        RosterActionBottomSheet(
            memberId = selectedMemberId,
            onDismiss = { actions.onRosterMenuDismiss() },
            onRemoveMember = { actions.onRemoveMemberConfirmShow() },
            onChangeRole = { actions.onChangeRoleClicked() },
            onChangeJerseyNumber = { actions.onChangeJerseyNumberClick(selectedMemberId) },
        )
    }

    // JERSEY NUMBER
    if (uiState.showJerseyNumberDialog && selectedMemberId != null) {
        ChangeJerseyNumberDialog(
            input = uiState.jerseyNumberInput,
            onInputChange = { actions.onJerseyNumberInputChange(it) },
            onConfirm = { actions.onChangeJerseyNumber(selectedMemberId, it) },
            onDismiss = { actions.onJerseyNumberDialogDismiss() },
        )
    }
    // REMOVE MEMBER
    if (uiState.showRemoveMemberConfirm && selectedMemberId != null) {
        AlertDialog(
            onDismissRequest = { actions.onRemoveMemberConfirmDismiss() },
            title = { Text(stringResource(R.string.remove_member_title)) },
            text = { Text(stringResource(R.string.remove_member_message)) },
            confirmButton = {
                TextButton(onClick = { actions.onRemoveMember(selectedMemberId) }) {
                    Text(
                        text = stringResource(R.string.remove_member_confirm),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { actions.onRemoveMemberConfirmDismiss() }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
        )
    }

    BaseScreenWithBottomNav(
        topBar = {
            CoachDashboardTopBar(
                coachName = uiState.coachName,
                activeTeam = uiState.activeTeam,
                teams = uiState.teams,
                isDropdownOpen = uiState.isTeamDropdownOpen,
                onToggleDropdown = { actions.onTeamDropdownToggle() },
                onTeamSelected = { actions.onTeamSelected(it) },
                onSettingsTap = { navigation.navigateToSettingsScreen() },
                onAnalyticsTap = { actions.onAnalyticsTap() },
            )
        },
        bottomBar = {
            CoachDashboardBottomNav(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
            )
        },
        floatingActionButton = {
            when (selectedTab) {
                TAB_SCHEDULE -> FloatingActionButton(
                    onClick = { actions.onAddEvent() },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(R.string.schedule_add_event_cd),
                    )
                }
                TAB_ROSTER -> FloatingActionButton(
                    onClick = { actions.onGrowTeamClicked() },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(R.string.roster_grow_team_cd),
                    )
                }
            }
        },
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                when (selectedTab) {
                    TAB_HOME -> CoachHomeTab(
                        uiState = uiState,
                        onLogAttendance = { actions.onLogAttendance(it) },
                        onLogStatistics = { actions.onLogStatistics(it) },
                        onCardClicked = { actions.onCardClicked(it) },
                    )
                    TAB_SCHEDULE -> ScheduleTab(
                        uiState = uiState,
                        onPreviousMonth = { actions.onPreviousMonth() },
                        onNextMonth = { actions.onNextMonth() },
                        onDateSelected = { actions.onDateSelected(it) },
                        onLogAttendance = { actions.onLogAttendance(it) },
                        onLogStatistics = { actions.onLogStatistics(it) },
                        onShowStatistics = { actions.onShowStatistics(it) },
                        onCardClicked = { actions.onCardClicked(it) },
                    )
                    TAB_ROSTER -> MembersTab(
                        uiState = ClubManagementScreenUiState(
                            filteredMembers = uiState.filteredMembers,
                            memberSearchQuery = uiState.memberSearchQuery,
                            memberFilter = uiState.memberFilter,
                            isMembersLoading = uiState.isMembersLoading,
                        ),
                        onSearchQueryChange = { actions.onRosterSearchQueryChange(it) },
                        onFilterChange = { actions.onRosterFilterChange(it) },
                        onMenuClick = { actions.onRosterMenuClick(it) },
                        showMenu = uiState.currentUserRole == "coach",
                    )
                }
            }
        }
    }
}

// ── Top app bar ───────────────────────────────────────────────────────────────

@Composable
private fun CoachDashboardTopBar(
    coachName: String,
    activeTeam: TeamUiItem?,
    teams: List<TeamUiItem>,
    isDropdownOpen: Boolean,
    onToggleDropdown: () -> Unit,
    onTeamSelected: (String) -> Unit,
    onSettingsTap: () -> Unit,
    onAnalyticsTap: () -> Unit,
) {
    val outlineColor = MaterialTheme.colorScheme.surface

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .drawWithContent {
                drawContent()
                val thickness = 1.dp.toPx()
                val y = thickness / 2
                drawLine(
                    color = outlineColor,
                    start = Offset(x = 0f, y = y),
                    end = Offset(x = size.width, y = y),
                    strokeWidth = thickness,
                )
            },
        color = MaterialTheme.colorScheme.background,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(64.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left cluster — avatar + team name + dropdown
            Box {
                Row(
                    modifier = Modifier.clickable(onClick = onToggleDropdown),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (activeTeam?.accentColor?.isNotBlank() == true) {
                        KourtTeamAvatar(
                            initials = activeTeam.initials,
                            accentColor = activeTeam.accentColor,
                            size = 40.dp,
                        )
                    } else {
                        KourtAvatarLeading(fallbackText = coachName, size = 40.dp)
                    }
                    Column {
                        Text(
                            text = stringResource(R.string.coach_dashboard_managing_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = activeTeam?.teamName
                                    ?: stringResource(R.string.coach_dashboard_no_team),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }

                DropdownMenu(
                    expanded = isDropdownOpen,
                    onDismissRequest = onToggleDropdown,
                ) {
                    teams.forEach { team ->
                        val isActive = team.teamId == activeTeam?.teamId
                        DropdownMenuItem(
                            leadingIcon = {
                                KourtTeamAvatar(
                                    initials = team.initials,
                                    accentColor = team.accentColor,
                                    size = 32.dp,
                                )
                            },
                            text = {
                                Text(
                                    text = team.teamName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isActive) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface,
                                )
                            },
                            trailingIcon = if (isActive) {
                                {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            } else null,
                            onClick = { onTeamSelected(team.teamId) },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            IconButton(onClick = onAnalyticsTap) {
                Icon(
                    painter = painterResource(R.drawable.analytics),
                    contentDescription = stringResource(R.string.coach_dashboard_analytics_cd),
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(24.dp),
                )
            }

            IconButton(onClick = onSettingsTap) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = stringResource(R.string.coach_dashboard_settings_cd),
                    tint = MaterialTheme.colorScheme.onBackground,
                )
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
private fun CoachDashboardBottomNav(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
) {
    val items = listOf(
        BottomNavItem(stringResource(R.string.coach_dashboard_tab_home), painterResource(R.drawable.home), TAB_HOME),
        BottomNavItem(stringResource(R.string.coach_dashboard_tab_schedule), painterResource(R.drawable.event), TAB_SCHEDULE),
        BottomNavItem(stringResource(R.string.coach_dashboard_tab_roster), painterResource(R.drawable.group), TAB_ROSTER),
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
                    strokeWidth = thickness,
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
                val tint = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)

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
private fun ChangeJerseyNumberDialog(
    input: String,
    onInputChange: (String) -> Unit,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val number = input.toIntOrNull()
    val isValid = input.isNotEmpty() && number != null && number >= 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.jersey_number_dialog_title),
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        text = {
            OutlinedTextField(
                value = input,
                onValueChange = { new ->
                    if (new.all { it.isDigit() } && new.length <= 3) onInputChange(new)
                },
                label = { Text(stringResource(R.string.jersey_number_hint)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(onClick = { if (isValid) onConfirm(number!!) }, enabled = isValid) {
                Text(stringResource(R.string.jersey_number_update))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
    )
}
