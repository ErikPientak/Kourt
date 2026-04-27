package com.kourt.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kourt.app.R
import com.kourt.app.ui.screens.dashboard.coach.EventUiItem
import com.kourt.app.ui.theme.KourtTheme
import java.util.concurrent.TimeUnit

private val PracticeBlue = Color(0xFF2563EB)

@Composable
fun ScheduleEventCard(
    event: EventUiItem,
    onCtaClick: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isCancelled = event.status.lowercase() == "cancelled"
    val today = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())
    val isPast = event.epochDay >= 0 && event.epochDay < today && !isCancelled
    val colorScheme = MaterialTheme.colorScheme
    val badgeColor = when {
        isCancelled || isPast                   -> Color(0xFF6B7280)
        event.type.lowercase() == "match"       -> colorScheme.primary
        else                                    -> PracticeBlue
    }
    val ctaText = if (event.type.lowercase() == "match") {
        stringResource(R.string.schedule_log_statistics)
    } else {
        stringResource(R.string.schedule_log_attendance)
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colorScheme.surface,
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (isCancelled || isPast) 0.8f else 1f),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .clickable(onClick = onClick),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // ── Top row: type badge + date/time ──────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(badgeColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = event.type.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = badgeColor,
                    )
                }
                Text(
                    text = "${event.dateLabel}  •  ${event.startTime}",
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }

            // ── Title ─────────────────────────────────────────────────────────
            val titleColor = colorScheme.onSurface
            Text(
                text = event.title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = titleColor,
                modifier = if (isCancelled) {
                    Modifier.drawWithContent {
                        drawContent()
                        drawLine(
                            color = titleColor,
                            start = Offset(0f, size.height / 2f),
                            end = Offset(size.width, size.height / 2f),
                            strokeWidth = 2.dp.toPx(),
                        )
                    }
                } else Modifier,
            )

            // ── Subtitle (conditional) ────────────────────────────────────────
            if (event.subtitle.isNotBlank()) {
                Text(
                    text = event.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }

            // ── Location row ──────────────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(R.drawable.city),
                    contentDescription = null,
                    tint = colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = event.location.ifBlank { stringResource(R.string.coach_dashboard_location_tbd) },
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }

            // ── CTA button — hidden for cancelled events ──────────────────────
            if (!isCancelled) {
                KourtButton(
                    text = ctaText,
                    onClick = onCtaClick,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

private val previewMatchEvent = EventUiItem(
    eventId = "evt-001",
    title = "Spartans vs. Bulls",
    type = "match",
    status = "upcoming",
    dayOfWeek = "SAT",
    dayOfMonth = 26,
    startTime = "18:00",
    location = "United Center, Chicago",
    isToday = false,
    dateLabel = "Apr 26",
    epochDay = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis()) + 5,
    subtitle = "Home opener — full roster required",
)

private val previewPracticeEvent = EventUiItem(
    eventId = "evt-002",
    title = "Defensive Drills",
    type = "practice",
    status = "upcoming",
    dayOfWeek = "TUE",
    dayOfMonth = 22,
    startTime = "09:00",
    location = "Training Facility B",
    isToday = true,
    dateLabel = "Today",
    epochDay = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis()),
    subtitle = "",
)

@Preview(name = "ScheduleEventCard Match – Light", showBackground = true, backgroundColor = 0xFFF5F5F5)
@Composable
private fun ScheduleEventCardMatchLightPreview() {
    KourtTheme(darkTheme = false) {
        ScheduleEventCard(
            event = previewMatchEvent,
            onCtaClick = {},
            onClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "ScheduleEventCard Practice – Light", showBackground = true, backgroundColor = 0xFFF5F5F5)
@Composable
private fun ScheduleEventCardPracticeLightPreview() {
    KourtTheme(darkTheme = false) {
        ScheduleEventCard(
            event = previewPracticeEvent,
            onCtaClick = {},
            onClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "ScheduleEventCard Match – Dark", showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun ScheduleEventCardMatchDarkPreview() {
    KourtTheme(darkTheme = true) {
        ScheduleEventCard(
            event = previewMatchEvent,
            onCtaClick = {},
            onClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "ScheduleEventCard Practice – Dark", showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun ScheduleEventCardPracticeDarkPreview() {
    KourtTheme(darkTheme = true) {
        ScheduleEventCard(
            event = previewPracticeEvent,
            onCtaClick = {},
            onClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "ScheduleEventCard Cancelled – Dark", showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun ScheduleEventCardCancelledDarkPreview() {
    KourtTheme(darkTheme = true) {
        ScheduleEventCard(
            event = previewMatchEvent.copy(status = "cancelled"),
            onCtaClick = {},
            onClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "ScheduleEventCard Cancelled – Light", showBackground = true, backgroundColor = 0xFFF5F5F5)
@Composable
private fun ScheduleEventCardCancelledLightPreview() {
    KourtTheme(darkTheme = false) {
        ScheduleEventCard(
            event = previewMatchEvent.copy(status = "cancelled"),
            onCtaClick = {},
            onClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
