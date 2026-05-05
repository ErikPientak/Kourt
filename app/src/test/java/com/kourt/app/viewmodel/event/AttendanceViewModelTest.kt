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
 */

import android.text.format.DateFormat
import androidx.lifecycle.SavedStateHandle
import com.google.common.truth.Truth.assertThat
import com.kourt.app.R
import com.kourt.app.data.model.Attendance
import com.kourt.app.data.model.Event
import com.kourt.app.data.model.Rsvp
import com.kourt.app.data.model.TeamMember
import com.kourt.app.data.model.User
import com.kourt.app.data.repository.remote.AttendanceRepository
import com.kourt.app.data.repository.remote.AuthRepository
import com.kourt.app.data.repository.remote.EventRepository
import com.kourt.app.data.repository.remote.PlayerStatsRepository
import com.kourt.app.data.repository.remote.RsvpRepository
import com.kourt.app.data.repository.remote.TeamMemberRepository
import com.kourt.app.data.repository.remote.TeamStatsRepository
import com.kourt.app.data.repository.remote.UserRepository
import com.kourt.app.util.MainCoroutineRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AttendanceViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val eventRepository: EventRepository = mockk(relaxed = true)
    private val rsvpRepository: RsvpRepository = mockk(relaxed = true)
    private val teamMemberRepository: TeamMemberRepository = mockk(relaxed = true)
    private val userRepository: UserRepository = mockk(relaxed = true)
    private val attendanceRepository: AttendanceRepository = mockk(relaxed = true)
    private val playerStatsRepository: PlayerStatsRepository = mockk(relaxed = true)
    private val teamStatsRepository: TeamStatsRepository = mockk(relaxed = true)

    @Before
    fun setUp() {
        // android.text.format.DateFormat is an Android framework class with no JVM impl.
        // Stub the only call the ViewModel makes against it.
        mockkStatic(DateFormat::class)
        every { DateFormat.getBestDateTimePattern(any(), any()) } returns "MMM d"
    }

    @After
    fun tearDown() {
        unmockkStatic(DateFormat::class)
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

        coEvery { eventRepository.getEvent(EVENT_ID) } returns Event(id = EVENT_ID, teamId = TEAM_ID)
        coEvery { attendanceRepository.getAttendanceByEvent(EVENT_ID) } returns emptyList()
        coEvery { rsvpRepository.getRsvpsByEvent(EVENT_ID) } returns rsvps
        coEvery { teamMemberRepository.getMembersByTeam(TEAM_ID) } returns members
        coEvery { userRepository.getUsersByIds(any()) } returns users

        // Act
        val vm = newViewModel()
        advanceUntilIdle()

        // Assert
        val statusByUserId = vm.uiState.members.associate { it.userId to it.status }
        assertThat(statusByUserId["u_yes"]).isEqualTo("on_time")
        assertThat(statusByUserId["u_late"]).isEqualTo("on_time")
        assertThat(statusByUserId["u_no_reason"]).isEqualTo("excused")
        assertThat(statusByUserId["u_no_blank"]).isEqualTo("unexcused")
        assertThat(statusByUserId["u_no_rsvp"]).isEqualTo("unexcused")
    }

    @Test
    fun `given an unknown RSVP status, then default attendance status is unexcused`() = runTest {
        // Arrange — exercise the else branch with an unrecognized status
        val members = listOf(
            TeamMember(id = "m1", userId = "u1", teamId = TEAM_ID, role = "player")
        )
        val users = listOf(User(id = "u1", displayName = "Alice"))
        val rsvps = listOf(
            Rsvp(eventId = EVENT_ID, status = "maybe", reason = "", submittedBy = "u1")
        )

        coEvery { eventRepository.getEvent(EVENT_ID) } returns Event(id = EVENT_ID, teamId = TEAM_ID)
        coEvery { attendanceRepository.getAttendanceByEvent(EVENT_ID) } returns emptyList()
        coEvery { rsvpRepository.getRsvpsByEvent(EVENT_ID) } returns rsvps
        coEvery { teamMemberRepository.getMembersByTeam(TEAM_ID) } returns members
        coEvery { userRepository.getUsersByIds(any()) } returns users

        // Act
        val vm = newViewModel()
        advanceUntilIdle()

        // Assert
        assertThat(vm.uiState.members.single().status).isEqualTo("unexcused")
    }

    // ── Existing attendance overrides RSVP default ────────────────────────────

    @Test
    fun `given an existing Attendance doc, then it overrides the RSVP-derived default`() = runTest {
        // Arrange — RSVP says "yes" (default would be "on_time"), but a saved
        // Attendance with status="late" already exists and must win.
        val members = listOf(
            TeamMember(id = "m1", userId = "u1", teamId = TEAM_ID, role = "player")
        )
        val users = listOf(User(id = "u1", displayName = "Alice"))
        val rsvps = listOf(Rsvp(eventId = EVENT_ID, status = "yes", submittedBy = "u1"))
        val attendance = listOf(Attendance(eventId = EVENT_ID, userId = "u1", status = "late"))

        coEvery { eventRepository.getEvent(EVENT_ID) } returns Event(id = EVENT_ID, teamId = TEAM_ID)
        coEvery { attendanceRepository.getAttendanceByEvent(EVENT_ID) } returns attendance
        coEvery { rsvpRepository.getRsvpsByEvent(EVENT_ID) } returns rsvps
        coEvery { teamMemberRepository.getMembersByTeam(TEAM_ID) } returns members
        coEvery { userRepository.getUsersByIds(any()) } returns users

        // Act
        val vm = newViewModel()
        advanceUntilIdle()

        // Assert
        assertThat(vm.uiState.members.single().status).isEqualTo("late")
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

        coEvery { eventRepository.getEvent(EVENT_ID) } returns Event(id = EVENT_ID, teamId = TEAM_ID)
        coEvery { attendanceRepository.getAttendanceByEvent(EVENT_ID) } returns emptyList()
        coEvery { rsvpRepository.getRsvpsByEvent(EVENT_ID) } returns emptyList()
        coEvery { teamMemberRepository.getMembersByTeam(TEAM_ID) } returns members
        coEvery { userRepository.getUsersByIds(any()) } returns users

        // Act
        val vm = newViewModel()
        advanceUntilIdle()

        // Assert
        val userIds = vm.uiState.members.map { it.userId }
        assertThat(userIds).containsExactly("u1", "u2")
    }

    // ── Error handling ────────────────────────────────────────────────────────

    @Test
    fun `given blank eventId, then error is error_load_failed and loading stops`() = runTest {
        // Arrange / Act — blank id short-circuits before any coroutine launches
        val vm = newViewModel(eventId = "")
        advanceUntilIdle()

        // Assert
        assertThat(vm.uiState.error).isEqualTo(R.string.error_load_failed)
        assertThat(vm.uiState.isLoading).isFalse()
    }

    @Test
    fun `given event lookup returns null, then error is error_load_failed`() = runTest {
        // Arrange
        coEvery { eventRepository.getEvent(EVENT_ID) } returns null
        coEvery { attendanceRepository.getAttendanceByEvent(EVENT_ID) } returns emptyList()
        coEvery { rsvpRepository.getRsvpsByEvent(EVENT_ID) } returns emptyList()

        // Act
        val vm = newViewModel()
        advanceUntilIdle()

        // Assert
        assertThat(vm.uiState.error).isEqualTo(R.string.error_load_failed)
        assertThat(vm.uiState.isLoading).isFalse()
    }

    private companion object {
        const val EVENT_ID = "event-1"
        const val TEAM_ID = "team-1"
    }
}
