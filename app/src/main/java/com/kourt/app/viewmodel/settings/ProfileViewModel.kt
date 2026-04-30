package com.kourt.app.viewmodel.settings

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kourt.app.R
import com.kourt.app.data.model.TeamMember
import com.kourt.app.data.repository.AppPreferencesRepository
import com.kourt.app.data.repository.AuthRepository
import com.kourt.app.data.repository.ClubRepository
import com.kourt.app.data.repository.TeamMemberRepository
import com.kourt.app.data.repository.TeamRepository
import com.kourt.app.data.repository.UserRepository
import com.kourt.app.ui.screens.settings.profile.ChildTeamUiItem
import com.kourt.app.ui.screens.settings.profile.ManagedChildUiItem
import com.kourt.app.ui.screens.settings.profile.MembershipRowUiItem
import com.kourt.app.ui.screens.settings.profile.ProfileScreenActions
import com.kourt.app.ui.screens.settings.profile.ProfileScreenUiState
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

            val user = userRepository.getUser(uid)
            uiState = uiState.copy(
                displayName = user?.displayName ?: firebaseUser.displayName.orEmpty(),
                email = user?.email ?: firebaseUser.email.orEmpty(),
                avatarId = user?.avatarId.orEmpty(),
            )

            val membershipRowsDeferred = async { buildMembershipRows(uid) }
            val adminRowsDeferred = async { buildAdminRows(uid) }
            val childRowsDeferred = async {
                if (user?.childIds.isNullOrEmpty()) emptyList()
                else buildChildRows(user!!.childIds)
            }

            val membershipRows = membershipRowsDeferred.await()
            val adminRows = adminRowsDeferred.await()
            val childRows = childRowsDeferred.await()
            val currentActiveChildId = appPreferencesRepository.activeChildId

            uiState = uiState.copy(
                memberships = adminRows + membershipRows,
                managedChildren = childRows.map { it.copy(isActive = it.userId == currentActiveChildId) },
                activeChildId = currentActiveChildId,
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
            }.filter { it.clubName.isNotEmpty() || it.subtitle.isNotEmpty() }
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

    private suspend fun buildChildRows(childIds: List<String>): List<ManagedChildUiItem> {
        return try {
            val children = userRepository.getUsersByIds(childIds)
            coroutineScope {
                children.map { child ->
                    async {
                        val teams = runCatching {
                            teamMemberRepository.getMembersByUser(child.id).mapNotNull { member ->
                                val teamName = teamRepository.getTeam(member.teamId)?.name ?: return@mapNotNull null
                                ChildTeamUiItem(
                                    membershipId = member.id,
                                    teamId = member.teamId,
                                    teamName = teamName,
                                )
                            }
                        }.getOrDefault(emptyList())
                        ManagedChildUiItem(
                            userId = child.id,
                            displayName = child.displayName,
                            avatarId = child.avatarId,
                            teams = teams,
                            isActive = false,
                        )
                    }
                }.awaitAll()
            }
        } catch (e: Exception) {
            Log.e(TAG, "buildChildRows failed", e)
            emptyList()
        }
    }

    // ── Child management ───────────────────────────────────────────────────────

    override fun onChildRowClick(child: ManagedChildUiItem) {
        uiState = uiState.copy(selectedChild = child, showChildActionSheet = true)
    }

    override fun onSwitchToChild(childId: String) {
        appPreferencesRepository.activeChildId = childId
        appPreferencesRepository.activeTeamId = ""
        uiState = uiState.copy(
            managedChildren = uiState.managedChildren.map { it.copy(isActive = it.userId == childId) },
            activeChildId = childId,
            showChildActionSheet = false,
            selectedChild = null,
            navigateToPlayerDashboard = true,
        )
        Log.d(TAG, "onSwitchToChild: switched to childId=$childId")
    }

    override fun onChildActionSheetDismiss() {
        uiState = uiState.copy(showChildActionSheet = false, selectedChild = null)
    }

    override fun onChildJoinTeamDialogShow() {
        uiState = uiState.copy(
            showChildActionSheet = false,
            showJoinTeamForChildDialog = true,
            joinCodeForChild = "",
            joinCodeForChildError = null,
        )
    }

    override fun onChildJoinTeamDialogDismiss() {
        uiState = uiState.copy(showJoinTeamForChildDialog = false, joinCodeForChild = "", joinCodeForChildError = null)
    }

    override fun onChildJoinCodeChange(code: String) {
        uiState = uiState.copy(joinCodeForChild = code.uppercase(), joinCodeForChildError = null)
    }

    override fun onChildJoinTeam() {
        val childId = uiState.selectedChild?.userId ?: return
        val code = uiState.joinCodeForChild.trim()
        if (code.isBlank()) {
            uiState = uiState.copy(joinCodeForChildError = R.string.error_join_code_required)
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, joinCodeForChildError = null)
            runCatching {
                val team = teamRepository.getTeamByJoinCode(code)
                if (team == null) {
                    uiState = uiState.copy(isLoading = false, joinCodeForChildError = R.string.error_invalid_join_code)
                    return@launch
                }
                val alreadyMember = teamMemberRepository.isMember(childId, team.id)
                if (alreadyMember) {
                    uiState = uiState.copy(isLoading = false, joinCodeForChildError = R.string.error_already_member)
                    return@launch
                }
                teamMemberRepository.addMember(
                    TeamMember(userId = childId, teamId = team.id, role = "player")
                )
                val parentUid = authRepository.currentUser?.uid
                if (parentUid != null && !teamMemberRepository.isMember(parentUid, team.id)) {
                    teamMemberRepository.addMember(
                        TeamMember(userId = parentUid, teamId = team.id, role = "parent")
                    )
                }
                Log.d(TAG, "onChildJoinTeam: child=$childId joined team=${team.id}")
                uiState = uiState.copy(isLoading = false, showJoinTeamForChildDialog = false)
                refreshChildRow(childId)
            }.onFailure { e ->
                Log.e(TAG, "onChildJoinTeam: failed", e)
                uiState = uiState.copy(isLoading = false, joinCodeForChildError = R.string.error_invalid_join_code)
            }
        }
    }

    override fun onRemoveChild() {
        val child = uiState.selectedChild ?: return
        uiState = uiState.copy(
            showChildActionSheet = false,
            selectedChild = null,
            childToRemove = child,
            showRemoveChildConfirm = true,
        )
    }

    override fun onRemoveChildConfirm() {
        val child = uiState.childToRemove ?: return
        val parentUid = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            runCatching {
                val childMemberships = teamMemberRepository.getMembersByUser(child.userId)
                val childTeamIds = childMemberships.map { it.teamId }.toSet()
                // Remove all team memberships for the child
                childMemberships.forEach { member ->
                    teamMemberRepository.removeMember(member.id)
                }
                // Remove parent from teams where no other managed child remains
                val teamsWithOtherChildren = uiState.managedChildren
                    .filter { it.userId != child.userId }
                    .flatMap { it.teams }
                    .map { it.teamId }
                    .toSet()
                val teamsToLeave = childTeamIds - teamsWithOtherChildren
                if (teamsToLeave.isNotEmpty()) {
                    teamMemberRepository.getMembersByUser(parentUid)
                        .filter { it.teamId in teamsToLeave && it.role == "parent" }
                        .forEach { teamMemberRepository.removeMember(it.id) }
                }
                // Unlink child from parent
                userRepository.removeChildFromParent(parentUid, child.userId)
                // Clear activeChildId if it was this child
                if (appPreferencesRepository.activeChildId == child.userId) {
                    appPreferencesRepository.activeChildId = ""
                }
                Log.d(TAG, "onRemoveChildConfirm: removed child=${child.userId}")
                uiState = uiState.copy(
                    managedChildren = uiState.managedChildren.filter { it.userId != child.userId },
                    activeChildId = if (uiState.activeChildId == child.userId) "" else uiState.activeChildId,
                    showRemoveChildConfirm = false,
                    childToRemove = null,
                )
            }.onFailure { e ->
                Log.e(TAG, "onRemoveChildConfirm: failed", e)
                uiState = uiState.copy(showRemoveChildConfirm = false, childToRemove = null)
            }
        }
    }

    override fun onRemoveChildDismiss() {
        uiState = uiState.copy(showRemoveChildConfirm = false, childToRemove = null)
    }

    override fun onChildLeaveTeam(membershipId: String) {
        val childId = uiState.selectedChild?.userId ?: return
        val teamId = uiState.selectedChild?.teams?.find { it.membershipId == membershipId }?.teamId
        viewModelScope.launch {
            runCatching {
                teamMemberRepository.removeMember(membershipId)
                if (teamId != null) {
                    val parentUid = authRepository.currentUser?.uid
                    if (parentUid != null) {
                        val otherChildStillInTeam = uiState.managedChildren
                            .filter { it.userId != childId }
                            .any { other -> other.teams.any { it.teamId == teamId } }
                        if (!otherChildStillInTeam) {
                            teamMemberRepository.getMembersByUser(parentUid)
                                .find { it.teamId == teamId && it.role == "parent" }
                                ?.let { teamMemberRepository.removeMember(it.id) }
                        }
                    }
                }
                Log.d(TAG, "onChildLeaveTeam: removed membershipId=$membershipId for child=$childId")
                refreshChildRow(childId)
                val updated = uiState.managedChildren.find { it.userId == childId }
                uiState = uiState.copy(selectedChild = updated)
            }.onFailure { e ->
                Log.e(TAG, "onChildLeaveTeam: failed", e)
            }
        }
    }

    private suspend fun refreshChildRow(childId: String) {
        val child = userRepository.getUser(childId) ?: return
        val teams = runCatching {
            teamMemberRepository.getMembersByUser(childId).mapNotNull { member ->
                val teamName = teamRepository.getTeam(member.teamId)?.name ?: return@mapNotNull null
                ChildTeamUiItem(membershipId = member.id, teamId = member.teamId, teamName = teamName)
            }
        }.getOrDefault(emptyList())
        val updated = ManagedChildUiItem(
            userId = child.id,
            displayName = child.displayName,
            avatarId = child.avatarId,
            teams = teams,
            isActive = child.id == appPreferencesRepository.activeChildId,
        )
        uiState = uiState.copy(
            managedChildren = uiState.managedChildren.map { if (it.userId == childId) updated else it },
            selectedChild = if (uiState.selectedChild?.userId == childId) updated else uiState.selectedChild,
        )
    }

    // ── Avatar ─────────────────────────────────────────────────────────────────

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

    // ── Memberships ────────────────────────────────────────────────────────────

    override fun onMembershipClick(item: MembershipRowUiItem) {
        if (uiState.isReadOnly) return
        appPreferencesRepository.activeClubId = item.clubId
        appPreferencesRepository.activeTeamId = if (item.teamId.isNullOrBlank()) "" else item.teamId
        Log.d(TAG, "onMembershipClick: clubId=${item.clubId}, role=${item.role}")
        when (item.role.lowercase()) {
            "admin" -> uiState = uiState.copy(navigateToClubManagement = true)
            "player" -> uiState = uiState.copy(navigateToPlayerDashboard = true)
            "coach" -> uiState = uiState.copy(navigateToCoachDashboard = true)
        }
    }

    override fun onJoinOrCreateTeam() {
        uiState = uiState.copy(navigateToSetup = true)
    }

    override fun onNavigationConsumed() {
        uiState = uiState.copy(
            navigateToClubManagement = false,
            navigateToCoachDashboard = false,
            navigateToPlayerDashboard = false,
            navigateToSetup = false,
        )
    }
}
