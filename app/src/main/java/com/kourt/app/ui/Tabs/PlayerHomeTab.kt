package com.kourt.app.ui.Tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kourt.app.R
import com.kourt.app.ui.components.EventCard
import com.kourt.app.ui.components.UpNextCard
import com.kourt.app.ui.screens.dashboard.player.PlayerDashboardScreenUiState


@Composable
fun PlayerHomeTab(
    uiState: PlayerDashboardScreenUiState,
    onUpdateAttendance: () -> Unit,
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

        val upNext = uiState.upNextEvent
        if (upNext != null) {
            UpNextCard(
                event = upNext,
                rosterSize = uiState.members.size,
                myRsvpStatus = uiState.myRsvpStatus,
                ctaText = R.string.player_dashboard_update_attendance,
                onCtaClick = onUpdateAttendance,
                attendeeNames = uiState.upNextAttendees
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
                    showChevron = true,
                    onClick = { onCardClicked(event.eventId) },
                )
            }
        }
    }
}