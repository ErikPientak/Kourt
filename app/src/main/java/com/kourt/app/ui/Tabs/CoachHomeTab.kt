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
import com.kourt.app.ui.screens.dashboard.coach.CoachDashboardScreenUiState

@Composable
fun CoachHomeTab(
    uiState: CoachDashboardScreenUiState,
    onLogAttendance: (String) -> Unit,
    onLogStatistics: (String) -> Unit,
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
                ctaText = if (uiState.upNextEvent.type.lowercase() == "match") {
                    R.string.schedule_log_statistics
                } else {
                    R.string.schedule_log_attendance
                },
                onCtaClick = {
                    if (uiState.upNextEvent.type.lowercase() == "match") {
                        onLogStatistics(uiState.upNextEvent.eventId)
                    } else {
                        onLogAttendance(uiState.upNextEvent.eventId)
                    }
                },
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