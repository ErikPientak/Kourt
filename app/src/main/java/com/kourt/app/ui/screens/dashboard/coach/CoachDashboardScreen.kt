package com.kourt.app.ui.screens.dashboard.coach

import android.text.format.DateFormat
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kourt.app.R
import com.kourt.app.navigation.INavigationRouter
import com.kourt.app.ui.components.EventCard
import com.kourt.app.ui.components.KourtAvatarLeading
import com.kourt.app.ui.components.KourtTeamAvatar
import com.kourt.app.ui.components.KourtButton
import com.kourt.app.ui.components.KourtCalendar
import com.kourt.app.ui.components.ScheduleEventCard
import com.kourt.app.ui.screens.club.management.ClubManagementScreenUiState
import com.kourt.app.ui.screens.club.management.MemberFilter
import com.kourt.app.ui.Tabs.MembersTab
import com.kourt.app.ui.Tabs.ScheduleTab
import com.kourt.app.ui.components.UpNextCard
import com.kourt.app.viewmodel.dashboard.CoachDashboardViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

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

    CoachDashboardContent(
        uiState = uiState,
        onTeamDropdownToggle = viewModel::onTeamDropdownToggle,
        onTeamSelected = viewModel::onTeamSelected,
        onSettingsTap = { navigation.navigateToSettingsScreen() },
        onUpdateLineup = viewModel::onUpdateLineup,
        onRosterSearchQueryChange = viewModel::onRosterSearchQueryChange,
        onRosterFilterChange = viewModel::onRosterFilterChange,
        onPreviousMonth = viewModel::onPreviousMonth,
        onNextMonth = viewModel::onNextMonth,
        onDateSelected = viewModel::onDateSelected,
        onLogAttendance = viewModel::onLogAttendance,
        onLogStatistics = viewModel::onLogStatistics,
        onAddEvent = viewModel::onAddEvent,
        onCardClicked = viewModel::onCardClicked,
    )
}

@Composable
private fun CoachDashboardContent(
    uiState: CoachDashboardScreenUiState,
    onTeamDropdownToggle: () -> Unit,
    onTeamSelected: (String) -> Unit,
    onSettingsTap: () -> Unit,
    onUpdateLineup: () -> Unit,
    onRosterSearchQueryChange: (String) -> Unit,
    onRosterFilterChange: (MemberFilter) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDateSelected: (Long) -> Unit,
    onLogAttendance: (String) -> Unit,
    onLogStatistics: (String) -> Unit,
    onAddEvent: () -> Unit,
    onCardClicked: (String) -> Unit
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(TAB_HOME) }

    BaseScreenWithBottomNav(
        topBar = {
            CoachDashboardTopBar(
                coachName = uiState.coachName,
                activeTeam = uiState.activeTeam,
                teams = uiState.teams,
                isDropdownOpen = uiState.isTeamDropdownOpen,
                onToggleDropdown = onTeamDropdownToggle,
                onTeamSelected = onTeamSelected,
                onSettingsTap = onSettingsTap,
            )
        },
        bottomBar = {
            CoachDashboardBottomNav(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
            )
        },
        floatingActionButton = {
            if (selectedTab == TAB_SCHEDULE) {
                FloatingActionButton(
                    onClick = onAddEvent,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(R.string.schedule_add_event_cd),
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
                    TAB_HOME -> HomeTab(
                        uiState = uiState,
                        onUpdateLineup = onUpdateLineup,
                        onCardClicked = onCardClicked
                        )
                    TAB_SCHEDULE -> ScheduleTab(
                        uiState = uiState,
                        onPreviousMonth = onPreviousMonth,
                        onNextMonth = onNextMonth,
                        onDateSelected = onDateSelected,
                        onLogAttendance = onLogAttendance,
                        onLogStatistics = onLogStatistics,
                        onCardClicked = onCardClicked,
                    )
                    TAB_ROSTER -> MembersTab(
                        uiState = ClubManagementScreenUiState(
                            filteredMembers = uiState.filteredMembers,
                            memberSearchQuery = uiState.memberSearchQuery,
                            memberFilter = uiState.memberFilter,
                            isMembersLoading = uiState.isMembersLoading,
                        ),
                        onSearchQueryChange = onRosterSearchQueryChange,
                        onFilterChange = onRosterFilterChange,
                        onMenuClick = {},
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

// ── Tab content ───────────────────────────────────────────────────────────────

@Composable
private fun HomeTab(
    uiState: CoachDashboardScreenUiState,
    onUpdateLineup: () -> Unit,
    onCardClicked: (String) -> Unit,
    ) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.coach_dashboard_up_next),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
        )

        if (uiState.upNextEvent != null) {
            UpNextCard(
                event = uiState.upNextEvent,
                rosterSize = uiState.members.size,
                attendeeNames = uiState.upNextAttendees,
                ctaText = R.string.coach_dashboard_update_lineup,
                onCtaClick = onUpdateLineup,
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.coach_dashboard_no_events),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
            }
        }

        if (uiState.upcomingEvents.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.coach_dashboard_upcoming),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
            )
            uiState.upcomingEvents.forEach { event ->
                EventCard(
                    title = event.title,
                    eventType = event.type,
                    status = event.status,
                    epochDay = event.epochDay,
                    dayOfWeek = event.dayOfWeek,
                    dayOfMonth = event.dayOfMonth,
                    startTime = event.startTime,
                    location = event.location,
                    showChevron = false,
                    onClick = { onCardClicked(event.eventId) }
                    )
            }
        }
    }
}

