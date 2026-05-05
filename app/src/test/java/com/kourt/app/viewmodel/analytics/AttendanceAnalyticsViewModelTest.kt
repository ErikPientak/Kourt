package com.kourt.app.viewmodel.analytics

/*
 * Tests for [AttendanceAnalyticsViewModel].
 *
 * Covers:
 *  - Per-player attendance rate formula:  (on_time + late) / total
 *  - Reliability buckets:
 *      * >= 0.9  -> EXCELLENT
 *      * >= 0.7  -> CONSISTENT
 *      *  < 0.7  -> NEEDS ATTENTION
 *  - Trend direction (isTrendImproving):
 *      * latest week rate > previous -> Improving (true)
 *      * latest week rate < previous -> Declining (false)
 *  - Edge cases: zero stats player skipped, blank teamId emits error, generic failure path.
 *
 * Note: the ViewModel calls loadAnalytics() from init, so we must stub *every* repository
 * before constructing the VM.
 */

import com.google.common.truth.Truth.assertThat
import com.google.firebase.Timestamp
import com.kourt.app.R
import com.kourt.app.data.model.Event
import com.kourt.app.data.model.PlayerStats
import com.kourt.app.data.model.TeamMember
import com.kourt.app.data.model.User
import com.kourt.app.data.repository.local.AppPreferencesRepository
import com.kourt.app.data.repository.remote.AttendanceRepository
import com.kourt.app.data.repository.remote.EventRepository
import com.kourt.app.data.repository.remote.PlayerStatsRepository
import com.kourt.app.data.repository.remote.TeamMemberRepository
import com.kourt.app.data.repository.remote.UserRepository
import com.kourt.app.util.MainCoroutineRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
class AttendanceAnalyticsViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private val appPrefs: AppPreferencesRepository = mockk(relaxed = true)
    private val eventRepository: EventRepository = mockk(relaxed = true)
    private val attendanceRepository: AttendanceRepository = mockk(relaxed = true)
    private val playerStatsRepository: PlayerStatsRepository = mockk(relaxed = true)
    private val teamMemberRepository: TeamMemberRepository = mockk(relaxed = true)
    private val userRepository: UserRepository = mockk(relaxed = true)

    private fun newViewModel() = AttendanceAnalyticsViewModel(
        appPreferencesRepository = appPrefs,
        eventRepository = eventRepository,
        attendanceRepository = attendanceRepository,
        playerStatsRepository = playerStatsRepository,
        teamMemberRepository = teamMemberRepository,
        userRepository = userRepository,
    )

    private fun stubBase(
        teamId: String = TEAM_ID,
        stats: List<PlayerStats> = emptyList(),
        members: List<TeamMember> = emptyList(),
        events: List<Event> = emptyList(),
        users: List<User> = emptyList(),
    ) {
        every { appPrefs.activeTeamId } returns teamId
        coEvery { playerStatsRepository.getPlayersStatsByTeam(any()) } returns stats
        coEvery { teamMemberRepository.getMembersByTeam(any()) } returns members
        coEvery { eventRepository.getEventsByTeam(any()) } returns events
        coEvery { userRepository.getUsersByIds(any()) } returns users
        coEvery { attendanceRepository.getAttendanceByEvent(any()) } returns emptyList()
    }

    // ── Reliability bucket boundaries ─────────────────────────────────────────

    @Test
    fun `given attendance rate is 0_9, then reliability is EXCELLENT`() = runTest {
        // Arrange  - 9 on_time, 0 late, 1 unexcused -> rate = 9/10 = 0.9
        val members = listOf(TeamMember(id = "m1", userId = "u1", teamId = TEAM_ID, role = "player"))
        val stats = listOf(
            PlayerStats(
                teamMemberId = "m1",
                teamId = TEAM_ID,
                totalTrainingsOnTime = 9,
                totalTrainingsLate = 0,
                totalTrainingsExcused = 0,
                totalTrainingsUnexcused = 1,
            )
        )
        val users = listOf(User(id = "u1", displayName = "Alice"))
        stubBase(stats = stats, members = members, users = users)

        // Act
        val vm = newViewModel()
        advanceUntilIdle()

        // Assert
        val player = vm.uiState.players.single()
        assertThat(player.attendanceRate).isWithin(EPS).of(0.9f)
        assertThat(player.reliabilityLabel).isEqualTo("EXCELLENT")
    }

    @Test
    fun `given attendance rate is 0_7, then reliability is CONSISTENT`() = runTest {
        // Arrange — 6 on_time, 1 late, 3 unexcused -> rate = 7/10 = 0.7
        val members = listOf(TeamMember(id = "m1", userId = "u1", teamId = TEAM_ID, role = "player"))
        val stats = listOf(
            PlayerStats(
                teamMemberId = "m1",
                teamId = TEAM_ID,
                totalTrainingsOnTime = 6,
                totalTrainingsLate = 1,
                totalTrainingsExcused = 0,
                totalTrainingsUnexcused = 3,
            )
        )
        val users = listOf(User(id = "u1", displayName = "Bob"))
        stubBase(stats = stats, members = members, users = users)

        // Act
        val vm = newViewModel()
        advanceUntilIdle()

        // Assert
        val player = vm.uiState.players.single()
        assertThat(player.attendanceRate).isWithin(EPS).of(0.7f)
        assertThat(player.reliabilityLabel).isEqualTo("CONSISTENT")
    }

    @Test
    fun `given attendance rate is below 0_7, then reliability is NEEDS ATTENTION`() = runTest {
        // Arrange — 5 on_time, 0 late, 5 unexcused -> rate = 0.5
        val members = listOf(TeamMember(id = "m1", userId = "u1", teamId = TEAM_ID, role = "player"))
        val stats = listOf(
            PlayerStats(
                teamMemberId = "m1",
                teamId = TEAM_ID,
                totalTrainingsOnTime = 5,
                totalTrainingsLate = 0,
                totalTrainingsExcused = 0,
                totalTrainingsUnexcused = 5,
            )
        )
        val users = listOf(User(id = "u1", displayName = "Carol"))
        stubBase(stats = stats, members = members, users = users)

        // Act
        val vm = newViewModel()
        advanceUntilIdle()

        // Assert
        val player = vm.uiState.players.single()
        assertThat(player.attendanceRate).isWithin(EPS).of(0.5f)
        assertThat(player.reliabilityLabel).isEqualTo("NEEDS ATTENTION")
    }

    // ── Attendance rate formula ───────────────────────────────────────────────

    @Test
    fun `given on_time and late counts, then attendance rate is sum over total`() = runTest {
        // Arrange — 8 on_time + 2 late + 5 excused + 5 unexcused -> 10/20 = 0.5
        val members = listOf(TeamMember(id = "m1", userId = "u1", teamId = TEAM_ID, role = "player"))
        val stats = listOf(
            PlayerStats(
                teamMemberId = "m1",
                teamId = TEAM_ID,
                totalTrainingsOnTime = 8,
                totalTrainingsLate = 2,
                totalTrainingsExcused = 5,
                totalTrainingsUnexcused = 5,
            )
        )
        val users = listOf(User(id = "u1", displayName = "Dan"))
        stubBase(stats = stats, members = members, users = users)

        // Act
        val vm = newViewModel()
        advanceUntilIdle()

        // Assert
        val player = vm.uiState.players.single()
        assertThat(player.attendanceRate).isWithin(EPS).of(0.5f)
        assertThat(player.onTimeRate).isWithin(EPS).of(0.4f)   // 8/20
        assertThat(player.lateRate).isWithin(EPS).of(0.1f)     // 2/20
        assertThat(player.absentRate).isWithin(EPS).of(0.5f)   // (5+5)/20
    }

    @Test
    fun `given a player with zero total stats, then they are excluded from results`() = runTest {
        // Arrange — total == 0 -> mapNotNull skips
        val members = listOf(TeamMember(id = "m1", userId = "u1", teamId = TEAM_ID, role = "player"))
        val stats = listOf(PlayerStats(teamMemberId = "m1", teamId = TEAM_ID))
        val users = listOf(User(id = "u1", displayName = "NoStats"))
        stubBase(stats = stats, members = members, users = users)

        // Act
        val vm = newViewModel()
        advanceUntilIdle()

        // Assert
        assertThat(vm.uiState.players).isEmpty()
        assertThat(vm.uiState.mostReliablePlayer).isNull()
    }

    // ── Trend direction ───────────────────────────────────────────────────────

    @Test
    fun `given last week rate exceeds previous, then trend is improving`() = runTest {
        // Arrange — two events in the most recent week with both members on_time (rate 1.0)
        // and one event in the previous week with no attendance (rate 0.0)
        val today = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())
        val previousWeekEpochDay = today - 8L     // last week bucket
        val currentWeekEpochDay = today           // current week bucket
        val members = listOf(
            TeamMember(id = "m1", userId = "u1", teamId = TEAM_ID, role = "player"),
            TeamMember(id = "m2", userId = "u2", teamId = TEAM_ID, role = "player"),
        )
        val users = listOf(User(id = "u1"), User(id = "u2"))
        val events = listOf(
            Event(id = "e_prev", teamId = TEAM_ID, date = epochDayToTimestamp(previousWeekEpochDay)),
            Event(id = "e_curr", teamId = TEAM_ID, date = epochDayToTimestamp(currentWeekEpochDay)),
        )

        every { appPrefs.activeTeamId } returns TEAM_ID
        coEvery { playerStatsRepository.getPlayersStatsByTeam(any()) } returns emptyList()
        coEvery { teamMemberRepository.getMembersByTeam(any()) } returns members
        coEvery { eventRepository.getEventsByTeam(any()) } returns events
        coEvery { userRepository.getUsersByIds(any()) } returns users
        coEvery { attendanceRepository.getAttendanceByEvent("e_prev") } returns emptyList()
        coEvery { attendanceRepository.getAttendanceByEvent("e_curr") } returns listOf(
            com.kourt.app.data.model.Attendance(eventId = "e_curr", userId = "u1", status = "on_time"),
            com.kourt.app.data.model.Attendance(eventId = "e_curr", userId = "u2", status = "on_time"),
        )

        // Act
        val vm = newViewModel()
        advanceUntilIdle()

        // Assert — trend bars should have at least 2; current week rate (1.0) > previous (0.0)
        assertThat(vm.uiState.trendBars.size).isAtLeast(2)
        assertThat(vm.uiState.isTrendImproving).isTrue()
        assertThat(vm.uiState.trendDelta).isGreaterThan(0f)
    }

    @Test
    fun `given last week rate is below previous, then trend is declining`() = runTest {
        // Arrange — previous week full attendance, current week zero attendance
        val today = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())
        val previousWeekEpochDay = today - 8L
        val currentWeekEpochDay = today
        val members = listOf(
            TeamMember(id = "m1", userId = "u1", teamId = TEAM_ID, role = "player"),
            TeamMember(id = "m2", userId = "u2", teamId = TEAM_ID, role = "player"),
        )
        val users = listOf(User(id = "u1"), User(id = "u2"))
        val events = listOf(
            Event(id = "e_prev", teamId = TEAM_ID, date = epochDayToTimestamp(previousWeekEpochDay)),
            Event(id = "e_curr", teamId = TEAM_ID, date = epochDayToTimestamp(currentWeekEpochDay)),
        )

        every { appPrefs.activeTeamId } returns TEAM_ID
        coEvery { playerStatsRepository.getPlayersStatsByTeam(any()) } returns emptyList()
        coEvery { teamMemberRepository.getMembersByTeam(any()) } returns members
        coEvery { eventRepository.getEventsByTeam(any()) } returns events
        coEvery { userRepository.getUsersByIds(any()) } returns users
        coEvery { attendanceRepository.getAttendanceByEvent("e_prev") } returns listOf(
            com.kourt.app.data.model.Attendance(eventId = "e_prev", userId = "u1", status = "on_time"),
            com.kourt.app.data.model.Attendance(eventId = "e_prev", userId = "u2", status = "on_time"),
        )
        coEvery { attendanceRepository.getAttendanceByEvent("e_curr") } returns emptyList()

        // Act
        val vm = newViewModel()
        advanceUntilIdle()

        // Assert
        assertThat(vm.uiState.trendBars.size).isAtLeast(2)
        assertThat(vm.uiState.isTrendImproving).isFalse()
        assertThat(vm.uiState.trendDelta).isLessThan(0f)
    }

    // ── Failure / edge cases ──────────────────────────────────────────────────

    @Test
    fun `given no active team, then error is error_no_team`() = runTest {
        // Arrange
        every { appPrefs.activeTeamId } returns ""

        // Act
        val vm = newViewModel()
        advanceUntilIdle()

        // Assert
        assertThat(vm.uiState.error).isEqualTo(R.string.error_no_team)
        assertThat(vm.uiState.isLoading).isFalse()
    }

    @Test
    fun `given a repository fails, then error is error_generic`() = runTest {
        // Arrange
        every { appPrefs.activeTeamId } returns TEAM_ID
        coEvery { playerStatsRepository.getPlayersStatsByTeam(any()) } throws RuntimeException("boom")
        coEvery { teamMemberRepository.getMembersByTeam(any()) } returns emptyList()
        coEvery { eventRepository.getEventsByTeam(any()) } returns emptyList()

        // Act
        val vm = newViewModel()
        advanceUntilIdle()

        // Assert
        assertThat(vm.uiState.error).isEqualTo(R.string.error_generic)
        assertThat(vm.uiState.isLoading).isFalse()
    }

    private fun epochDayToTimestamp(epochDay: Long): Timestamp =
        Timestamp(TimeUnit.DAYS.toSeconds(epochDay), 0)

    private companion object {
        const val TEAM_ID = "team-1"
        const val EPS = 0.0001f
    }
}
