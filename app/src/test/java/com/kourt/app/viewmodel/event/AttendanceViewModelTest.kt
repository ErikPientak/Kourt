package com.kourt.app.viewmodel.event

/*
 * Tests for [AttendanceViewModel] focused on the rsvpDefaultStatus mapping:
 *   "yes"  -> "on_time"
 *   "late" -> "on_time"
 *   "no" + non-blank reason -> "excused"
 *   "no" + blank reason     -> "unexcused"
 *   null / unknown          -> "unexcused"
 *
 * The mapping is private; we exercise it through loadAttendance() by stubbing
 * one RSVP per member and reading uiState.members[*].status.
 *
 * Also covers:
 *   - blank eventId emits error_load_failed without launching coroutines
 *   - existing Attendance docs win over RSVP defaults
 *   - members not in {player, parent} are filtered out
 *
 * Note: android.text.format.DateFormat.getBestDateTimePattern returns null on the
 * JVM (Android stub jar + isReturnDefaultValues=true). The production code
 * falls back to "MMM d" via the elvis operator, so no static mocking is required.
 */

import androidx.lifecycle.SavedStateHandle
import com.kourt.app.R
import com.kourt.app.data.model.Attendance
import com.kourt.app.data.model.Event
import com.kourt.app.data.model.Rsvp
import com.kourt.app.data.model.TeamMember
import com.kourt.app.data.model.User
import com.kourt.app.util.FakeAttendanceRepository
import com.kourt.app.util.FakeAuthRepository
import com.kourt.app.util.FakeEventRepository
import com.kourt.app.util.FakePlayerStatsRepository
import com.kourt.app.util.FakeRsvpRepository
import com.kourt.app.util.FakeTeamMemberRepository
import com.kourt.app.util.FakeTeamStatsRepository
import com.kourt.app.util.FakeUserRepository
import com.kourt.app.util.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse

