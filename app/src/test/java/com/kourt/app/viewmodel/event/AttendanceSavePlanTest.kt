package com.kourt.app.viewmodel.event

/*
 * Tests for planAttendanceSave in AttendanceViewModel.kt:
 *   - first save records everyone and counts the training once
 *   - saving again unchanged writes nothing
 *   - a changed status moves the count from the old counter to the new one
 *   - a cleared status deletes the record and takes its count back off
 *   - clearing the last record takes the training back off
 *   - members without a team membership are skipped
 */

import com.google.firebase.Timestamp
import com.kourt.app.data.model.Attendance
import com.kourt.app.ui.screens.event.attendance.AttendanceMemberUiItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AttendanceSavePlanTest {

    private val now = Timestamp(0, 0)
    private val memberIds = mapOf("u1" to "m1", "u2" to "m2")

    private fun member(userId: String, status: String?) =
        AttendanceMemberUiItem(userId = userId, displayName = userId, avatarUrl = "", status = status)

    private fun record(userId: String, status: String) =
        Attendance(eventId = "e1", userId = userId, status = status, recordedBy = "coach", recordedAt = now)

    private fun plan(members: List<AttendanceMemberUiItem>, previous: Map<String, Attendance>) =
        planAttendanceSave("e1", members, previous, memberIds, uid = "coach", now = now)

    @Test
    fun `first save records everyone and counts the training once`() {
        val result = plan(listOf(member("u1", "on_time"), member("u2", "late")), previous = emptyMap())

        assertEquals(listOf("u1", "u2"), result.upserts.map { it.userId })
        assertTrue(result.deletes.isEmpty())
        assertEquals(
            mapOf("m1" to mapOf("totalTrainingsOnTime" to 1L), "m2" to mapOf("totalTrainingsLate" to 1L)),
            result.counterDeltas,
        )
        assertEquals(1L, result.trainingsDelta)
    }

    @Test
    fun `saving again unchanged writes nothing`() {
        val previous = mapOf("u1" to record("u1", "on_time"))

        val result = plan(listOf(member("u1", "on_time")), previous)

        assertTrue(result.upserts.isEmpty())
        assertTrue(result.deletes.isEmpty())
        assertTrue(result.counterDeltas.isEmpty())
        assertEquals(0L, result.trainingsDelta)
        assertEquals(previous, result.savedAfter)
    }

    @Test
    fun `a changed status moves the count to the new counter`() {
        val previous = mapOf("u1" to record("u1", "unexcused"))

        val result = plan(listOf(member("u1", "excused")), previous)

        assertEquals("excused", result.upserts.single().status)
        assertEquals("coach", result.upserts.single().updatedBy)
        assertEquals(
            mapOf("m1" to mapOf("totalTrainingsUnexcused" to -1L, "totalTrainingsExcused" to 1L)),
            result.counterDeltas,
        )
        assertEquals(0L, result.trainingsDelta)
    }

    @Test
    fun `a cleared status deletes the record and takes its count off`() {
        val previous = mapOf("u1" to record("u1", "late"), "u2" to record("u2", "on_time"))

        val result = plan(listOf(member("u1", null), member("u2", "on_time")), previous)

        assertEquals(listOf("u1"), result.deletes.map { it.userId })
        assertTrue(result.upserts.isEmpty())
        assertEquals(mapOf("m1" to mapOf("totalTrainingsLate" to -1L)), result.counterDeltas)
        assertEquals(setOf("u2"), result.savedAfter.keys)
        assertEquals(0L, result.trainingsDelta)
    }

    @Test
    fun `clearing the last record takes the training back off`() {
        val previous = mapOf("u1" to record("u1", "on_time"))

        val result = plan(listOf(member("u1", null)), previous)

        assertEquals(-1L, result.trainingsDelta)
        assertTrue(result.savedAfter.isEmpty())
    }

    @Test
    fun `members without a team membership are skipped`() {
        val result = plan(listOf(member("u9", "on_time")), previous = emptyMap())

        assertTrue(result.upserts.isEmpty())
        assertTrue(result.counterDeltas.isEmpty())
        assertEquals(0L, result.trainingsDelta)
    }
}
