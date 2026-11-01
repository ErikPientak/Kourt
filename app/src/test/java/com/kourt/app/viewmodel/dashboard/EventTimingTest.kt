package com.kourt.app.viewmodel.dashboard

/*
 * Tests for isEventOver in BaseDashboardViewModel.kt:
 *   - an event on an earlier day is over
 *   - today's event is over only once its end time has passed
 *   - without a readable end time, today's event lasts until the day ends
 *   - a future event is never over
 */

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class EventTimingTest {

    private val today = LocalDate.of(2026, 11, 5)
    private val todayEpochDay = today.toEpochDay()

    private fun at(hour: Int, minute: Int = 0) = LocalDateTime.of(today, LocalTime.of(hour, minute))

    @Test
    fun `an event on an earlier day is over`() {
        assertTrue(isEventOver(todayEpochDay - 1, "20:00", now = at(0, 5)))
    }

    @Test
    fun `today's event is over only once its end time has passed`() {
        assertFalse(isEventOver(todayEpochDay, "19:30", now = at(19, 29)))
        assertTrue(isEventOver(todayEpochDay, "19:30", now = at(19, 30)))
    }

    @Test
    fun `without a readable end time today's event lasts until the day ends`() {
        assertFalse(isEventOver(todayEpochDay, "", now = at(23, 59)))
        assertFalse(isEventOver(todayEpochDay, "7pm", now = at(23, 59)))
    }

    @Test
    fun `a future event is never over`() {
        assertFalse(isEventOver(todayEpochDay + 1, "08:00", now = at(23, 0)))
    }
}
