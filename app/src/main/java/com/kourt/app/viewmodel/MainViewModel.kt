package com.kourt.app.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kourt.app.data.repository.local.AppPreferencesRepository
import com.kourt.app.data.repository.remote.AuthRepository
import com.kourt.app.data.repository.remote.ClubRepository
import com.kourt.app.data.repository.remote.TeamMemberRepository
import com.kourt.app.navigation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val teamMemberRepository: TeamMemberRepository,
    private val clubRepository: ClubRepository,
    private val appPreferences: AppPreferencesRepository,
) : ViewModel() {

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination: StateFlow<String?> = _startDestination

    init {
        resolveDestination()
    }

    fun resolveDestination() {
        viewModelScope.launch {
            val uid = authRepository.currentUser?.uid
            if (!authRepository.isLoggedIn || uid == null) {
                _startDestination.value = Destination.LoginScreen.route
                return@launch
            }
            val route = runCatching { resolveStartRoute(uid, clubRepository, teamMemberRepository) }
                .onSuccess { appPreferences.lastStartRoute = it.name }
                .getOrElse { e ->
                    // Offline with nothing cached: reopen where the user was last time rather
                    // than treating them as a new user. Setup only if there's no previous launch.
                    Log.w("MainViewModel", "Start route lookup failed, using last known route", e)
                    StartRoute.entries.firstOrNull { it.name == appPreferences.lastStartRoute } ?: StartRoute.SETUP
                }
            Log.d("MainViewModel", "Start route: $route")
            _startDestination.value = when (route) {
                StartRoute.CLUB_MANAGEMENT -> Destination.ClubManagementScreen.route
                StartRoute.COACH_DASHBOARD -> Destination.CoachDashboardScreen.route
                StartRoute.PLAYER_DASHBOARD -> Destination.PlayerDashboardScreen.route
                StartRoute.SETUP -> Destination.SetupScreen.route
            }
        }
    }

    var isDarkTheme by mutableStateOf(appPreferences.isDarkTheme)
        private set

    var language by mutableStateOf(appPreferences.language)
        private set

    fun toggleTheme() {
        val newValue = !isDarkTheme
        appPreferences.isDarkTheme = newValue
        isDarkTheme = newValue
    }

    fun updateLanguage(code: String) {
        appPreferences.language = code
        language = code
    }
}

/** The screen a signed-in user starts on. */
enum class StartRoute { CLUB_MANAGEMENT, COACH_DASHBOARD, PLAYER_DASHBOARD, SETUP }

/**
 * Picks the start screen for [uid]: club admins go to Club Management, then any staff role wins
 * over player or parent, so a user in several teams always lands on the same screen.
 * Failures are not caught, so callers can tell "offline" apart from "not in a team yet".
 */
internal suspend fun resolveStartRoute(
    uid: String,
    clubRepository: ClubRepository,
    teamMemberRepository: TeamMemberRepository,
): StartRoute {
    if (clubRepository.getClubByAdminId(uid) != null) return StartRoute.CLUB_MANAGEMENT
    val roles = teamMemberRepository.getMembersByUser(uid).map { it.role.lowercase() }
    return when {
        roles.isEmpty() -> StartRoute.SETUP
        roles.any { it in setOf("coach", "assistant", "admin") } -> StartRoute.COACH_DASHBOARD
        else -> StartRoute.PLAYER_DASHBOARD
    }
}
