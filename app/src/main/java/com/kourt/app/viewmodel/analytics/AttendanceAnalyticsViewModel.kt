package com.kourt.app.viewmodel.analytics

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kourt.app.R
import com.kourt.app.data.repository.local.AppPreferencesRepository
import com.kourt.app.data.repository.remote.AttendanceRepository
import com.kourt.app.data.repository.remote.EventRepository
import com.kourt.app.data.repository.remote.PlayerStatsRepository
import com.kourt.app.data.repository.remote.TeamMemberRepository
import com.kourt.app.data.repository.remote.UserRepository
import com.kourt.app.ui.screens.analytics.attendance.AttendanceAnalyticsScreenUiState
import com.kourt.app.ui.screens.analytics.attendance.PlayerAttendanceItem
import com.kourt.app.ui.screens.analytics.attendance.WeeklyAttendanceBar
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.IsoFields
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val TAG = "AttendanceAnalyticsVM"
private const val WINDOW_DAYS = 56L   // 8 ISO weeks
private const val MAX_BARS    = 8

@HiltViewModel
class AttendanceAnalyticsViewModel @Inject constructor(
    private val appPreferencesRepository: AppPreferencesRepository,
    private val eventRepository: EventRepository,
    private val attendanceRepository: AttendanceRepository,
    private val playerStatsRepository: PlayerStatsRepository,
    private val teamMemberRepository: TeamMemberRepository,
    private val userRepository: UserRepository,
) : ViewModel(){

    var uiState by mutableStateOf(AttendanceAnalyticsScreenUiState())
        private set

    init {
        loadAnalytics()
    }

    // ── Loading ───────────────────────────────────────────────────────────────

    private fun loadAnalytics() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)

            runCatching {
                val teamId = appPreferencesRepository.activeTeamId
                if (teamId.isBlank()) {
                    Log.w(TAG, "No active team set")
                    uiState = uiState.copy(isLoading = false, error = R.string.error_no_team)
                    return@runCatching
                }

                Log.d(TAG, "Loading attendance analytics for team $teamId")

                // Parallel fetch: player stats, members, events
                val statsDeferred   = async { playerStatsRepository.getPlayersStatsByTeam(teamId) }
                val membersDeferred = async { teamMemberRepository.getMembersByTeam(teamId) }
                val eventsDeferred  = async { eventRepository.getEventsByTeam(teamId) }

                val stats   = statsDeferred.await()
                val members = membersDeferred.await()
                val events  = eventsDeferred.await()

                Log.d(TAG, "Fetched ${stats.size} stats, ${members.size} members, ${events.size} events")

                // Resolve user display names and avatars
                val playerMembers = members.filter { it.role == "player" }
                val userIds = playerMembers.map { it.userId }.filter { it.isNotBlank() }
                val users   = if (userIds.isNotEmpty()) userRepository.getUsersByIds(userIds) else emptyList()
                val userMap = users.associateBy { it.id }

                // ── Player attendance items ────────────────────────────────
                val playerItems = stats.mapNotNull { ps ->
                    val member = playerMembers.firstOrNull { it.id == ps.teamMemberId } ?: return@mapNotNull null
                    val user   = userMap[member.userId]

                    val onTime   = ps.totalTrainingsOnTime
                    val late     = ps.totalTrainingsLate
                    val excused  = ps.totalTrainingsExcused
                    val unexcused = ps.totalTrainingsUnexcused
                    val total    = (onTime + late + excused + unexcused).toFloat()

                    if (total == 0f) return@mapNotNull null

                    val attendanceRate = (onTime + late) / total
                    val onTimeRate     = onTime / total
                    val lateRate       = late / total
                    val absentRate     = (excused + unexcused) / total

                    val reliabilityLabel = when {
                        attendanceRate >= 0.9f -> "EXCELLENT"
                        attendanceRate >= 0.7f -> "CONSISTENT"
                        else                   -> "NEEDS ATTENTION"
                    }

                    PlayerAttendanceItem(
                        teamMemberId   = ps.teamMemberId,
                        displayName    = user?.displayName?.ifBlank { user.email }
                            ?: member.id,
                        avatarUrl      = user?.avatarId?.ifBlank { user.photoURL } ?: "",
                        attendanceRate = attendanceRate,
                        onTimeRate     = onTimeRate,
                        lateRate       = lateRate,
                        absentRate     = absentRate,
                        reliabilityLabel = reliabilityLabel,
                    )
                }.sortedByDescending { it.attendanceRate }

                val teamAttendanceRate = if (playerItems.isNotEmpty())
                    playerItems.map { it.attendanceRate }.average().toFloat()
                else 0f

                val teamOnTimeRate = if (playerItems.isNotEmpty())
                    playerItems.map { it.onTimeRate }.average().toFloat()
                else 0f

                val teamLateRate = if (playerItems.isNotEmpty())
                    playerItems.map { it.lateRate }.average().toFloat()
                else 0f

                val teamAbsentRate = if (playerItems.isNotEmpty())
                    playerItems.map { it.absentRate }.average().toFloat()
                else 0f

                val mostReliablePlayer = playerItems.maxByOrNull { it.attendanceRate }

                Log.d(TAG, "Built ${playerItems.size} player items, team rate=${teamAttendanceRate}")

                // ── Weekly trend bars ─────────────────────────────────────
                val todayEpoch = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())
                val windowStart = todayEpoch - WINDOW_DAYS

                val recentEvents = events.filter { event ->
                    val eventEpochDay = TimeUnit.SECONDS.toDays(event.date.seconds)
                    eventEpochDay in windowStart..todayEpoch
                }

                Log.d(TAG, "Recent events in window: ${recentEvents.size}")

                // Fetch attendance for all recent events in parallel
                val attendanceByEvent = recentEvents.map { event ->
                    async {
                        val records = runCatching {
                            attendanceRepository.getAttendanceByEvent(event.id)
                        }.getOrElse { e ->
                            Log.w(TAG, "Failed to fetch attendance for event ${event.id}", e)
                            emptyList()
                        }
                        event.id to records
                    }
                }.awaitAll().toMap()

                val memberCount = playerMembers.size.coerceAtLeast(1)

                // Group by ISO week bucket (epochDay / 7)
                val weekBuckets = recentEvents.groupBy { event ->
                    val eventEpochDay = TimeUnit.SECONDS.toDays(event.date.seconds)
                    eventEpochDay / 7
                }

                val currentWeekBucket = todayEpoch / 7

                val trendBars = weekBuckets.entries
                    .sortedBy { it.key }
                    .takeLast(MAX_BARS)
                    .map { (weekKey, weekEvents) ->
                        val totalPresent = weekEvents.sumOf { event ->
                            val records = attendanceByEvent[event.id] ?: emptyList()
                            records.count { it.status == "on_time" || it.status == "late" }
                        }
                        val totalPossible = memberCount * weekEvents.size
                        val weekRate = if (totalPossible > 0)
                            (totalPresent.toFloat() / totalPossible).coerceIn(0f, 1f)
                        else 0f

                        val isoWeek = LocalDate.ofEpochDay(weekKey * 7L)
                            .get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
                        WeeklyAttendanceBar(
                            weekLabel     = "W$isoWeek",
                            rate          = weekRate,
                            isCurrentWeek = weekKey == currentWeekBucket,
                        )
                    }

                Log.d(TAG, "Built ${trendBars.size} trend bars")

                // ── Trend delta ───────────────────────────────────────────
                val trendDelta: Float
                val isTrendImproving: Boolean

                if (trendBars.size >= 2) {
                    val prev = trendBars[trendBars.lastIndex - 1].rate
                    val curr = trendBars[trendBars.lastIndex].rate
                    trendDelta = curr - prev
                    isTrendImproving = trendDelta >= 0f
                } else {
                    trendDelta = 0f
                    isTrendImproving = true
                }

                uiState = uiState.copy(
                    isLoading          = false,
                    error              = null,
                    teamAttendanceRate = teamAttendanceRate,
                    teamOnTimeRate     = teamOnTimeRate,
                    teamLateRate       = teamLateRate,
                    teamAbsentRate     = teamAbsentRate,
                    mostReliablePlayer = mostReliablePlayer,
                    trendBars          = trendBars,
                    trendDelta         = trendDelta,
                    isTrendImproving   = isTrendImproving,
                    players            = playerItems,
                )

                Log.d(TAG, "Analytics load complete")

            }.onFailure { e ->
                Log.e(TAG, "Failed to load attendance analytics", e)
                uiState = uiState.copy(isLoading = false, error = R.string.error_generic)
            }
        }
    }
}
