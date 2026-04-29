package com.kourt.app.ui.components.bottomSheets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kourt.app.R
import com.kourt.app.ui.components.KourtAvatarLeading
import com.kourt.app.ui.screens.settings.profile.ChildTeamUiItem
import com.kourt.app.ui.screens.settings.profile.ManagedChildUiItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildActionBottomSheet(
    child: ManagedChildUiItem,
    onSwitchToChild: () -> Unit,
    onJoinTeam: () -> Unit,
    onLeaveTeam: (membershipId: String) -> Unit,
    onRemoveChild: () -> Unit,
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
            // Child header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                KourtAvatarLeading(
                    fallbackText = child.displayName,
                    size = 44.dp,
                    photoUrl = child.avatarId,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = child.displayName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (child.teams.isNotEmpty()) {
                        Text(
                            text = child.teams.joinToString(" · ") { it.teamName },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // Switch to this child
            ChildActionRow(
                icon = { Icon(painter = painterResource(R.drawable.check), contentDescription = null, modifier = Modifier.size(22.dp), tint = if (child.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface) },
                label = stringResource(R.string.child_action_switch, child.displayName),
                contentColor = if (child.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                onClick = { onSwitchToChild(); onDismiss() },
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // Teams section
            if (child.teams.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.child_action_teams_title),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                )
                child.teams.forEach { team ->
                    TeamLeaveRow(team = team, onLeave = onLeaveTeam)
                }
            }

            // Join another team
            ChildActionRow(
                icon = { Icon(painter = painterResource(R.drawable.handshake), contentDescription = null, modifier = Modifier.size(22.dp), tint = MaterialTheme.colorScheme.primary) },
                label = stringResource(R.string.child_action_join_team),
                contentColor = MaterialTheme.colorScheme.primary,
                onClick = onJoinTeam,
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // Remove child
            ChildActionRow(
                icon = { Icon(painter = painterResource(R.drawable.delete), contentDescription = null, modifier = Modifier.size(22.dp), tint = MaterialTheme.colorScheme.error) },
                label = stringResource(R.string.child_action_remove, child.displayName),
                contentColor = MaterialTheme.colorScheme.error,
                onClick = onRemoveChild,
            )
        }
    }
}

@Composable
private fun TeamLeaveRow(
    team: ChildTeamUiItem,
    onLeave: (membershipId: String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = team.teamName,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = { onLeave(team.membershipId) }) {
            Icon(
                painter = painterResource(R.drawable.delete),
                contentDescription = stringResource(R.string.child_action_leave_team, team.teamName),
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun ChildActionRow(
    icon: @Composable () -> Unit,
    label: String,
    contentColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            icon()
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = contentColor,
            )
        }
    }
}
