package com.kourt.app.viewmodel.settings

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kourt.app.R
import com.kourt.app.data.repository.AppPreferencesRepository
import com.kourt.app.data.repository.AuthRepository
import com.kourt.app.data.repository.ClubRepository
import com.kourt.app.data.repository.TeamMemberRepository
import com.kourt.app.data.repository.TeamRepository
import com.kourt.app.data.repository.UserRepository
import com.kourt.app.ui.screens.settings.profile.MembershipRowUiItem
import com.kourt.app.ui.screens.settings.profile.ProfileScreenActions
import com.kourt.app.ui.screens.settings.profile.ProfileScreenUiState
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "ProfileViewModel"

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val teamMemberRepository: TeamMemberRepository,
    private val teamRepository: TeamRepository,
    private val clubRepository: ClubRepository,
    private val appPreferencesRepository: AppPreferencesRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel(), ProfileScreenActions {

    var uiState by mutableStateOf(ProfileScreenUiState())
        private set

    private val targetUserId: String = savedStateHandle["userId"] ?: ""

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val firebaseUser = authRepository.currentUser
            if (firebaseUser == null) {
                Log.w(TAG, "loadProfile: no signed-in user")
                uiState = uiState.copy(isLoading = false, error = R.string.error_not_signed_in)
                return@launch
            }

            val uid = targetUserId.ifBlank { firebaseUser.uid }
            val isReadOnly = targetUserId.isNotBlank() && targetUserId != firebaseUser.uid
            uiState = uiState.copy(isReadOnly = isReadOnly)

            // Load Firestore User document for display name + email
            val user = userRepository.getUser(uid)
            uiState = uiState.copy(
                displayName = user?.displayName ?: firebaseUser.displayName.orEmpty(),
                email = user?.email ?: firebaseUser.email.orEmpty(),
                avatarId = user?.avatarId.orEmpty(),
            )

            // Load team memberships and admin clubs concurrently
            val membershipRowsDeferred = async { buildMembershipRows(uid) }
            val adminRowsDeferred = async { buildAdminRows(uid) }

            val membershipRows = membershipRowsDeferred.await()
            val adminRows = adminRowsDeferred.await()

            uiState = uiState.copy(
                memberships = adminRows + membershipRows,
                isLoading = false,
            )

            Log.d(TAG, "loadProfile: loaded ${membershipRows.size} memberships + ${adminRows.size} admin rows for uid=$uid")
        }
    }

    private suspend fun buildMembershipRows(uid: String): List<MembershipRowUiItem> {
        return try {
            val members = teamMemberRepository.getMembersByUser(uid)
            coroutineScope {
                members.map { member ->
                    async {
                        val team = teamRepository.getTeam(member.teamId)
                        val club = team?.clubId?.let { clubRepository.getClub(it) }
                        MembershipRowUiItem(
                            clubId = team?.clubId.orEmpty(),
                            teamId = member.teamId,
                            clubName = club?.name.orEmpty().ifEmpty { team?.name.orEmpty() },
                            subtitle = team?.name.orEmpty(),
                            role = member.role,
                            accentColor = team?.accentColor.orEmpty(),
                            initials = team?.initials.orEmpty(),
                        )
                    }
                }.awaitAll()
            }
                .filter { it.clubName.isNotEmpty() || it.subtitle.isNotEmpty() }
        } catch (e: Exception) {
            Log.e(TAG, "buildMembershipRows failed", e)
            emptyList()
        }
    }

    private suspend fun buildAdminRows(uid: String): List<MembershipRowUiItem> {
        return try {
            val clubs = clubRepository.getClubsByAdminId(uid)
            clubs.map { club ->
                MembershipRowUiItem(
                    clubId = club.id,
                    teamId = null,
                    clubName = club.name,
                    subtitle = "Club",
                    role = "ADMIN",
                    accentColor = club.accentColor,
                    initials = club.initials,
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "buildAdminRows failed", e)
            emptyList()
        }
    }

    override fun onEditAvatarClick() {
        if (uiState.isReadOnly) return
        uiState = uiState.copy(showAvatarPicker = true)
    }

    override fun onAvatarSelected(avatarId: String) {
        val uid = authRepository.currentUser?.uid ?: return
        uiState = uiState.copy(avatarId = avatarId, showAvatarPicker = false)
        viewModelScope.launch {
            runCatching {
                val user = userRepository.getUser(uid) ?: return@launch
                userRepository.updateUser(user.copy(avatarId = avatarId))
                Log.d(TAG, "onAvatarSelected: saved avatarId=$avatarId")
            }.onFailure { e ->
                Log.e(TAG, "onAvatarSelected: failed to save", e)
            }
        }
    }

    override fun onAvatarPickerDismiss() {
        uiState = uiState.copy(showAvatarPicker = false)
    }

    override fun onMembershipClick(item: MembershipRowUiItem) {
        if (uiState.isReadOnly) return
        appPreferencesRepository.activeClubId = item.clubId
        Log.d(TAG, "onMembershipClick: clubId=${item.clubId}, role=${item.role}")
        if (item.role.lowercase() == "admin") {
            uiState = uiState.copy(navigateToClubManagement = true)
        }
        if(item.role.lowercase() == "player"){
            uiState = uiState.copy(navigateToPlayerDashboard = true)
        }
        if(item.role.lowercase() == "coach") {
            uiState = uiState.copy(navigateToCoachDashboard = true)
        }

    }

    override fun onNavigationConsumed() {
        uiState = uiState.copy(navigateToClubManagement = false, navigateToCoachDashboard = false,navigateToPlayerDashboard = false )
    }
}
