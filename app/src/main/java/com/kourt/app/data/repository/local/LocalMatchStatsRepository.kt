package com.kourt.app.data.repository.local

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kourt.app.data.local.PendingMatchStatsDao
import com.kourt.app.data.local.PendingMatchStatsEntity
import com.kourt.app.data.model.PlayerMatchStat
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalMatchStatsRepository @Inject constructor(
    private val dao: PendingMatchStatsDao,
) {
    private val gson = Gson()
    private val mapType = object : TypeToken<Map<String, PlayerMatchStat>>() {}.type

    suspend fun saveDraft(entity: PendingMatchStatsEntity) = dao.upsert(entity)

    suspend fun getDraft(eventId: String): PendingMatchStatsEntity? = dao.getByEventId(eventId)

    suspend fun getAllPending(): List<PendingMatchStatsEntity> = dao.getAllPending()

    suspend fun deleteDraft(eventId: String) = dao.deleteByEventId(eventId)

    fun serializePlayerStats(stats: Map<String, PlayerMatchStat>): String = gson.toJson(stats)

    fun deserializePlayerStats(json: String): Map<String, PlayerMatchStat> =
        gson.fromJson(json, mapType)
}
