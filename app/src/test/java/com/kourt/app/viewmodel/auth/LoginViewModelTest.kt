package com.kourt.app.viewmodel.auth

/*
 * Tests for [LoginViewModel].
 *
 * Covers:
 *  - resolveDestination routing (exercised via onSignInWithEmail success):
 *      * admin club found  -> CLUB_MANAGEMENT
 *      * coach role        -> COACH_DASHBOARD
 *      * assistant role    -> COACH_DASHBOARD
 *      * empty members     -> SETUP
 *      * only player/parent -> PLAYER_DASHBOARD
 *  - onForgetPassword success/failure
 *  - onSignInWithEmail failure
 */

import com.google.common.truth.Truth.assertThat
import com.google.firebase.auth.FirebaseUser
import com.kourt.app.R
import com.kourt.app.data.model.Club
import com.kourt.app.data.model.TeamMember
import com.kourt.app.data.repository.remote.AuthRepository
import com.kourt.app.data.repository.remote.ClubRepository
import com.kourt.app.data.repository.remote.TeamMemberRepository
import com.kourt.app.data.repository.remote.UserRepository
import com.kourt.app.ui.screens.auth.login.LoginDestination
import com.kourt.app.util.MainCoroutineRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val userRepository: UserRepository = mockk(relaxed = true)
    private val teamMemberRepository: TeamMemberRepository = mockk(relaxed = true)
    private val clubRepository: ClubRepository = mockk(relaxed = true)

    private lateinit var viewModel: LoginViewModel

    private val firebaseUser: FirebaseUser = mockk(relaxed = true) {
        every { uid } returns TEST_UID
        every { email } returns "test@example.com"
    }

    @Before
    fun setUp() {
        viewModel = LoginViewModel(
            authRepository = authRepository,
            userRepository = userRepository,
            teamMemberRepository = teamMemberRepository,
            clubRepository = clubRepository,
        )
    }

    // ── onSignInWithEmail / resolveDestination ────────────────────────────────

    @Test
    fun `given user is admin of a club, when sign in succeeds, then destination is CLUB_MANAGEMENT`() = runTest {
        // Arrange
        coEvery { authRepository.signInWithEmail(any(), any()) } returns Result.success(firebaseUser)
        coEvery { clubRepository.getClubByAdminId(TEST_UID) } returns Club(id = "club-1")

        // Act
        viewModel.onSignInWithEmail("a@b.c", "pw")
        advanceUntilIdle()

        // Assert
        assertThat(viewModel.uiState.destination).isEqualTo(LoginDestination.CLUB_MANAGEMENT)
        assertThat(viewModel.uiState.isLoading).isFalse()
        assertThat(viewModel.uiState.error).isNull()
    }

    @Test
    fun `given user is a coach, when sign in succeeds, then destination is COACH_DASHBOARD`() = runTest {
        // Arrange
        coEvery { authRepository.signInWithEmail(any(), any()) } returns Result.success(firebaseUser)
        coEvery { clubRepository.getClubByAdminId(TEST_UID) } returns null
        coEvery { teamMemberRepository.getMembersByUser(TEST_UID) } returns listOf(
            TeamMember(id = "m1", userId = TEST_UID, role = "coach")
        )

        // Act
        viewModel.onSignInWithEmail("a@b.c", "pw")
        advanceUntilIdle()

        // Assert
        assertThat(viewModel.uiState.destination).isEqualTo(LoginDestination.COACH_DASHBOARD)
        assertThat(viewModel.uiState.isLoading).isFalse()
    }

    @Test
    fun `given user is an assistant, when sign in succeeds, then destination is COACH_DASHBOARD`() = runTest {
        // Arrange
        coEvery { authRepository.signInWithEmail(any(), any()) } returns Result.success(firebaseUser)
        coEvery { clubRepository.getClubByAdminId(TEST_UID) } returns null
        coEvery { teamMemberRepository.getMembersByUser(TEST_UID) } returns listOf(
            TeamMember(id = "m1", userId = TEST_UID, role = "assistant")
        )

        // Act
        viewModel.onSignInWithEmail("a@b.c", "pw")
        advanceUntilIdle()

        // Assert
        assertThat(viewModel.uiState.destination).isEqualTo(LoginDestination.COACH_DASHBOARD)
    }

    @Test
    fun `given role casing is mixed, when sign in succeeds, then COACH_DASHBOARD is still chosen`() = runTest {
        // Arrange — role lookup is lowercased in the ViewModel, so "COACH" must still map to coach
        coEvery { authRepository.signInWithEmail(any(), any()) } returns Result.success(firebaseUser)
        coEvery { clubRepository.getClubByAdminId(TEST_UID) } returns null
        coEvery { teamMemberRepository.getMembersByUser(TEST_UID) } returns listOf(
            TeamMember(id = "m1", userId = TEST_UID, role = "COACH")
        )

        // Act
        viewModel.onSignInWithEmail("a@b.c", "pw")
        advanceUntilIdle()

        // Assert
        assertThat(viewModel.uiState.destination).isEqualTo(LoginDestination.COACH_DASHBOARD)
    }

    @Test
    fun `given user has no memberships and no club, when sign in succeeds, then destination is SETUP`() = runTest {
        // Arrange
        coEvery { authRepository.signInWithEmail(any(), any()) } returns Result.success(firebaseUser)
        coEvery { clubRepository.getClubByAdminId(TEST_UID) } returns null
        coEvery { teamMemberRepository.getMembersByUser(TEST_UID) } returns emptyList()

        // Act
        viewModel.onSignInWithEmail("a@b.c", "pw")
        advanceUntilIdle()

        // Assert
        assertThat(viewModel.uiState.destination).isEqualTo(LoginDestination.SETUP)
    }

    @Test
    fun `given user is only player or parent, when sign in succeeds, then destination is PLAYER_DASHBOARD`() = runTest {
        // Arrange
        coEvery { authRepository.signInWithEmail(any(), any()) } returns Result.success(firebaseUser)
        coEvery { clubRepository.getClubByAdminId(TEST_UID) } returns null
        coEvery { teamMemberRepository.getMembersByUser(TEST_UID) } returns listOf(
            TeamMember(id = "m1", userId = TEST_UID, role = "player"),
            TeamMember(id = "m2", userId = TEST_UID, role = "parent"),
        )

        // Act
        viewModel.onSignInWithEmail("a@b.c", "pw")
        advanceUntilIdle()

        // Assert
        assertThat(viewModel.uiState.destination).isEqualTo(LoginDestination.PLAYER_DASHBOARD)
    }

    @Test
    fun `given teamMember lookup throws, when sign in succeeds, then destination falls back to SETUP`() = runTest {
        // Arrange — runCatching wraps the throw and maps it to emptyList -> SETUP branch
        coEvery { authRepository.signInWithEmail(any(), any()) } returns Result.success(firebaseUser)
        coEvery { clubRepository.getClubByAdminId(TEST_UID) } returns null
        coEvery { teamMemberRepository.getMembersByUser(TEST_UID) } throws RuntimeException("network down")

        // Act
        viewModel.onSignInWithEmail("a@b.c", "pw")
        advanceUntilIdle()

        // Assert
        assertThat(viewModel.uiState.destination).isEqualTo(LoginDestination.SETUP)
    }

    @Test
    fun `given email auth fails, when onSignInWithEmail called, then error is error_login_failed`() = runTest {
        // Arrange
        coEvery { authRepository.signInWithEmail(any(), any()) } returns
            Result.failure(RuntimeException("bad creds"))

        // Act
        viewModel.onSignInWithEmail("a@b.c", "wrong")
        advanceUntilIdle()

        // Assert
        assertThat(viewModel.uiState.error).isEqualTo(R.string.error_login_failed)
        assertThat(viewModel.uiState.isLoading).isFalse()
        assertThat(viewModel.uiState.destination).isEqualTo(LoginDestination.NONE)
    }

    // ── onForgetPassword ──────────────────────────────────────────────────────

    @Test
    fun `given reset succeeds, when onForgetPassword called, then passwordResetSent is true`() = runTest {
        // Arrange
        coEvery { authRepository.resetPassword("a@b.c") } returns Result.success(Unit)

        // Act
        viewModel.onForgetPassword("a@b.c")
        advanceUntilIdle()

        // Assert
        assertThat(viewModel.uiState.passwordResetSent).isTrue()
        assertThat(viewModel.uiState.isLoading).isFalse()
        assertThat(viewModel.uiState.error).isNull()
    }

    @Test
    fun `given reset fails, when onForgetPassword called, then error is error_reset_failed`() = runTest {
        // Arrange
        coEvery { authRepository.resetPassword(any()) } returns
            Result.failure(RuntimeException("nope"))

        // Act
        viewModel.onForgetPassword("a@b.c")
        advanceUntilIdle()

        // Assert
        assertThat(viewModel.uiState.error).isEqualTo(R.string.error_reset_failed)
        assertThat(viewModel.uiState.isLoading).isFalse()
        assertThat(viewModel.uiState.passwordResetSent).isFalse()
    }

    @Test
    fun `given password reset is sent, when onPasswordResetConsumed called, then flag clears`() = runTest {
        // Arrange — manually push the flag to true then consume
        coEvery { authRepository.resetPassword(any()) } returns Result.success(Unit)
        viewModel.onForgetPassword("a@b.c")
        advanceUntilIdle()
        assertThat(viewModel.uiState.passwordResetSent).isTrue()

        // Act
        viewModel.onPasswordResetConsumed()

        // Assert
        assertThat(viewModel.uiState.passwordResetSent).isFalse()
    }

    @Test
    fun `given an error is set, when onClearError called, then error becomes null`() = runTest {
        // Arrange — produce an error first
        coEvery { authRepository.signInWithEmail(any(), any()) } returns
            Result.failure(RuntimeException("bad creds"))
        viewModel.onSignInWithEmail("a@b.c", "wrong")
        advanceUntilIdle()
        assertThat(viewModel.uiState.error).isNotNull()

        // Act
        viewModel.onClearError()

        // Assert
        assertThat(viewModel.uiState.error).isNull()
    }

    private companion object {
        const val TEST_UID = "uid-123"
    }
}
