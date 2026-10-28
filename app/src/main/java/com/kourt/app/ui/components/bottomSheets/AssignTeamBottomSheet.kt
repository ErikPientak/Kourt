package com.kourt.app.ui.components.bottomSheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kourt.app.data.model.Team
import com.kourt.app.ui.components.TeamCard
import com.kourt.app.ui.components.roleBadgeColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignTeamBottomSheet(
    teams: List<Team>,
    /** teamId → the member's current role there; teams they're already on show it as a badge. */
    currentRoles: Map<String, String>,
    onTeamSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 8.dp),
        ) {
            // Title row — matches MemberActionBottomSheet header style
            Text(
                text = stringResource(com.kourt.app.R.string.assign_team_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
            )

            // Scrollable team list
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            ) {
                items(items = teams, key = { it.id }) { team ->
                    val role = currentRoles[team.id]
                    TeamCard(
                        teamName = team.name,
                        headCoach = team.headCoach,
                        badge = role?.uppercase(),
                        badgeColor = role?.let { roleBadgeColor(it) } ?: Color.Transparent,
                        onClick = {
                            onTeamSelected(team.id)
                            onDismiss()
                        },
                    )
                }
            }

            // Cancel button — full-width, matches sheet's dismiss style
            TextButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text(
                    text = stringResource(com.kourt.app.R.string.action_cancel),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}