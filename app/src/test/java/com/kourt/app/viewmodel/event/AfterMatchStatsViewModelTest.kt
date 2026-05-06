package com.kourt.app.viewmodel.event

/*
 * Tests for [AfterMatchStatsViewModel].
 *
 * Covers:
 *  - MVP selection: highest (points + rebounds + assists)
 *  - isWin = myTeamScore > opponentScore (strict — ties are NOT wins)
 *  - Players are sorted by score descending
 *  - Players whose member or user is missing are dropped
 *  - opponentName falls back to event title when opponent is blank
 *  - Failure paths: null event / null matchStats / blank eventId / repo throws
 */

import androidx.lifecycle.SavedStateHandle
import com.kourt.app.R
import com.kourt.app.data.model.Event
import com.kourt.app.data.model.MatchStats
import com.kourt.app.data.model.PlayerMatchStat
import com.kourt.app.data.model.TeamMember
import com.kourt.app.data.model.User
import com.kourt.app.util.FakeEventRepository
import com.kourt.app.util.FakeMatchStatsRepository
import com.kourt.app.util.FakeTeamMemberRepository
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AfterMatchStatsViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var eventRepository: FakeEventRepository
    private lateinit var matchStatsRepository: FakeMatchStatsRepository
    private lateinit var teamMemberRepository: FakeTeamMemberRepository
    private lateinit var userRepository: FakeUserRepository

    @Before
    fun setUp() {
        eventRepository = FakeEventRepository.create()
        matchStatsRepository = FakeMatchStatsRepository.create()
        teamMemberRepository = FakeTeamMemberRepository.create()
        userRepository = FakeUserRepository.create()
    }

    private fun newViewModel(eventId: String = EVENT_ID): AfterMatchStatsViewModel =
        AfterMatchStatsViewModel(
            savedStateHandle = SavedStateHandle(mapOf("eventId" to eventId)),
            eventRepository = eventRepository,
            matchStatsRepository = matchStatsRepository,
            teamMemberRepository = teamMemberRepository,
            userRepository = userRepository,
        )

    private fun stubScreen(
        event: Event? = Event(id = EVENT_ID, teamId = TEAM_ID, title = "Game", opponent = "Foes"),
        matchStats: MatchStats? = null,
        members: List<TeamMember> = emptyList(),
        users: List<User> = emptyList(),
    ) {
        eventRepository.event = event
        matchStatsRepository.matchStats = matchStats
        teamMemberRepository.membersByTeam = members
        userRepository.users = users
    }

    // ── MVP selection ─────────────────────────────────────────────────────────

    @Test
    fun `given several players, then MVP has highest points plus rebounds plus assists`() = runTest {
        // Arrange
        // m1: 10+5+5 = 20
        // m2: 30+0+0 = 30   <- MVP
        // m3: 12+8+9 = 29
        val members = listOf(
            TeamMember(id = "m1", userId = "u1", teamId = TEAM_ID, role = "player"),
            TeamMember(id = "m2", userId = "u2", teamId = TEAM_ID, role = "player"),
            TeamMember(id = "m3", userId = "u3", teamId = TEAM_ID, role = "player"),
        )
        val users = listOf(
            User(id = "u1", displayName = "Alice"),
            User(id = "u2", displayName = "Bob"),
            User(id = "u3", displayName = "Carol"),
        )
        val matchStats = MatchStats(
            id = EVENT_ID,
            eventId = EVENT_ID,
            myTeamScore = 80,
            opponentScore = 60,
            playerStats = mapOf(
                "m1" to PlayerMatchStat(points = 10, rebounds = 5, assists = 5),
                "m2" to PlayerMatchStat(points = 30, rebounds = 0, assists = 0),
                "m3" to PlayerMatchStat(points = 12, rebounds = 8, assists = 9),
            )
        )
        stubScreen(matchStats = matchStats, members = members, users = users)

        // Act
        val vm = newViewModel()
        advanceUntilIdle()

        // Assert
        assertNotNull(vm.uiState.mvpItem)
        val mvp = vm.uiState.mvpItem!!
        assertEquals("m2", mvp.teamMemberId)
        assertEquals("Bob", mvp.displayName)
        assertEquals(30, mvp.points)
    }

    @Test
    fun `given player stats, then players list is sorted by score descending`() = runTest {
        // Arrange — same data as MVP test; expect order m2(30), m3(29), m1(20)
        val members = listOf(
            TeamMember(id = "m1", userId = "u1", teamId = TEAM_ID, role = "player"),
            TeamMember(id = "m2", userId = "u2", teamId = TEAM_ID, role = "player"),
            TeamMember(id = "m3", userId = "u3", teamId = TEAM_ID, role = "player"),
        )
        val users = listOf(
            User(id = "u1", displayName = "Alice"),
            User(id = "u2", displayName = "Bob"),
            User(id = "u3", displayName = "Carol"),
        )
        val matchStats = MatchStats(
            eventId = EVENT_ID,
            playerStats = mapOf(
                "m1" to PlayerMatchStat(points = 10, rebounds = 5, assists = 5),
                "m2" to PlayerMatchStat(points = 30, rebounds = 0, assists = 0),
                "m3" to PlayerMatchStat(points = 12, rebounds = 8, assists = 9),
            )
        )
        stubScreen(matchStats = matchStats, members = members, users = users)

        // Act
        val vm = newViewModel()
        advanceUntilIdle()

        // Assert
        val orderedIds = vm.uiState.players.map { it.teamMemberId }
        assertEquals(listOf("m2", "m3", "m1"), orderedIds)
    }

    @Test
    fun `given playerStats keys without a matching team member, then those entries are skipped`() = runTest {
        // Arrange — m_ghost has no TeamMember; m1 is fine
        val members = listOf(TeamMember(id = "m1", userId = "u1", teamId = TEAM_ID, role = "player"))
        val users = listOf(User(id = "u1", displayName = "Alice"))
        val matchStats = MatchStats(
            eventId = EVENT_ID,
            playerStats = mapOf(
                "m1" to PlayerMatchStat(points = 5, rebounds = 1, assists = 1),
                "m_ghost" to PlayerMatchStat(points = 99, rebounds = 99, assists = 99),
            )
        )
        stubScreen(matchStats = matchStats, members = members, users = users)

        // Act
        val vm = newViewModel()
        advanceUntilIdle()

        // Assert
        assertEquals(listOf("m1"), vm.uiState.players.map { it.teamMemberId })
        assertEquals("m1", vm.uiState.mvpItem!!.teamMemberId)
    }

    @Test
    fun `given a member without a matching user, then that player row is skipped`() = runTest {
        // Arrange — member m2 references missing user u_missing
        val members = listOf(
            TeamMember(id = "m1", userId = "u1", teamId = TEAM_ID, role = "player"),
            TeamMember(id = "m2", userId = "u_missing", teamId = TEAM_ID, role = "player"),
        )
        val users = listOf(User(id = "u1", displayName = "Alice"))
        val matchStats = MatchStats(
            eventId = EVENT_ID,
            playerStats = mapOf(
                "m1" to PlayerMatchStat(points = 1, rebounds = 1, assists = 1),
                "m2" to PlayerMatchStat(points = 50, rebounds = 50, assists = 50),
            )
        )
        stubScreen(matchStats = matchStats, members = members, users = users)

        // Act
        val vm = newViewModel()
        advanceUntilIdle()

        // Assert
        assertEquals(listOf("m1"), vm.uiState.players.map { it.teamMemberId })
    }

    // ── Win detection ─────────────────────────────────────────────────────────

    @Test
    fun `given my team score is greater than opponent, then isWin is true`() = runTest {
        // Arrange
        stubScreen(matchStats = MatchStats(eventId = EVENT_ID, myTeamScore = 80, opponentScore = 60))

        // Act
        val vm = newViewModel()
        advanceUntilIdle()

        // Assert
        assertTrue(vm.uiState.isWin)
        assertEquals(80, vm.uiState.myTeamScore)
        assertEquals(60, vm.uiState.opponentScore)
    }

    @Test
    fun `given my team score is less than opponent, then isWin is false`() = runTest {
        // Arrange
        stubScreen(matchStats = MatchStats(eventId = EVENT_ID, myTeamScore = 50, opponentScore = 70))

        // Act
        val vm = newViewModel()
        advanceUntilIdle()

        // Assert
        assertFalse(vm.uiState.isWin)
    }

    @Test
    fun `given a tied score, then isWin is false`() = runTest {
        // Arrange — strict greater-than means a tie is not a win
        stubScreen(matchStats = MatchStats(eventId = EVENT_ID, myTeamScore = 70, opponentScore = 70))

        // Act
        val vm = newViewModel()
        advanceUntilIdle()

        // Assert
        assertFalse(vm.uiState.isWin)
    }

    // ── opponentName fallback ─────────────────────────────────────────────────

    @Test
    fun `given a blank opponent, then opponentName falls back to event title`() = runTest {
        // Arrange
        stubScreen(
            event = Event(id = EVENT_ID, teamId = TEAM_ID, title = "Tournament Day", opponent = ""),
            matchStats = MatchStats(eventId = EVENT_ID, myTeamScore = 1, opponentScore = 0)
        )

        // Act
        val vm = newViewModel()
        advanceUntilIdle()

        // Assert
        assertEquals("Tournament Day", vm.uiState.opponentName)
    }

    // ── Failure paths ─────────────────────────────────────────────────────────

    @Test
    fun `given a blank eventId, then error is error_load_failed and loading stops`() = runTest {
        // Arrange / Act
        val vm = newViewModel(eventId = "")
        advanceUntilIdle()

        // Assert
        assertEquals(R.string.error_load_failed, vm.uiState.error)
        assertFalse(vm.uiState.isLoading)
    }

    @Test
    fun `given event is missing, then error is error_load_failed`() = runTest {
        // Arrange
        stubScreen(event = null, matchStats = MatchStats(eventId = EVENT_ID))

        // Act
        val vm = newViewModel()
        advanceUntilIdle()

        // Assert
        assertEquals(R.string.error_load_failed, vm.uiState.error)
        assertFalse(vm.uiState.isLoading)
    }

    @Test
    fun `given matchStats are missing, then error is error_load_failed`() = runTest {
        // Arrange
        stubScreen(matchStats = null)

        // Act
        val vm = newViewModel()
        advanceUntilIdle()

        // Assert
        assertEquals(R.string.error_load_failed, vm.uiState.error)
        assertFalse(vm.uiState.isLoading)
    }

    @Test
    fun `given a repository throws, then error is error_load_failed`() = runTest {
        // Arrange
        eventRepository.failOnGetEvent = true
        matchStatsRepository.matchStats = MatchStats(eventId = EVENT_ID)

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
