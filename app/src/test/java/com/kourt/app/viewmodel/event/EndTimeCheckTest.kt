package com.kourt.app.viewmodel.event

/* Tests for [isEndAfterStart]: the event form's end-time check. */

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EndTimeCheckTest {

    @Test
    fun `end after start is accepted`() = assertTrue(isEndAfterStart("18:00", "19:30"))

    @Test
    fun `end before start is rejected`() = assertFalse(isEndAfterStart("19:00", "18:00"))

    @Test
    fun `end equal to start is rejected`() = assertFalse(isEndAfterStart("18:00", "18:00"))

    @Test
    fun `unparsable time is left to the required-field checks`() = assertTrue(isEndAfterStart("", "18:00"))
}
