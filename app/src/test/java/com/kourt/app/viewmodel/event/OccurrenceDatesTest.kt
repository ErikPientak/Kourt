package com.kourt.app.viewmodel.event

/*
 * Tests for [calculateOccurrenceDates]: repeat days are ISO (1 = Mon … 7 = Sun)
 * and both ends of the Starts on … Ends on range are inclusive.
 */

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class OccurrenceDatesTest {

    private fun day(iso: String) = LocalDate.parse(iso).toEpochDay()
    private fun Long.asDate() = LocalDate.ofEpochDay(this)

    @Test
    fun `mon and wed in range including both ends`() {
        // 2026-09-07 is a Monday, 2026-09-16 a Wednesday
        val dates = calculateOccurrenceDates(day("2026-09-07"), day("2026-09-16"), setOf(1, 3))

        assertEquals(
            listOf("2026-09-07", "2026-09-09", "2026-09-14", "2026-09-16").map(::day),
            dates,
        )
    }

    @Test
    fun `sunday maps to iso 7`() {
        val dates = calculateOccurrenceDates(day("2026-09-01"), day("2026-09-30"), setOf(7))

        assertEquals(4, dates.size)
        assertTrue(dates.all { it.asDate().dayOfWeek == DayOfWeek.SUNDAY })
    }

    @Test
    fun `range without a matching weekday is empty`() {
        // Tue 2026-09-08 … Thu 2026-09-10, asking for Saturdays
        val dates = calculateOccurrenceDates(day("2026-09-08"), day("2026-09-10"), setOf(6))

        assertTrue(dates.isEmpty())
    }

    @Test
    fun `single day range`() {
        val dates = calculateOccurrenceDates(day("2026-09-07"), day("2026-09-07"), setOf(1))

        assertEquals(listOf(day("2026-09-07")), dates)
    }
}
