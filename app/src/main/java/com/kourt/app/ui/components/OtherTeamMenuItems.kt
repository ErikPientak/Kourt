package com.kourt.app.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kourt.app.ui.screens.dashboard.coach.TeamUiItem

/**
 * Teams the user belongs to through the other dashboard (a coach who also plays, or the reverse),
 * listed under the normal teams of a dashboard's team switcher with the user's role on each.
 */
@Composable
fun OtherTeamMenuItems(
    teams: List<TeamUiItem>,
    onTeamSelected: (String) -> Unit,
) {
    if (teams.isEmpty()) return
    HorizontalDivider()
    teams.forEach { team ->
        DropdownMenuItem(
            leadingIcon = {
                KourtTeamAvatar(initials = team.initials, accentColor = team.accentColor, size = 32.dp)
            },
            text = { Text(text = team.teamName, style = MaterialTheme.typography.bodyMedium) },
            trailingIcon = {
                Text(
                    text = roleLabel(team.role),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            },
            onClick = { onTeamSelected(team.teamId) },
        )
    }
}
