package com.kourt.app.viewmodel.dashboard

/* Tests for [eventDaysInMonth]: calendar dots for the month on screen (month is 0-based, as Calendar). */

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class EventDaysInMonthTest {

    private fun day(iso: String) = LocalDate.parse(iso).toEpochDay()

    @Test
    fun `31st of a 31-day month is included`() {
        val days = listOf(day("2026-10-26"), day("2026-10-31"), day("2026-11-01"))
        assertEquals(setOf(day("2026-10-26"), day("2026-10-31")), eventDaysInMonth(days, 2026, 9))
    }

    @Test
    fun `first and last day of a 30-day month, neighbours excluded`() {
        val days = listOf(day("2026-10-31"), day("2026-11-01"), day("2026-11-30"), day("2026-12-01"))
        assertEquals(setOf(day("2026-11-01"), day("2026-11-30")), eventDaysInMonth(days, 2026, 10))
    }

    @Test
    fun `february in a non-leap year ends on the 28th`() {
        val days = listOf(day("2027-02-28"), day("2027-03-01"))
        assertEquals(setOf(day("2027-02-28")), eventDaysInMonth(days, 2027, 1))
    }
}
