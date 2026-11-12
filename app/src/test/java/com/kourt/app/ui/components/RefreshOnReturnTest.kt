package com.kourt.app.ui.components

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RefreshOnReturnTest {

    @Test
    fun `resume right after the screen appeared is the initial load and is skipped`() {
        assertFalse(shouldRefreshOnResume(lastHandledMs = 10_000, nowMs = 10_050))
    }

    @Test
    fun `resume after coming back from another screen refreshes`() {
        assertTrue(shouldRefreshOnResume(lastHandledMs = 10_000, nowMs = 25_000))
    }

    @Test
    fun `resume just past the gap refreshes`() {
        assertTrue(shouldRefreshOnResume(lastHandledMs = 10_000, nowMs = 10_000 + MIN_REFRESH_GAP_MS + 1))
    }

    @Test
    fun `clock that went backwards refreshes instead of skipping forever`() {
        assertTrue(shouldRefreshOnResume(lastHandledMs = 50_000, nowMs = 1_000))
    }
}
