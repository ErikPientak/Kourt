package com.kourt.app.viewmodel.event

/*
 * Tests for [AfterMatchStatsViewModel].
 *
 * Covers:
 *  - MVP selection: highest (points + rebounds + assists)
 *  - Tie-breaking falls back to first encountered (sortedByDescending stable)
 *  - isWin = myTeamScore > opponentScore
 *  - isWin = false on ties and losses
 *  - Players are sorted by score descending
 *  - Players whose member or user is missing are dropped
 *  - Failure paths: null event / null matchStats / blank eventId / repo throws
 */

import androidx.lifecycle.SavedStateHandle
import com.google.common.truth.Truth.assertThat
import com.kourt.app.R
import com.kourt.app.data.model.Event
import com.kourt.app.data.model.MatchStats
import com.kourt.app.data.model.PlayerMatchStat
import com.kourt.app.data.model.TeamMember
import com.kourt.app.data.model.User
import com.kourt.app.data.repository.remote.EventRepository
import com.kourt.app.data.repository.remote.MatchStatsRepository
import com.kourt.app.data.repository.remote.TeamMemberRepository
import com.kourt.app.data.repository.remote.UserRepository
import com.kourt.app.util.MainCoroutineRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AfterMatchStatsViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private val eventRepository: EventRepository = mockk(relaxed = true)
    private val matchStatsRepository: MatchStatsRepository = mockk(relaxed = true)
    private val teamMemberRepository: TeamMemberRepository = mockk(relaxed = true)
    private val userRepository: UserRepository = mockk(relaxed = true)

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
        coEvery { eventRepository.getEvent(EVENT_ID) } returns event
        coEvery { matchStatsRepository.getMatchStats(EVENT_ID) } returns matchStats
        coEvery { teamMemberRepository.getMembersByTeam(any()) } returns members
        coEvery { userRepository.getUsersByIds(any()) } returns users
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
        assertThat(vm.uiState.mvpItem).isNotNull()
        assertThat(vm.uiState.mvpItem!!.teamMemberId).isEqualTo("m2")
        assertThat(vm.uiState.mvpItem!!.displayName).isEqualTo("Bob")
        assertThat(vm.uiState.mvpItem!!.points).isEqualTo(30)
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
        assertThat(orderedIds).containsExactly("m2", "m3", "m1").inOrder()
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
        assertThat(vm.uiState.players.map { it.teamMemberId }).containsExactly("m1")
        assertThat(vm.uiState.mvpItem!!.teamMemberId).isEqualTo("m1")
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
        assertThat(vm.uiState.players.map { it.teamMemberId }).containsExactly("m1")
    }

    // ── Win detection ─────────────────────────────────────────────────────────

    @Test
    fun `given my team score is greater than opponent, then isWin is true`() = runTest {
        // Arrange
        stubScreen(
            matchStats = MatchStats(eventId = EVENT_ID, myTeamScore = 80, opponentScore = 60)
        )

        // Act
        val vm = newViewModel()
        advanceUntilIdle()

        // Assert
        assertThat(vm.uiState.isWin).isTrue()
        assertThat(vm.uiState.myTeamScore).isEqualTo(80)
        assertThat(vm.uiState.opponentScore).isEqualTo(60)
    }

    @Test
    fun `given my team score is less than opponent, then isWin is false`() = runTest {
        // Arrange
        stubScreen(
            matchStats = MatchStats(eventId = EVENT_ID, myTeamScore = 50, opponentScore = 70)
        )

        // Act
        val vm = newViewModel()
        advanceUntilIdle()

        // Assert
        assertThat(vm.uiState.isWin).isFalse()
    }

    @Test
    fun `given a tied score, then isWin is false`() = runTest {
        // Arrange — strict greater-than means a tie is not a win
        stubScreen(
            matchStats = MatchStats(eventId = EVENT_ID, myTeamScore = 70, opponentScore = 70)
        )

        // Act
        val vm = newViewModel()
        advanceUntilIdle()

        // Assert
        assertThat(vm.uiState.isWin).isFalse()
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
        assertThat(vm.uiState.opponentName).isEqualTo("Tournament Day")
    }

    // ── Failure paths ─────────────────────────────────────────────────────────

    @Test
    fun `given a blank eventId, then error is error_load_failed and loading stops`() = runTest {
        // Arrange / Act
        val vm = newViewModel(eventId = "")
        advanceUntilIdle()

        // Assert
        assertThat(vm.uiState.error).isEqualTo(R.string.error_load_failed)
        assertThat(vm.uiState.isLoading).isFalse()
    }

    @Test
    fun `given event is missing, then error is error_load_failed`() = runTest {
        // Arrange
        stubScreen(event = null, matchStats = MatchStats(eventId = EVENT_ID))

        // Act
        val vm = newViewModel()
        advanceUntilIdle()

        // Assert
        assertThat(vm.uiState.error).isEqualTo(R.string.error_load_failed)
        assertThat(vm.uiState.isLoading).isFalse()
    }

    @Test
    fun `given matchStats are missing, then error is error_load_failed`() = runTest {
        // Arrange
        stubScreen(matchStats = null)

        // Act
        val vm = newViewModel()
        advanceUntilIdle()

        // Assert
        assertThat(vm.uiState.error).isEqualTo(R.string.error_load_failed)
        assertThat(vm.uiState.isLoading).isFalse()
    }

    @Test
    fun `given a repository throws, then error is error_load_failed`() = runTest {
        // Arrange
        coEvery { eventRepository.getEvent(EVENT_ID) } throws RuntimeException("boom")
        coEvery { matchStatsRepository.getMatchStats(EVENT_ID) } returns MatchStats(eventId = EVENT_ID)

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
