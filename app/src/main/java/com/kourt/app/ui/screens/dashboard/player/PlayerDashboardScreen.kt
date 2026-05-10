package com.kourt.app.ui.screens.dashboard.player

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import com.kourt.app.ui.components.BaseScreenWithBottomNav
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.compose.currentBackStackEntryAsState
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
import com.kourt.app.navigation.Destination
import com.kourt.app.navigation.INavigationRouter
import com.kourt.app.ui.Tabs.MembersTab
import com.kourt.app.ui.Tabs.PlayerHomeTab
import com.kourt.app.ui.Tabs.ScheduleTab
import com.kourt.app.ui.components.EventCard
import com.kourt.app.ui.components.KourtAvatarLeading
import com.kourt.app.ui.components.KourtTeamAvatar
import com.kourt.app.ui.components.UpNextCard
import com.kourt.app.ui.components.bottomSheets.RsvpReasonBottomSheet
import com.kourt.app.ui.components.bottomSheets.RsvpSelectionBottomSheet
import com.kourt.app.ui.screens.club.management.ClubManagementScreenUiState
import com.kourt.app.ui.screens.dashboard.coach.CoachDashboardScreenUiState
import com.kourt.app.ui.screens.dashboard.coach.TeamUiItem
import com.kourt.app.viewmodel.dashboard.PlayerDashboardViewModel

private const val TAB_HOME = 0
private const val TAB_SCHEDULE = 1
private const val TAB_ROSTER = 2

@Composable
fun PlayerDashboardScreen(
    navigation: INavigationRouter,
    viewModel: PlayerDashboardViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState

    val navBackStackEntry by navigation.getNavController().currentBackStackEntryAsState()
    LaunchedEffect(navBackStackEntry) {
        if (navBackStackEntry?.destination?.route == Destination.PlayerDashboardScreen.route) {
            viewModel.refreshEvents()
        }
    }

    LaunchedEffect(uiState.navigateToEventDetailId) {
        val id = uiState.navigateToEventDetailId ?: return@LaunchedEffect
        navigation.navigateToEventDetail(id)
        viewModel.onEventDetailNavigated()
    }

    PlayerDashboardContent(
        uiState = uiState,
        actions = viewModel,
        onSettingsTap = { navigation.navigateToSettingsScreen() },
        onCardClicked = viewModel::onCardClicked,
    )
}

@Composable
private fun PlayerDashboardContent(
    uiState: PlayerDashboardScreenUiState,
    actions: PlayerDashboardScreenActions,
    onSettingsTap: () -> Unit,
    onCardClicked: (String) -> Unit,
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(TAB_HOME) }

    BaseScreenWithBottomNav(
        topBar = {
            PlayerDashboardTopBar(
                playerName = uiState.playerName,
                activeTeam = uiState.activeTeam,
                teams = uiState.teams,
                isDropdownOpen = uiState.isTeamDropdownOpen,
                onToggleDropdown = actions::onTeamDropdownToggle,
                onTeamSelected = actions::onTeamSelected,
                onSettingsTap = onSettingsTap,
            )
        },
        bottomBar = {
            PlayerDashboardBottomNav(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
            )
        },
    ) { padding ->
        if (uiState.isRsvpSelectionSheetOpen) {
            RsvpSelectionBottomSheet(
                onRsvpSelected = actions::onRsvpSelected,
                onDismiss = actions::onDismissRsvpSheet,
            )
        }
        if (uiState.isRsvpReasonSheetOpen) {
            RsvpReasonBottomSheet(
                selectedReason = uiState.selectedReason,
                reasonNote = uiState.reasonNote,
                isSubmitting = uiState.isRsvpSubmitting,
                onReasonSelected = actions::onReasonSelected,
                onNoteChanged = actions::onReasonNoteChanged,
                onSubmitReason = actions::onSubmitReason,
                onDismiss = actions::onDismissReasonSheet,
            )
        }
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                when (selectedTab) {
                    TAB_HOME -> PlayerHomeTab(
                        uiState = uiState,
                        onUpdateAttendance = actions::onUpdateAttendance,
                        onCardClicked = onCardClicked,
                    )
                    TAB_SCHEDULE -> ScheduleTab(
                        uiState = CoachDashboardScreenUiState(
                            allEvents = uiState.allEvents,
                            calendarYear = uiState.calendarYear,
                            calendarMonth = uiState.calendarMonth,
                            selectedEpochDay = uiState.selectedEpochDay,
                            eventDaysInView = uiState.eventDaysInView,
                        ),
                        onPreviousMonth = actions::onPreviousMonth,
                        onNextMonth = actions::onNextMonth,
                        onDateSelected = actions::onDateSelected,
                        onLogAttendance = actions::onUpdateAttendanceForEvent,
                        onLogStatistics = actions::onUpdateAttendanceForEvent,
                        onCardClicked = actions::onCardClicked,
                        ctaTextOverride = stringResource(R.string.player_dashboard_update_attendance),
                    )
                    TAB_ROSTER -> MembersTab(
                        uiState = ClubManagementScreenUiState(
                            filteredMembers = uiState.filteredMembers,
                            memberSearchQuery = uiState.memberSearchQuery,
                            memberFilter = uiState.memberFilter,
                            isMembersLoading = uiState.isMembersLoading,
                        ),
                        onSearchQueryChange = actions::onRosterSearchQueryChange,
                        onFilterChange = actions::onRosterFilterChange,
                        showMenu = false,
                        onMenuClick = {},
                    )
                }
            }
        }
    }
}

// ── Top app bar ───────────────────────────────────────────────────────────────

@Composable
private fun PlayerDashboardTopBar(
    playerName: String,
    activeTeam: TeamUiItem?,
    teams: List<TeamUiItem>,
    isDropdownOpen: Boolean,
    onToggleDropdown: () -> Unit,
    onTeamSelected: (String) -> Unit,
    onSettingsTap: () -> Unit,
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
            // Left cluster — avatar + team name + dropdown arrow
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
                        KourtAvatarLeading(fallbackText = playerName, size = 40.dp)
                    }

                    // No "Managing" sub-label for players — just team name + arrow
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

            IconButton(onClick = onSettingsTap) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = stringResource(R.string.coach_dashboard_settings_cd),
                    tint = MaterialTheme.colorScheme.onSurface,
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
private fun PlayerDashboardBottomNav(
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

