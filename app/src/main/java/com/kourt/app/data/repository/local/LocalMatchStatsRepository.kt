package com.kourt.app.data.repository.local

import com.kourt.app.data.local.PendingMatchStatsDao
import com.kourt.app.data.local.PendingMatchStatsEntity
import com.kourt.app.data.model.PlayerMatchStat
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalMatchStatsRepository @Inject constructor(
    private val dao: PendingMatchStatsDao,
) {
    suspend fun saveDraft(entity: PendingMatchStatsEntity) = dao.upsert(entity)

    suspend fun getDraft(eventId: String): PendingMatchStatsEntity? = dao.getByEventId(eventId)

    suspend fun getAllPending(): List<PendingMatchStatsEntity> = dao.getAllPending()

    suspend fun deleteDraft(eventId: String) = dao.deleteByEventId(eventId)

    fun serializePlayerStats(stats: Map<String, PlayerMatchStat>): String {
        val root = JSONObject()
        stats.forEach { (memberId, stat) ->
            root.put(memberId, JSONObject().apply {
                put("points", stat.points)
                put("rebounds", stat.rebounds)
                put("assists", stat.assists)
                put("fouls", stat.fouls)
                put("minutesPlayed", stat.minutesPlayed)
                put("freeThrowsAttempted", stat.freeThrowsAttempted)
                put("freeThrowsMade", stat.freeThrowsMade)
            })
        }
        return root.toString()
    }

    fun deserializePlayerStats(json: String): Map<String, PlayerMatchStat> {
        val root = JSONObject(json)
        return buildMap {
            root.keys().forEach { memberId ->
                val s = root.getJSONObject(memberId)
                put(memberId, PlayerMatchStat(
                    points              = s.getInt("points"),
                    rebounds            = s.getInt("rebounds"),
                    assists             = s.getInt("assists"),
                    fouls               = s.getInt("fouls"),
                    minutesPlayed       = s.getDouble("minutesPlayed"),
                    freeThrowsAttempted = s.getInt("freeThrowsAttempted"),
                    freeThrowsMade      = s.getInt("freeThrowsMade"),
                ))
            }
        }
    }
}
