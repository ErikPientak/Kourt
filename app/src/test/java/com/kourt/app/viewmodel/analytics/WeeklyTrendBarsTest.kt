package com.kourt.app.viewmodel.analytics

/*
 * Tests for buildWeeklyTrendBars in AttendanceAnalyticsViewModel.kt:
 *   - trainings group into Monday-to-Sunday weeks
 *   - a week's rate is players present out of players recorded
 *   - trainings without recorded attendance are left out
 *   - the bar for this week is marked as current
 */

import com.kourt.app.data.model.Attendance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class WeeklyTrendBarsTest {

    // Monday 2 Nov 2026 is the start of ISO week 45.
    private val monday = LocalDate.of(2026, 11, 2).toEpochDay()

    private fun records(eventId: String, vararg statuses: String) =
        statuses.mapIndexed { i, status -> Attendance(eventId = eventId, userId = "u$i", status = status) }

    @Test
    fun `Monday and Sunday trainings fall into the same week`() {
        val bars = buildWeeklyTrendBars(
            eventDays = mapOf("mon" to monday, "sun" to monday + 6),
            attendanceByEvent = mapOf(
                "mon" to records("mon", "on_time", "late"),
                "sun" to records("sun", "on_time", "unexcused"),
            ),
            todayEpochDay = monday + 6,
            maxBars = 8,
        )

        val bar = bars.single()
        assertEquals(45, bar.weekNumber)
        assertEquals(0.75f, bar.rate, 0.0001f) // 3 present of 4 recorded
        assertTrue(bar.isCurrentWeek)
    }

    @Test
    fun `the next Monday starts a new week`() {
        val bars = buildWeeklyTrendBars(
            eventDays = mapOf("a" to monday + 6, "b" to monday + 7),
            attendanceByEvent = mapOf("a" to records("a", "on_time"), "b" to records("b", "excused")),
            todayEpochDay = monday + 7,
            maxBars = 8,
        )

        assertEquals(listOf(45, 46), bars.map { it.weekNumber })
        assertEquals(listOf(1f, 0f), bars.map { it.rate })
    }

    @Test
    fun `trainings without recorded attendance are left out`() {
        val bars = buildWeeklyTrendBars(
            eventDays = mapOf("logged" to monday, "notLogged" to monday + 2),
            attendanceByEvent = mapOf("logged" to records("logged", "on_time", "on_time")),
            todayEpochDay = monday + 3,
            maxBars = 8,
        )

        assertEquals(1f, bars.single().rate, 0.0001f)
    }
}
