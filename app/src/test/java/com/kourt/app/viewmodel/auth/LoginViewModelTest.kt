package com.kourt.app.viewmodel.auth

/*
 * Tests for [LoginViewModel].
 *
 * Covers:
 *  - resolveDestination routing (called directly via the `internal` seam):
 *      * admin club found     -> CLUB_MANAGEMENT
 *      * coach role           -> COACH_DASHBOARD
 *      * assistant role       -> COACH_DASHBOARD
 *      * mixed-case role      -> COACH_DASHBOARD
 *      * empty members        -> SETUP
 *      * only player/parent   -> PLAYER_DASHBOARD
 *      * teamMember lookup throws -> SETUP fallback (runCatching)
 *  - onSignInWithEmail failure -> error_login_failed
 *  - onForgetPassword success / failure
 *  - onPasswordResetConsumed clears the success flag
 *  - onClearError clears any error
 *
 * No FirebaseUser is constructed: routing branches are exercised by calling
 * the (internal) resolveDestination directly, which sidesteps the abstract
 * FirebaseUser type entirely. The success arm of onSignInWithEmail is covered
 * indirectly through resolveDestination, which is everything that arm calls.
 */

import com.kourt.app.R
import com.kourt.app.data.model.Club
import com.kourt.app.data.model.TeamMember
import com.kourt.app.ui.screens.auth.login.LoginDestination
import com.kourt.app.util.FakeAuthRepository
import com.kourt.app.util.FakeClubRepository
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var authRepository: FakeAuthRepository
    private lateinit var userRepository: FakeUserRepository
    private lateinit var teamMemberRepository: FakeTeamMemberRepository
    private lateinit var clubRepository: FakeClubRepository
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        authRepository = FakeAuthRepository.create()
        userRepository = FakeUserRepository.create()
        teamMemberRepository = FakeTeamMemberRepository.create()
        clubRepository = FakeClubRepository.create()
        viewModel = LoginViewModel(
            authRepository = authRepository,
            userRepository = userRepository,
            teamMemberRepository = teamMemberRepository,
            clubRepository = clubRepository,
        )
    }

    // ── resolveDestination routing ────────────────────────────────────────────

    @Test
    fun `given user is admin of a club, when resolveDestination called, then destination is CLUB_MANAGEMENT`() = runTest {
        // Arrange
        clubRepository.clubByAdmin = Club(id = "club-1")

        // Act
        viewModel.resolveDestination(TEST_UID)
        advanceUntilIdle()

        // Assert
        assertEquals(LoginDestination.CLUB_MANAGEMENT, viewModel.uiState.destination)
        assertFalse(viewModel.uiState.isLoading)
        assertNull(viewModel.uiState.error)
    }

    @Test
    fun `given user is a coach, when resolveDestination called, then destination is COACH_DASHBOARD`() = runTest {
        // Arrange
        clubRepository.clubByAdmin = null
        teamMemberRepository.membersByUser = listOf(
            TeamMember(id = "m1", userId = TEST_UID, role = "coach")
        )

        // Act
        viewModel.resolveDestination(TEST_UID)
        advanceUntilIdle()

        // Assert
        assertEquals(LoginDestination.COACH_DASHBOARD, viewModel.uiState.destination)
        assertFalse(viewModel.uiState.isLoading)
    }

    @Test
    fun `given user is an assistant, when resolveDestination called, then destination is COACH_DASHBOARD`() = runTest {
        // Arrange
        teamMemberRepository.membersByUser = listOf(
            TeamMember(id = "m1", userId = TEST_UID, role = "assistant")
        )

        // Act
        viewModel.resolveDestination(TEST_UID)
        advanceUntilIdle()

        // Assert
        assertEquals(LoginDestination.COACH_DASHBOARD, viewModel.uiState.destination)
    }

    @Test
    fun `given role casing is mixed, when resolveDestination called, then COACH_DASHBOARD is still chosen`() = runTest {
        // Arrange — role lookup is lowercased in the ViewModel, so "COACH" must still map to coach
        teamMemberRepository.membersByUser = listOf(
            TeamMember(id = "m1", userId = TEST_UID, role = "COACH")
        )

        // Act
        viewModel.resolveDestination(TEST_UID)
        advanceUntilIdle()

        // Assert
        assertEquals(LoginDestination.COACH_DASHBOARD, viewModel.uiState.destination)
    }

    @Test
    fun `given user has no memberships and no club, when resolveDestination called, then destination is SETUP`() = runTest {
        // Arrange
        clubRepository.clubByAdmin = null
        teamMemberRepository.membersByUser = emptyList()

        // Act
        viewModel.resolveDestination(TEST_UID)
        advanceUntilIdle()

        // Assert
        assertEquals(LoginDestination.SETUP, viewModel.uiState.destination)
    }

    @Test
    fun `given user is only player or parent, when resolveDestination called, then destination is PLAYER_DASHBOARD`() = runTest {
        // Arrange
        teamMemberRepository.membersByUser = listOf(
            TeamMember(id = "m1", userId = TEST_UID, role = "player"),
            TeamMember(id = "m2", userId = TEST_UID, role = "parent"),
        )

        // Act
        viewModel.resolveDestination(TEST_UID)
        advanceUntilIdle()

        // Assert
        assertEquals(LoginDestination.PLAYER_DASHBOARD, viewModel.uiState.destination)
    }

    @Test
    fun `given teamMember lookup throws, when resolveDestination called, then destination falls back to SETUP`() = runTest {
        // Arrange — runCatching wraps the throw and maps it to emptyList -> SETUP branch
        teamMemberRepository.failOnGetMembersByUser = true

        // Act
        viewModel.resolveDestination(TEST_UID)
        advanceUntilIdle()

        // Assert
        assertEquals(LoginDestination.SETUP, viewModel.uiState.destination)
    }

    // ── onSignInWithEmail failure path ────────────────────────────────────────

    @Test
    fun `given email auth fails, when onSignInWithEmail called, then error is error_login_failed`() = runTest {
        // Arrange
        authRepository.signInResult = Result.failure(RuntimeException("bad creds"))

        // Act
        viewModel.onSignInWithEmail("a@b.c", "wrong")
        advanceUntilIdle()

        // Assert
        assertEquals(R.string.error_login_failed, viewModel.uiState.error)
        assertFalse(viewModel.uiState.isLoading)
        assertEquals(LoginDestination.NONE, viewModel.uiState.destination)
    }

    // ── onForgetPassword ──────────────────────────────────────────────────────

    @Test
    fun `given reset succeeds, when onForgetPassword called, then passwordResetSent is true`() = runTest {
        // Arrange
        authRepository.resetResult = Result.success(Unit)

        // Act
        viewModel.onForgetPassword("a@b.c")
        advanceUntilIdle()

        // Assert
        assertTrue(viewModel.uiState.passwordResetSent)
        assertFalse(viewModel.uiState.isLoading)
        assertNull(viewModel.uiState.error)
    }

    @Test
    fun `given reset fails, when onForgetPassword called, then error is error_reset_failed`() = runTest {
        // Arrange
        authRepository.resetResult = Result.failure(RuntimeException("nope"))

        // Act
        viewModel.onForgetPassword("a@b.c")
        advanceUntilIdle()

        // Assert
        assertEquals(R.string.error_reset_failed, viewModel.uiState.error)
        assertFalse(viewModel.uiState.isLoading)
        assertFalse(viewModel.uiState.passwordResetSent)
    }

    @Test
    fun `given password reset is sent, when onPasswordResetConsumed called, then flag clears`() = runTest {
        // Arrange — push the flag to true first
        authRepository.resetResult = Result.success(Unit)
        viewModel.onForgetPassword("a@b.c")
        advanceUntilIdle()
        assertTrue(viewModel.uiState.passwordResetSent)

        // Act
        viewModel.onPasswordResetConsumed()

        // Assert
        assertFalse(viewModel.uiState.passwordResetSent)
    }

    @Test
    fun `given an error is set, when onClearError called, then error becomes null`() = runTest {
        // Arrange — produce an error first
        authRepository.signInResult = Result.failure(RuntimeException("bad creds"))
        viewModel.onSignInWithEmail("a@b.c", "wrong")
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.error)

        // Act
        viewModel.onClearError()

        // Assert
        assertNull(viewModel.uiState.error)
    }

    private companion object {
        const val TEST_UID = "uid-123"
    }
}
