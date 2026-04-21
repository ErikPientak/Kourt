package com.kourt.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kourt.app.ui.theme.KourtTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

private data class CalendarDay(
    val dayOfMonth: Int,
    val epochDay: Long,
    val isCurrentMonth: Boolean,
)

@Composable
fun KourtCalendar(
    year: Int,
    month: Int,
    selectedEpochDay: Long,
    eventDays: Set<Long>,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDateSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val locale = Locale.getDefault()

    // Month+year header label
    val monthYearLabel = remember(year, month, locale) {
        val cal = Calendar.getInstance().apply { set(year, month, 1) }
        SimpleDateFormat("MMMM yyyy", locale).format(cal.time)
    }

    // Day-of-week header labels: Sun–Sat derived from Calendar, never hardcoded
    val dayOfWeekLabels = remember(locale) {
        val cal = Calendar.getInstance(locale)
        val firstDayOfWeek = cal.firstDayOfWeek
        (0 until 7).map { offset ->
            val dayIndex = ((firstDayOfWeek - 1 + offset) % 7) + 1
            val tmp = Calendar.getInstance(locale).apply { set(Calendar.DAY_OF_WEEK, dayIndex) }
            tmp.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, locale)
                ?.uppercase(locale)
                ?: ""
        }
    }

    // Calendar grid computation
    val calendarDays: List<CalendarDay> = remember(year, month) {
        val days = mutableListOf<CalendarDay>()
        val locale = Locale.getDefault()

        val firstOfMonth = Calendar.getInstance(locale).apply {
            set(year, month, 1, 12, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val firstDayOfWeek = firstOfMonth.firstDayOfWeek
        // How many leading cells to fill (days before the 1st of the month)
        val startDow = firstOfMonth.get(Calendar.DAY_OF_WEEK)
        val leadingOffset = ((startDow - firstDayOfWeek + 7) % 7)

        // Previous month trailing days
        val prevMonth = Calendar.getInstance(locale).apply {
            set(year, month, 1, 12, 0, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.MONTH, -1)
        }
        val prevMonthMaxDay = prevMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (i in leadingOffset downTo 1) {
            val day = prevMonthMaxDay - i + 1
            prevMonth.set(Calendar.DAY_OF_MONTH, day)
            days.add(
                CalendarDay(
                    dayOfMonth = day,
                    epochDay = TimeUnit.MILLISECONDS.toDays(prevMonth.timeInMillis),
                    isCurrentMonth = false,
                )
            )
        }

        // Current month days
        val daysInMonth = firstOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (day in 1..daysInMonth) {
            firstOfMonth.set(Calendar.DAY_OF_MONTH, day)
            days.add(
                CalendarDay(
                    dayOfMonth = day,
                    epochDay = TimeUnit.MILLISECONDS.toDays(firstOfMonth.timeInMillis),
                    isCurrentMonth = true,
                )
            )
        }

        // Trailing days to complete up to 42 cells (6 rows × 7)
        val nextMonth = Calendar.getInstance(locale).apply {
            set(year, month, 1, 12, 0, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.MONTH, 1)
        }
        var trailingDay = 1
        while (days.size < 42) {
            nextMonth.set(Calendar.DAY_OF_MONTH, trailingDay)
            days.add(
                CalendarDay(
                    dayOfMonth = trailingDay,
                    epochDay = TimeUnit.MILLISECONDS.toDays(nextMonth.timeInMillis),
                    isCurrentMonth = false,
                )
            )
            trailingDay++
        }

        days
    }

    val todayEpochDay = remember { TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis()) }

    Column(modifier = modifier) {
        // ── Month navigation header ───────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onPreviousMonth) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
            Text(
                text = monthYearLabel,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            IconButton(onClick = onNextMonth) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        // ── Day-of-week labels ────────────────────────────────────────────────
        Row(modifier = Modifier.fillMaxWidth()) {
            dayOfWeekLabels.forEach { label ->
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // ── Calendar grid ─────────────────────────────────────────────────────
        val weeks = calendarDays.chunked(7)
        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
            weeks.forEach { week ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    week.forEach { day ->
                        DayCell(
                            day = day,
                            isSelected = day.epochDay == selectedEpochDay,
                            isToday = day.epochDay == todayEpochDay,
                            hasEvent = day.epochDay in eventDays,
                            onSelected = { onDateSelected(day.epochDay) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: CalendarDay,
    isSelected: Boolean,
    isToday: Boolean,
    hasEvent: Boolean,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val circleBackground = when {
        isToday -> MaterialTheme.colorScheme.primary
        isSelected -> MaterialTheme.colorScheme.surface
        else -> androidx.compose.ui.graphics.Color.Transparent
    }
    val textColor = when {
        isToday -> MaterialTheme.colorScheme.onPrimary
        day.isCurrentMonth -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
    }
    val dotBackground = if (hasEvent && day.isCurrentMonth) {
        MaterialTheme.colorScheme.primary
    } else {
        androidx.compose.ui.graphics.Color.Transparent
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable(onClick = onSelected)
            .padding(vertical = 4.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(circleBackground),
        ) {
            Text(
                text = day.dayOfMonth.toString(),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                ),
                color = textColor,
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Box(
            modifier = Modifier
                .size(4.dp)
                .clip(CircleShape)
                .background(dotBackground),
        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(name = "KourtCalendar – Light", showBackground = true, backgroundColor = 0xFFF5F5F5)
@Composable
private fun KourtCalendarLightPreview() {
    KourtTheme(darkTheme = false) {
        KourtCalendar(
            year = 2026,
            month = Calendar.APRIL,
            selectedEpochDay = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis()),
            eventDays = setOf(
                TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis()),
                TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis()) + 3,
                TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis()) + 7,
            ),
            onPreviousMonth = {},
            onNextMonth = {},
            onDateSelected = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        )
    }
}

@Preview(name = "KourtCalendar – Dark", showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun KourtCalendarDarkPreview() {
    KourtTheme(darkTheme = true) {
        KourtCalendar(
            year = 2026,
            month = Calendar.APRIL,
            selectedEpochDay = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis()),
            eventDays = setOf(
                TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis()),
                TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis()) + 3,
                TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis()) + 7,
            ),
            onPreviousMonth = {},
            onNextMonth = {},
            onDateSelected = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        )
    }
}
