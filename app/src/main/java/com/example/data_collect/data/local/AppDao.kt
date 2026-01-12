package com.example.data_collect.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM app_meta WHERE id = 1")
    fun observeMeta(): Flow<AppMetaEntity?>

    @Query("SELECT * FROM app_meta WHERE id = 1")
    suspend fun getMeta(): AppMetaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMeta(meta: AppMetaEntity)

    @Query("SELECT * FROM users ORDER BY name")
    fun observeUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUsers(users: List<UserEntity>)

    @Query("SELECT * FROM flocks ORDER BY startDate DESC")
    fun observeFlocks(): Flow<List<FlockEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFlocks(flocks: List<FlockEntity>)

    @Query("SELECT COUNT(*) FROM flocks")
    suspend fun countFlocks(): Int

    @Query("SELECT COUNT(*) FROM users")
    suspend fun countUsers(): Int

    @Query("SELECT * FROM feed_logs ORDER BY date DESC")
    fun observeFeedLogs(): Flow<List<FeedLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFeedLogs(logs: List<FeedLogEntity>)

    @Query("SELECT * FROM mortality_logs ORDER BY date DESC")
    fun observeMortalityLogs(): Flow<List<MortalityLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMortalityLogs(logs: List<MortalityLogEntity>)

    @Query("SELECT * FROM egg_logs ORDER BY date DESC")
    fun observeEggLogs(): Flow<List<EggLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEggLogs(logs: List<EggLogEntity>)

    @Query("SELECT * FROM treatment_logs ORDER BY date DESC")
    fun observeTreatmentLogs(): Flow<List<TreatmentLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTreatmentLogs(logs: List<TreatmentLogEntity>)

    @Query("SELECT * FROM env_logs ORDER BY date DESC")
    fun observeEnvLogs(): Flow<List<EnvLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEnvLogs(logs: List<EnvLogEntity>)

    @Query("SELECT * FROM pending_items ORDER BY createdAt ASC")
    fun observePendingItems(): Flow<List<PendingItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPendingItems(items: List<PendingItemEntity>)

    @Query("DELETE FROM pending_items")
    suspend fun clearPendingItems()
}
