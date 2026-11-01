package com.kourt.app.viewmodel.event

/*
 * Tests for applyStatChange in MatchStatsViewModel.kt (the match stats steppers):
 *   - a made free throw adds a point and an attempt
 *   - removing a made free throw at 0 changes nothing, including points
 *   - attempted can't go below made
 *   - fouls stay between 0 and 5
 */

import com.kourt.app.ui.screens.event.matchstats.PlayerStatEntryUiItem
import com.kourt.app.ui.screens.event.matchstats.StatField
import org.junit.Assert.assertEquals
import org.junit.Test

class StatStepperTest {

    private val player = PlayerStatEntryUiItem(
        userId = "u1", teamMemberId = "m1", displayName = "Alice", avatarUrl = "", jerseyNumber = 7,
    )

    @Test
    fun `a made free throw adds a point and an attempt`() {
        val result = player.applyStatChange(StatField.FREE_THROWS_MADE, 1)

        assertEquals(1, result.freeThrowsMade)
        assertEquals(1, result.freeThrowsAttempted)
        assertEquals(1, result.points)
    }

    @Test
    fun `removing a made free throw at zero changes nothing`() {
        val start = player.copy(points = 4)

        val result = start.applyStatChange(StatField.FREE_THROWS_MADE, -1)

        assertEquals(start, result)
    }

    @Test
    fun `removing a made free throw keeps the attempt and takes the point off`() {
        val start = player.copy(points = 5, freeThrowsMade = 1, freeThrowsAttempted = 2)

        val result = start.applyStatChange(StatField.FREE_THROWS_MADE, -1)

        assertEquals(0, result.freeThrowsMade)
        assertEquals(2, result.freeThrowsAttempted)
        assertEquals(4, result.points)
    }

    @Test
    fun `attempted can't go below made`() {
        val start = player.copy(points = 2, freeThrowsMade = 2, freeThrowsAttempted = 2)

        val result = start.applyStatChange(StatField.FREE_THROWS_ATTEMPTED, -1)

        assertEquals(2, result.freeThrowsAttempted)
    }

    @Test
    fun `fouls stay between 0 and 5`() {
        assertEquals(0, player.applyStatChange(StatField.FOULS, -1).fouls)
        assertEquals(5, player.copy(fouls = 5).applyStatChange(StatField.FOULS, 1).fouls)
    }
}
