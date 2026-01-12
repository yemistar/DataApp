package com.example.data_collect.data

import android.content.Context
import android.net.Uri
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import androidx.room.withTransaction
import com.example.data_collect.BuildConfig
import com.example.data_collect.data.local.AppDatabase
import com.example.data_collect.data.local.AppMetaEntity
import com.example.data_collect.data.local.toDomain
import com.example.data_collect.data.local.toEntity
import com.example.data_collect.data.local.toMetaEntity
import com.example.data_collect.data.model.AppState
import com.example.data_collect.data.model.EggLog
import com.example.data_collect.data.model.EnvLog
import com.example.data_collect.data.model.FeedLog
import com.example.data_collect.data.model.Logs
import com.example.data_collect.data.model.MortalityLog
import com.example.data_collect.data.model.PendingItem
import com.example.data_collect.data.model.TreatmentLog
import com.example.data_collect.util.nowIso
import com.example.data_collect.util.uid
import java.nio.charset.Charset
import java.util.LinkedHashMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val DEFAULT_FARM_NAME = "Poultry Farm"
private const val DB_NAME = "poultry.db"

private val Context.legacyDataStore by preferencesDataStore(name = "poultry_demo_store")

class AppRepository(private val context: Context) {
    private val jsonKey = stringPreferencesKey("app_json")
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val db = Room.databaseBuilder(context, AppDatabase::class.java, DB_NAME).build()
    private val dao = db.appDao()

    private val logsFlow = combine(
        dao.observeFeedLogs(),
        dao.observeMortalityLogs(),
        dao.observeEggLogs(),
        dao.observeTreatmentLogs(),
        dao.observeEnvLogs()
    ) { feed, mortality, eggs, treatments, env ->
        Logs(
            feed = feed.map { it.toDomain() },
            mortality = mortality.map { it.toDomain() },
            eggs = eggs.map { it.toDomain() },
            treatments = treatments.map { it.toDomain() },
            environment = env.map { it.toDomain() },
        )
    }

    private val appStateFlow = combine(
        dao.observeMeta(),
        dao.observeUsers(),
        dao.observeFlocks(),
        logsFlow,
        dao.observePendingItems()
    ) { meta, users, flocks, logs, pending ->
        AppState(
            farmName = meta?.farmName ?: DEFAULT_FARM_NAME,
            users = users.map { it.toDomain() },
            flocks = flocks.map { it.toDomain() },
            logs = logs,
            pendingQueue = pending.map { it.toDomain() },
            selectedFlockId = meta?.selectedFlockId,
            lastSyncAt = meta?.lastSyncAt,
        )
    }

    val appState: StateFlow<AppState> = appStateFlow.stateIn(
        scope,
        SharingStarted.WhileSubscribed(5_000),
        AppState(farmName = DEFAULT_FARM_NAME)
    )

    init {
        scope.launch { ensureSeeded() }
    }

    suspend fun setSelectedFlock(flockId: String) {
        updateMeta { it.copy(selectedFlockId = flockId) }
    }

    suspend fun addFeed(
        flockId: String,
        date: String,
        feedKg: Double,
        feedType: String,
        cost: Double,
        notes: String?,
    ) {
        val feedLog = FeedLog(
            id = uid(),
            flockId = flockId,
            date = date,
            feedKg = feedKg,
            feedType = feedType,
            cost = cost,
            notes = notes,
            createdAt = nowIso(),
        )
        val pending = PendingItem(
            id = uid(),
            kind = "feed",
            payloadJson = json.encodeToString(feedLog),
            createdAt = nowIso(),
        )
        db.withTransaction {
            dao.upsertFeedLogs(listOf(feedLog.toEntity()))
            dao.upsertPendingItems(listOf(pending.toEntity()))
        }
    }

    suspend fun addMortality(
        flockId: String,
        date: String,
        count: Int,
        cause: String,
        notes: String?,
    ) {
        val mortalityLog = MortalityLog(
            id = uid(),
            flockId = flockId,
            date = date,
            count = count,
            cause = cause,
            notes = notes,
            createdAt = nowIso(),
        )
        val pending = PendingItem(
            id = uid(),
            kind = "mortality",
            payloadJson = json.encodeToString(mortalityLog),
            createdAt = nowIso(),
        )
        db.withTransaction {
            dao.upsertMortalityLogs(listOf(mortalityLog.toEntity()))
            dao.upsertPendingItems(listOf(pending.toEntity()))
        }
    }

    suspend fun addEggs(
        flockId: String,
        date: String,
        collected: Int,
        cracked: Int,
        notes: String?,
    ) {
        val eggLog = EggLog(
            id = uid(),
            flockId = flockId,
            date = date,
            collected = collected,
            cracked = cracked,
            notes = notes,
            createdAt = nowIso(),
        )
        val pending = PendingItem(
            id = uid(),
            kind = "eggs",
            payloadJson = json.encodeToString(eggLog),
            createdAt = nowIso(),
        )
        db.withTransaction {
            dao.upsertEggLogs(listOf(eggLog.toEntity()))
            dao.upsertPendingItems(listOf(pending.toEntity()))
        }
    }

