package com.kourt.app.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kourt.app.data.model.TeamMember
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
            if (authRepository.isLoggedIn) {
                val uid = authRepository.currentUser?.uid
                val isAdmin = runCatching { clubRepository.getClubByAdminId(uid!!) }.getOrNull() != null

                if (isAdmin) {
                    _startDestination.value = Destination.ClubManagementScreen.route
                }
                else{
                    val member: TeamMember = runCatching { teamMemberRepository.getMemberByUserId(uid!!) }.getOrNull()
                        ?: TeamMember(id = "", userId = "", teamId = "", role = "")

                    Log.d("MainViewModel", "Member role: ${member.role}")

                    when(member.role.lowercase()){
                        "player" -> {
                            _startDestination.value = Destination.PlayerDashboardScreen.route
                        }
                        "assistant" -> {
                            _startDestination.value = Destination.CoachDashboardScreen.route
                        }
                        "coach" -> {
                            _startDestination.value = Destination.CoachDashboardScreen.route
                        }
                        else -> {
                            _startDestination.value = Destination.SetupScreen.route
                        }

                    }
                }


            }
            else{
                _startDestination.value = Destination.LoginScreen.route
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