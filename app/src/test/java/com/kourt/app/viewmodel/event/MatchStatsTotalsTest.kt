package com.kourt.app.viewmodel.event

/*
 * Tests for the season-total delta logic in MatchStatsViewModel.kt:
 *   - first save adds the whole match
 *   - re-saving the same match only applies what changed (no extra matchesPlayed)
 *   - a changed result moves from one outcome bucket to the other
 *   - a player dropped on edit is subtracted again
 */

import com.google.firebase.Timestamp
import com.kourt.app.data.model.MatchStats
import com.kourt.app.data.model.PlayerMatchStat
import com.kourt.app.data.model.PlayerStats
import com.kourt.app.data.model.TeamStats
import org.junit.Assert.assertEquals
import org.junit.Test

class MatchStatsTotalsTest {

    private val now = Timestamp(0, 0)

    private fun match(my: Int, opp: Int) = MatchStats(
        eventId = "e1",
        myTeamScore = my,
        opponentScore = opp,
        createdAt = now,
        updatedAt = now,
    )

    private val emptyTeam = TeamStats(teamId = "t1", updatedAt = now)
    private val emptyPlayer = PlayerStats(teamMemberId = "m1", teamId = "t1", updatedAt = now)

    @Test
    fun `first save adds the whole match`() {
        val result = emptyTeam.applyMatch(previous = null, current = match(80, 70), now = now)

        assertEquals(1, result.matchesPlayed)
        assertEquals(80, result.totalPointsScored)
        assertEquals(70, result.totalPointsAgainst)
        assertEquals(1, result.wins)
        assertEquals(0, result.losses)
    }

    @Test
    fun `re-saving the same match changes nothing`() {
        val saved = match(80, 70)
        val once = emptyTeam.applyMatch(null, saved, now)

        val twice = once.applyMatch(previous = saved, current = saved, now = now)

        assertEquals(once.copy(updatedAt = now), twice)
    }

    @Test
    fun `editing the score moves the result and applies only the difference`() {
        val loss = match(60, 70)
        val afterLoss = emptyTeam.applyMatch(null, loss, now)

        val afterEdit = afterLoss.applyMatch(previous = loss, current = match(75, 70), now = now)

        assertEquals(1, afterEdit.matchesPlayed)
        assertEquals(75, afterEdit.totalPointsScored)
        assertEquals(70, afterEdit.totalPointsAgainst)
        assertEquals(1, afterEdit.wins)
        assertEquals(0, afterEdit.losses)
        assertEquals(0, afterEdit.draws)
    }

    @Test
    fun `player re-save applies only the difference`() {
        val first = PlayerMatchStat(points = 10, rebounds = 4)
        val afterFirst = emptyPlayer.applyMatch(null, first, now)

        val afterEdit = afterFirst.applyMatch(previous = first, current = PlayerMatchStat(points = 12, rebounds = 4), now = now)

        assertEquals(1, afterEdit.matchesPlayed)
        assertEquals(12, afterEdit.totalPoints)
        assertEquals(4, afterEdit.totalRebounds)
    }

    @Test
    fun `player removed on edit is subtracted again`() {
        val line = PlayerMatchStat(points = 10, fouls = 2)
        val afterFirst = emptyPlayer.applyMatch(null, line, now)

        val afterRemoval = afterFirst.applyMatch(previous = line, current = null, now = now)

        assertEquals(0, afterRemoval.matchesPlayed)
        assertEquals(0, afterRemoval.totalPoints)
        assertEquals(0, afterRemoval.totalFouls)
    }
}
