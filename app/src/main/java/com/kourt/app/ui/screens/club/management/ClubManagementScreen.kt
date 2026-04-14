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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kourt.app.navigation.INavigationRouter
import com.kourt.app.ui.components.BaseScreenWithBottomNav
import com.kourt.app.ui.components.TeamCard
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
        onAddTeam = { navigation.navigateToAddTeamScreen(uiState.clubId)
                    viewModel.onAddTeam() },
        onTeamClick = viewModel::onTeamClick,
        onSettingsTap = viewModel::onSettingsTap,
    )
}

@Composable
private fun ClubManagementScreenContent(
    uiState: ClubManagementScreenUiState,
    onAddTeam: () -> Unit,
    onTeamClick: (String) -> Unit,
    onSettingsTap: () -> Unit,
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(TAB_TEAMS) }

    BaseScreenWithBottomNav(
        title = uiState.clubName.ifBlank { "Club Management" },
        trailingIcon = {
            IconButton(onClick = onSettingsTap) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
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
            if (selectedTab == TAB_TEAMS) {
                FloatingActionButton(
                    onClick = onAddTeam,
                    shape = RoundedCornerShape(16.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add team",
                    )
                }
            }
        },
    ) { padding ->
        // ── Content area ─────────────────────────────────────────────────
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
                TAB_MEMBERS -> ComingSoonTab()
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
        BottomNavItem("Teams", painterResource(com.kourt.app.R.drawable.group), TAB_TEAMS),
        BottomNavItem("Members", painterResource(com.kourt.app.R.drawable.person_edit), TAB_MEMBERS),
        BottomNavItem("Events", painterResource(com.kourt.app.R.drawable.event), TAB_EVENTS),
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
                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
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

// ── Teams tab ─────────────────────────────────────────────────────────────────

@Composable
private fun TeamsTab(
    uiState: ClubManagementScreenUiState,
    onTeamClick: (String) -> Unit,
) {
    when {
        uiState.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        uiState.teams.isEmpty() -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "No teams yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = "Tap + to add one",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    )
                }
            }
        }

        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
            ) {
                item {
                    Text(
                        text = "ACTIVE TEAMS (${uiState.teams.size})",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    )
                }

                items(
                    items = uiState.teams,
                    key = { team -> team.id },
                ) { team ->
                    TeamCard(
                        teamName = team.name,
                        headCoach = team.headCoach,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        onClick = { onTeamClick(team.id) },
                    )
                }
            }
        }
    }
}

// ── Stub tabs ─────────────────────────────────────────────────────────────────

@Composable
private fun ComingSoonTab() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Coming soon",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
        )
    }
}

