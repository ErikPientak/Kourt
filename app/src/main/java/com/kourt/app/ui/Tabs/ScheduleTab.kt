package com.kourt.app.ui.Tabs

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kourt.app.R
import com.kourt.app.ui.components.KourtCalendar
import com.kourt.app.ui.components.ScheduleEventCard
import com.kourt.app.ui.screens.dashboard.coach.CoachDashboardScreenUiState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun ScheduleTab(
    uiState: CoachDashboardScreenUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDateSelected: (Long) -> Unit,
    onLogAttendance: (String) -> Unit,
    onLogStatistics: (String) -> Unit,
    onCardClicked: (String) -> Unit,
    ctaTextOverride: String? = null,
) {
    val locale = Locale.getDefault()
    val pattern = remember(locale) {
        DateFormat.getBestDateTimePattern(locale, "MMMd")
    }
    val selectedLabel = remember(uiState.selectedEpochDay, pattern, locale) {
        val cal = Calendar.getInstance().apply {
            timeInMillis = TimeUnit.DAYS.toMillis(uiState.selectedEpochDay)
        }
        SimpleDateFormat(pattern, locale).format(cal.time).uppercase(locale)
    }
    val visibleEvents = remember(uiState.allEvents, uiState.selectedEpochDay) {
        uiState.allEvents.filter { it.epochDay >= uiState.selectedEpochDay }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // ── Calendar (no extra horizontal padding — KourtCalendar handles its own) ──
        KourtCalendar(
            year = uiState.calendarYear,
            month = uiState.calendarMonth,
            selectedEpochDay = uiState.selectedEpochDay,
            eventDays = uiState.eventDaysInView,
            onPreviousMonth = onPreviousMonth,
            onNextMonth = onNextMonth,
            onDateSelected = onDateSelected,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
        )

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
        )

        // ── Section header ────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.schedule_upcoming_events),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            )
            Text(
                text = selectedLabel,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
            )
        }

        // ── Event list ────────────────────────────────────────────────────────
        if (visibleEvents.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.schedule_no_events),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(visibleEvents, key = { it.eventId }) { event ->
                    ScheduleEventCard(
                        event = event,
                        onCtaClick = {
                            if (event.type.lowercase() == "match") {
                                onLogStatistics(event.eventId)
                            } else {
                                onLogAttendance(event.eventId)
                            }
                        },
                        ctaText = ctaTextOverride,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onCardClicked(event.eventId) },
                    )
                }
            }
        }
    }
}