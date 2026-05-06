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
 *  - Edge cases: zero-stats player skipped, blank teamId emits error,
 *    generic failure path on repo throw.
 *
 * Note: the ViewModel calls loadAnalytics() from init, so we set every fake
 * field BEFORE constructing the VM.
 */

import com.google.firebase.Timestamp
import com.kourt.app.R
import com.kourt.app.data.model.Attendance
import com.kourt.app.data.model.Event
import com.kourt.app.data.model.PlayerStats
import com.kourt.app.data.model.TeamMember
import com.kourt.app.data.model.User
import com.kourt.app.util.FakeAppPreferencesRepository
import com.kourt.app.util.FakeAttendanceRepository
import com.kourt.app.util.FakeEventRepository
import com.kourt.app.util.FakePlayerStatsRepository
import com.kourt.app.util.FakeTeamMemberRepository
import com.kourt.app.util.FakeUserRepository
import com.kourt.app.util.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.TimeUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AttendanceAnalyticsViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var appPrefs: FakeAppPreferencesRepository
    private lateinit var eventRepository: FakeEventRepository
    private lateinit var attendanceRepository: FakeAttendanceRepository
    private lateinit var playerStatsRepository: FakePlayerStatsRepository
    private lateinit var teamMemberRepository: FakeTeamMemberRepository
    private lateinit var userRepository: FakeUserRepository

    @Before
    fun setUp() {
        appPrefs = FakeAppPreferencesRepository.create()
        eventRepository = FakeEventRepository.create()
        attendanceRepository = FakeAttendanceRepository.create()
        playerStatsRepository = FakePlayerStatsRepository.create()
        teamMemberRepository = FakeTeamMemberRepository.create()
        userRepository = FakeUserRepository.create()
    }

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
        appPrefs.activeTeamId = teamId
        playerStatsRepository.playerStats = stats
        teamMemberRepository.membersByTeam = members
        eventRepository.eventsByTeam = events
        userRepository.users = users
        attendanceRepository.attendance = emptyList()
    }

    // ── Reliability bucket boundaries ─────────────────────────────────────────

    @Test
    fun `given attendance rate is 0_9, then reliability is EXCELLENT`() = runTest {
        // Arrange — 9 on_time, 0 late, 1 unexcused -> rate = 9/10 = 0.9
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
        assertNear(0.9f, player.attendanceRate)
        assertEquals("EXCELLENT", player.reliabilityLabel)
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
        assertNear(0.7f, player.attendanceRate)
        assertEquals("CONSISTENT", player.reliabilityLabel)
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
        assertNear(0.5f, player.attendanceRate)
        assertEquals("NEEDS ATTENTION", player.reliabilityLabel)
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
        assertNear(0.5f, player.attendanceRate)
        assertNear(0.4f, player.onTimeRate)   // 8/20
        assertNear(0.1f, player.lateRate)     // 2/20
        assertNear(0.5f, player.absentRate)   // (5+5)/20
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
        assertTrue(vm.uiState.players.isEmpty())
        assertNull(vm.uiState.mostReliablePlayer)
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

        appPrefs.activeTeamId = TEAM_ID
        playerStatsRepository.playerStats = emptyList()
        teamMemberRepository.membersByTeam = members
        eventRepository.eventsByTeam = events
        userRepository.users = users
        attendanceRepository.attendanceByEvent = mapOf(
            "e_prev" to emptyList(),
            "e_curr" to listOf(
                Attendance(eventId = "e_curr", userId = "u1", status = "on_time"),
                Attendance(eventId = "e_curr", userId = "u2", status = "on_time"),
            ),
        )

        // Act
        val vm = newViewModel()
        advanceUntilIdle()

        // Assert — trend bars should have at least 2; current week rate (1.0) > previous (0.0)
        assertTrue("expected >= 2 trend bars, got ${vm.uiState.trendBars.size}", vm.uiState.trendBars.size >= 2)
        assertTrue(vm.uiState.isTrendImproving)
        assertTrue("expected trendDelta > 0, got ${vm.uiState.trendDelta}", vm.uiState.trendDelta > 0f)
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

        appPrefs.activeTeamId = TEAM_ID
        playerStatsRepository.playerStats = emptyList()
        teamMemberRepository.membersByTeam = members
        eventRepository.eventsByTeam = events
        userRepository.users = users
        attendanceRepository.attendanceByEvent = mapOf(
            "e_prev" to listOf(
                Attendance(eventId = "e_prev", userId = "u1", status = "on_time"),
                Attendance(eventId = "e_prev", userId = "u2", status = "on_time"),
            ),
            "e_curr" to emptyList(),
        )

        // Act
        val vm = newViewModel()
        advanceUntilIdle()

        // Assert
        assertTrue("expected >= 2 trend bars, got ${vm.uiState.trendBars.size}", vm.uiState.trendBars.size >= 2)
        assertFalse(vm.uiState.isTrendImproving)
        assertTrue("expected trendDelta < 0, got ${vm.uiState.trendDelta}", vm.uiState.trendDelta < 0f)
    }

    // ── Failure / edge cases ──────────────────────────────────────────────────

    @Test
    fun `given no active team, then error is error_no_team`() = runTest {
        // Arrange
        appPrefs.activeTeamId = ""

        // Act
        val vm = newViewModel()
        advanceUntilIdle()

        // Assert
        assertEquals(R.string.error_no_team, vm.uiState.error)
        assertFalse(vm.uiState.isLoading)
    }

    @Test
    fun `given a repository fails, then error is error_generic`() = runTest {
        // Arrange
        appPrefs.activeTeamId = TEAM_ID
        playerStatsRepository.failOnGetPlayersStatsByTeam = true
        teamMemberRepository.membersByTeam = emptyList()
        eventRepository.eventsByTeam = emptyList()

        // Act
        val vm = newViewModel()
        advanceUntilIdle()

        // Assert
        assertEquals(R.string.error_generic, vm.uiState.error)
        assertFalse(vm.uiState.isLoading)
    }

    private fun epochDayToTimestamp(epochDay: Long): Timestamp =
        Timestamp(TimeUnit.DAYS.toSeconds(epochDay), 0)

    private fun assertNear(expected: Float, actual: Float, eps: Float = EPS) {
        assertEquals(expected.toDouble(), actual.toDouble(), eps.toDouble())
    }

    private companion object {
        const val TEAM_ID = "team-1"
        const val EPS = 0.0001f
    }
}