    suspend fun addTreatment(
        flockId: String,
        date: String,
        treatment: String,
        dosage: String,
        administeredBy: String,
        notes: String?,
    ) {
        val treatmentLog = TreatmentLog(
            id = uid(),
            flockId = flockId,
            date = date,
            treatment = treatment,
            dosage = dosage,
            administeredBy = administeredBy,
            notes = notes,
            createdAt = nowIso(),
        )
        val pending = PendingItem(
            id = uid(),
            kind = "treatment",
            payloadJson = json.encodeToString(treatmentLog),
            createdAt = nowIso(),
        )
        db.withTransaction {
            dao.upsertTreatmentLogs(listOf(treatmentLog.toEntity()))
            dao.upsertPendingItems(listOf(pending.toEntity()))
        }
    }

    suspend fun addEnv(
        flockId: String,
        date: String,
        temperature: Double,
        humidity: Double,
        notes: String?,
    ) {
        val envLog = EnvLog(
            id = uid(),
            flockId = flockId,
            date = date,
            temperatureC = temperature,
            humidityPercent = humidity,
            notes = notes,
            createdAt = nowIso(),
        )
        val pending = PendingItem(
            id = uid(),
            kind = "environment",
            payloadJson = json.encodeToString(envLog),
            createdAt = nowIso(),
        )
        db.withTransaction {
            dao.upsertEnvLogs(listOf(envLog.toEntity()))
            dao.upsertPendingItems(listOf(pending.toEntity()))
        }
    }

    suspend fun simulateSync() {
        db.withTransaction {
            dao.clearPendingItems()
            updateMeta { it.copy(lastSyncAt = nowIso()) }
        }
    }

    suspend fun exportToUri(uri: Uri): Boolean {
        val state = appState.first()
        return withContext(Dispatchers.IO) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { output ->
                    output.write(json.encodeToString(state).toByteArray(Charset.forName("UTF-8")))
                    output.flush()
                } ?: error("Unable to open stream")
            }.isSuccess
        }
    }

    suspend fun importFromUri(uri: Uri): Boolean {
        val payload = withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
        } ?: return false
        val incoming = runCatching { json.decodeFromString<AppState>(payload) }.getOrElse { return false }
        val current = appState.first()
        val merged = mergeAppState(current, incoming)
        replaceAll(merged)
        return true
    }

    private suspend fun ensureSeeded() {
        if (migrateFromLegacyIfNeeded()) {
            return
        }
        val hasData = dao.countFlocks() > 0 || dao.countUsers() > 0
        val meta = dao.getMeta()
        if (!hasData) {
            if (BuildConfig.DEBUG) {
                replaceAll(Seeds.defaultAppState())
            } else if (meta == null) {
                dao.upsertMeta(defaultMeta())
            }
        } else if (meta == null) {
            dao.upsertMeta(defaultMeta())
        }
    }

    private suspend fun migrateFromLegacyIfNeeded(): Boolean {
        val prefs = context.legacyDataStore.data.first()
        val stored = prefs[jsonKey] ?: return false
        val legacy = runCatching { json.decodeFromString<AppState>(stored) }.getOrNull() ?: return false
        replaceAll(legacy)
        context.legacyDataStore.edit { it.remove(jsonKey) }
        return true
    }

    private suspend fun updateMeta(transform: (AppMetaEntity) -> AppMetaEntity) {
        val current = dao.getMeta() ?: defaultMeta()
        dao.upsertMeta(transform(current))
    }

    private fun defaultMeta(): AppMetaEntity = AppMetaEntity(
        farmName = DEFAULT_FARM_NAME,
        selectedFlockId = null,
        lastSyncAt = null
    )

    private suspend fun replaceAll(state: AppState) {
        db.withTransaction {
            dao.upsertMeta(state.toMetaEntity())
            dao.upsertUsers(state.users.map { it.toEntity() })
            dao.upsertFlocks(state.flocks.map { it.toEntity() })
            dao.upsertFeedLogs(state.logs.feed.map { it.toEntity() })
            dao.upsertMortalityLogs(state.logs.mortality.map { it.toEntity() })
            dao.upsertEggLogs(state.logs.eggs.map { it.toEntity() })
            dao.upsertTreatmentLogs(state.logs.treatments.map { it.toEntity() })
            dao.upsertEnvLogs(state.logs.environment.map { it.toEntity() })
            dao.upsertPendingItems(state.pendingQueue.map { it.toEntity() })
        }
    }

    private fun mergeAppState(current: AppState, incoming: AppState): AppState {
        fun <T> mergeLists(existing: List<T>, new: List<T>, idSelector: (T) -> String): List<T> {
            val map = LinkedHashMap<String, T>()
            existing.forEach { map[idSelector(it)] = it }
            new.forEach { item -> map.putIfAbsent(idSelector(item), item) }
            return map.values.toList()
        }

        return current.copy(
            users = mergeLists(current.users, incoming.users) { it.id },
            flocks = mergeLists(current.flocks, incoming.flocks) { it.id },
            logs = current.logs.copy(
                feed = mergeLists(current.logs.feed, incoming.logs.feed) { it.id },
                mortality = mergeLists(current.logs.mortality, incoming.logs.mortality) { it.id },
                eggs = mergeLists(current.logs.eggs, incoming.logs.eggs) { it.id },
                treatments = mergeLists(current.logs.treatments, incoming.logs.treatments) { it.id },
                environment = mergeLists(current.logs.environment, incoming.logs.environment) { it.id },
            ),
            pendingQueue = mergeLists(current.pendingQueue, incoming.pendingQueue) { it.id },
            lastSyncAt = current.lastSyncAt ?: incoming.lastSyncAt,
            selectedFlockId = current.selectedFlockId ?: incoming.selectedFlockId,
        )
    }
}
