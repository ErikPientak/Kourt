package com.kourt.app.viewmodel.event

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kourt.app.R
import com.kourt.app.data.repository.remote.EventRepository
import com.kourt.app.data.repository.remote.MatchStatsRepository
import com.kourt.app.data.repository.remote.TeamMemberRepository
import com.kourt.app.data.repository.remote.UserRepository
import com.kourt.app.ui.screens.event.aftermatch.AfterMatchStatsScreenUiState
import com.kourt.app.ui.screens.event.aftermatch.MatchOutcome
import com.kourt.app.ui.screens.event.aftermatch.PlayerStatRowItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

private const val TAG = "AfterMatchStatsViewModel"

@HiltViewModel
class AfterMatchStatsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val eventRepository: EventRepository,
    private val matchStatsRepository: MatchStatsRepository,
    private val teamMemberRepository: TeamMemberRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    val eventId: String = savedStateHandle["eventId"] ?: ""

    var uiState by mutableStateOf(AfterMatchStatsScreenUiState())
        private set

    init { loadData() }

    // ── Data loading ──────────────────────────────────────────────────────────

    fun loadData() {
        if (eventId.isBlank()) {
            uiState = uiState.copy(isLoading = false, error = R.string.error_load_failed)
            return
        }

        viewModelScope.launch {
            // Full-screen spinner only before the first load; refreshing on return keeps the screen.
            uiState = uiState.copy(isLoading = uiState.eventTitle.isEmpty(), error = null)

            runCatching {
                coroutineScope {
                    val eventDeferred      = async { eventRepository.getEvent(eventId) }
                    val matchStatsDeferred = async { matchStatsRepository.getMatchStats(eventId) }

                    val event      = eventDeferred.await()
                    val matchStats = matchStatsDeferred.await()

                    if (event == null) {
                        uiState = uiState.copy(isLoading = false, error = R.string.error_load_failed)
                        return@coroutineScope
                    }

                    if (matchStats == null) {
                        uiState = uiState.copy(isLoading = false, error = R.string.error_load_failed)
                        return@coroutineScope
                    }

                    // Format event date from Timestamp
                    val eventDateMillis = event.date.seconds * 1000L
                    val formattedDate = SimpleDateFormat("MMM d", Locale.getDefault())
                        .format(Date(eventDateMillis))

                    // Resolve event title: use opponent if available, otherwise event title
                    val opponentName = event.opponent.ifBlank { event.title }

                    // Load team members and resolve user display names
                    val allMembers = teamMemberRepository.getMembersByTeam(event.teamId)
                    val memberById = allMembers.associateBy { it.id }

                    val statEntryMemberIds = matchStats.playerStats.keys.toList()
                    val memberUserIds = statEntryMemberIds.mapNotNull { memberId ->
                        memberById[memberId]?.userId
                    }.filter { it.isNotBlank() }

                    val usersById = if (memberUserIds.isNotEmpty()) {
                        userRepository.getUsersByIds(memberUserIds).associateBy { it.id }
                    } else emptyMap()

                    val playerItems = matchStats.playerStats.entries.mapNotNull { (memberId, stat) ->
                        val member = memberById[memberId] ?: return@mapNotNull null
                        val user = usersById[member.userId] ?: return@mapNotNull null
                        PlayerStatRowItem(
                            teamMemberId = memberId,
                            displayName = user.displayName.ifBlank { user.email },
                            avatarUrl = user.avatarId.ifBlank { user.photoURL },
                            points = stat.points,
                            rebounds = stat.rebounds,
                            assists = stat.assists,
                            fouls = stat.fouls,
                            freeThrowsAttempted = stat.freeThrowsAttempted,
                            freeThrowsMade = stat.freeThrowsMade,
                        )
                    }.sortedByDescending { it.score }

                    val mvp = pickMvp(playerItems)
                    val outcome = matchOutcome(matchStats.myTeamScore, matchStats.opponentScore)

                    uiState = uiState.copy(
                        isLoading = false,
                        eventTitle = event.title,
                        opponentName = opponentName,
                        eventDate = formattedDate,
                        myTeamScore = matchStats.myTeamScore,
                        opponentScore = matchStats.opponentScore,
                        outcome = outcome,
                        mvpItem = mvp,
                        players = playerItems,
                    )
                    Log.d(TAG, "Loaded after-match stats for event $eventId: ${playerItems.size} players, outcome=$outcome")
                }
            }.onFailure { e ->
                Log.e(TAG, "Failed to load after-match stats for $eventId", e)
                uiState = uiState.copy(isLoading = false, error = R.string.error_load_failed)
            }
        }
    }
}

internal fun matchOutcome(myTeamScore: Int, opponentScore: Int): MatchOutcome = when {
    myTeamScore > opponentScore -> MatchOutcome.WIN
    myTeamScore < opponentScore -> MatchOutcome.LOSS
    else -> MatchOutcome.DRAW
}

/**
 * Player of the match: the one highest score (points + rebounds + assists).
 * Nobody when no one scored, or when the top score is shared, so the card never picks at random.
 */
internal fun pickMvp(players: List<PlayerStatRowItem>): PlayerStatRowItem? {
    val top = players.maxOfOrNull { it.score } ?: return null
    if (top == 0) return null
    return players.singleOrNull { it.score == top }
}