@OptIn(ExperimentalCoroutinesApi::class)
class AttendanceViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var authRepository: FakeAuthRepository
    private lateinit var eventRepository: FakeEventRepository
    private lateinit var rsvpRepository: FakeRsvpRepository
    private lateinit var teamMemberRepository: FakeTeamMemberRepository
    private lateinit var userRepository: FakeUserRepository
    private lateinit var attendanceRepository: FakeAttendanceRepository
    private lateinit var playerStatsRepository: FakePlayerStatsRepository
    private lateinit var teamStatsRepository: FakeTeamStatsRepository

    @Before
    fun setUp() {
        authRepository = FakeAuthRepository.create()
        eventRepository = FakeEventRepository.create()
        rsvpRepository = FakeRsvpRepository.create()
        teamMemberRepository = FakeTeamMemberRepository.create()
        userRepository = FakeUserRepository.create()
        attendanceRepository = FakeAttendanceRepository.create()
        playerStatsRepository = FakePlayerStatsRepository.create()
        teamStatsRepository = FakeTeamStatsRepository.create()
    }

    private fun newViewModel(eventId: String = EVENT_ID): AttendanceViewModel =
        AttendanceViewModel(
            savedStateHandle = SavedStateHandle(mapOf("eventId" to eventId)),
            authRepository = authRepository,
            eventRepository = eventRepository,
            rsvpRepository = rsvpRepository,
            teamMemberRepository = teamMemberRepository,
            userRepository = userRepository,
            attendanceRepository = attendanceRepository,
            playerStatsRepository = playerStatsRepository,
            teamStatsRepository = teamStatsRepository,
        )

    // ── rsvpDefaultStatus mapping ─────────────────────────────────────────────

    @Test
    fun `given RSVPs covering all status branches, then default attendance status is mapped correctly`() = runTest {
        // Arrange — five members, one per branch
        val members = listOf(
            TeamMember(id = "m_yes",       userId = "u_yes",       teamId = TEAM_ID, role = "player"),
            TeamMember(id = "m_late",      userId = "u_late",      teamId = TEAM_ID, role = "player"),
            TeamMember(id = "m_no_reason", userId = "u_no_reason", teamId = TEAM_ID, role = "player"),
            TeamMember(id = "m_no_blank",  userId = "u_no_blank",  teamId = TEAM_ID, role = "player"),
            TeamMember(id = "m_no_rsvp",   userId = "u_no_rsvp",   teamId = TEAM_ID, role = "player"),
        )
        val users = members.map { User(id = it.userId, displayName = it.userId) }
        val rsvps = listOf(
            Rsvp(eventId = EVENT_ID, status = "yes",  reason = "",        submittedBy = "u_yes"),
            Rsvp(eventId = EVENT_ID, status = "late", reason = "",        submittedBy = "u_late"),
            Rsvp(eventId = EVENT_ID, status = "no",   reason = "injured", submittedBy = "u_no_reason"),
            Rsvp(eventId = EVENT_ID, status = "no",   reason = "",        submittedBy = "u_no_blank"),
            // u_no_rsvp intentionally has no RSVP doc
        )

        eventRepository.event = Event(id = EVENT_ID, teamId = TEAM_ID)
        attendanceRepository.attendance = emptyList()
        rsvpRepository.rsvps = rsvps
        teamMemberRepository.membersByTeam = members
        userRepository.users = users

        // Act
        val vm = newViewModel()
        advanceUntilIdle()

        // Assert
        val statusByUserId = vm.uiState.members.associate { it.userId to it.status }
        assertEquals("on_time",   statusByUserId["u_yes"])
        assertEquals("on_time",   statusByUserId["u_late"])
        assertEquals("excused",   statusByUserId["u_no_reason"])
        assertEquals("unexcused", statusByUserId["u_no_blank"])
        assertEquals("unexcused", statusByUserId["u_no_rsvp"])
    }

    @Test
    fun `given an unknown RSVP status, then default attendance status is unexcused`() = runTest {
        // Arrange — exercise the else branch with an unrecognized status
        val members = listOf(TeamMember(id = "m1", userId = "u1", teamId = TEAM_ID, role = "player"))
        val users = listOf(User(id = "u1", displayName = "Alice"))
        val rsvps = listOf(Rsvp(eventId = EVENT_ID, status = "maybe", reason = "", submittedBy = "u1"))

        eventRepository.event = Event(id = EVENT_ID, teamId = TEAM_ID)
        attendanceRepository.attendance = emptyList()
        rsvpRepository.rsvps = rsvps
        teamMemberRepository.membersByTeam = members
        userRepository.users = users

        // Act
        val vm = newViewModel()
        advanceUntilIdle()

        // Assert
        assertEquals("unexcused", vm.uiState.members.single().status)
    }

    // ── Existing attendance overrides RSVP default ────────────────────────────

    @Test
    fun `given an existing Attendance doc, then it overrides the RSVP-derived default`() = runTest {
        // Arrange — RSVP says "yes" (default would be "on_time"), but a saved
        // Attendance with status="late" already exists and must win.
        val members = listOf(TeamMember(id = "m1", userId = "u1", teamId = TEAM_ID, role = "player"))
        val users = listOf(User(id = "u1", displayName = "Alice"))
        val rsvps = listOf(Rsvp(eventId = EVENT_ID, status = "yes", submittedBy = "u1"))
        val attendance = listOf(Attendance(eventId = EVENT_ID, userId = "u1", status = "late"))

        eventRepository.event = Event(id = EVENT_ID, teamId = TEAM_ID)
        attendanceRepository.attendance = attendance
        rsvpRepository.rsvps = rsvps
        teamMemberRepository.membersByTeam = members
        userRepository.users = users

        // Act
        val vm = newViewModel()
        advanceUntilIdle()

        // Assert
        assertEquals("late", vm.uiState.members.single().status)
    }

    // ── Filtering ─────────────────────────────────────────────────────────────

    @Test
    fun `given members with non-player roles, then only player and parent remain`() = runTest {
        // Arrange
        val members = listOf(
            TeamMember(id = "m1", userId = "u1", teamId = TEAM_ID, role = "player"),
            TeamMember(id = "m2", userId = "u2", teamId = TEAM_ID, role = "parent"),
            TeamMember(id = "m3", userId = "u3", teamId = TEAM_ID, role = "coach"),
            TeamMember(id = "m4", userId = "u4", teamId = TEAM_ID, role = "assistant"),
        )
        val users = members.map { User(id = it.userId, displayName = it.userId) }

        eventRepository.event = Event(id = EVENT_ID, teamId = TEAM_ID)
        attendanceRepository.attendance = emptyList()
        rsvpRepository.rsvps = emptyList()
        teamMemberRepository.membersByTeam = members
        userRepository.users = users

        // Act
        val vm = newViewModel()
        advanceUntilIdle()

        // Assert
        val userIds = vm.uiState.members.map { it.userId }
        assertEquals(listOf("u1", "u2"), userIds)
    }

    // ── Error handling ────────────────────────────────────────────────────────

    @Test
    fun `given blank eventId, then error is error_load_failed and loading stops`() = runTest {
        // Arrange / Act — blank id short-circuits before any coroutine launches
        val vm = newViewModel(eventId = "")
        advanceUntilIdle()

        // Assert
        assertEquals(R.string.error_load_failed, vm.uiState.error)
        assertFalse(vm.uiState.isLoading)
    }

    @Test
    fun `given event lookup returns null, then error is error_load_failed`() = runTest {
        // Arrange
        eventRepository.event = null
        attendanceRepository.attendance = emptyList()
        rsvpRepository.rsvps = emptyList()

        // Act
        val vm = newViewModel()
        advanceUntilIdle()

        // Assert
        assertEquals(R.string.error_load_failed, vm.uiState.error)
        assertFalse(vm.uiState.isLoading)
    }

    private companion object {
        const val EVENT_ID = "event-1"
        const val TEAM_ID = "team-1"
    }
}
