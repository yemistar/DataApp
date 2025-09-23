package com.example.data_collect.data

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data_collect.data.model.AppState
import com.example.data_collect.data.model.EggLog
import com.example.data_collect.data.model.EnvLog
import com.example.data_collect.data.model.FeedLog
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.poultryDataStore by preferencesDataStore(name = "poultry_demo_store")

@RequiresApi(Build.VERSION_CODES.O)
class AppRepository(private val context: Context) {
    private val jsonKey = stringPreferencesKey("app_json")
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()

    private val _appState = MutableStateFlow(Seeds.defaultAppState())
    val appState: StateFlow<AppState> = _appState.asStateFlow()

    init {
        scope.launch {
            val prefs = context.poultryDataStore.data.first()
            val stored = prefs[jsonKey]
            val initial = if (stored.isNullOrBlank()) {
                val seeded = Seeds.defaultAppState()
                context.poultryDataStore.edit { it[jsonKey] = json.encodeToString(seeded) }
                seeded
            } else {
                runCatching { json.decodeFromString<AppState>(stored) }.getOrElse {
                    val seeded = Seeds.defaultAppState()
                    context.poultryDataStore.edit { it[jsonKey] = json.encodeToString(seeded) }
                    seeded
                }
            }
            _appState.value = initial
        }
    }

    private suspend fun persist(state: AppState) {
        withContext(Dispatchers.IO) {
            context.poultryDataStore.edit { prefs ->
                prefs[jsonKey] = json.encodeToString(state)
            }
        }
    }

    private suspend fun update(transform: (AppState) -> AppState) {
        mutex.withLock {
            val newState = transform(_appState.value)
            _appState.value = newState
            persist(newState)
        }
    }

    suspend fun setSelectedFlock(flockId: String) {
        update { it.copy(selectedFlockId = flockId) }
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
        update { state ->
            state.copy(
                logs = state.logs.copy(feed = listOf(feedLog) + state.logs.feed),
                pendingQueue = listOf(pending) + state.pendingQueue,
            )
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
        update { state ->
            state.copy(
                logs = state.logs.copy(mortality = listOf(mortalityLog) + state.logs.mortality),
                pendingQueue = listOf(pending) + state.pendingQueue,
            )
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
        update { state ->
            state.copy(
                logs = state.logs.copy(eggs = listOf(eggLog) + state.logs.eggs),
                pendingQueue = listOf(pending) + state.pendingQueue,
            )
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
        update { state ->
            state.copy(
                logs = state.logs.copy(treatments = listOf(treatmentLog) + state.logs.treatments),
                pendingQueue = listOf(pending) + state.pendingQueue,
            )
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
        update { state ->
            state.copy(
                logs = state.logs.copy(environment = listOf(envLog) + state.logs.environment),
                pendingQueue = listOf(pending) + state.pendingQueue,
            )
        }
    }

    suspend fun simulateSync() {
        update { it.copy(pendingQueue = emptyList(), lastSyncAt = nowIso()) }
    }

    suspend fun exportToUri(uri: Uri): Boolean {
        val state = _appState.value
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
        update { mergeAppState(it, incoming) }
        return true
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
