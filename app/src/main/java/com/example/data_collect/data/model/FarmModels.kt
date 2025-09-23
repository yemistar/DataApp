package com.example.data_collect.data.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val name: String,
    val role: String,
    val contact: String? = null,
)

@Serializable
data class Flock(
    val id: String,
    val name: String,
    val type: String,
    val startDate: String,
    val initialCount: Int,
    val notes: String? = null,
)

@Serializable
data class FeedLog(
    val id: String,
    val flockId: String,
    val date: String,
    val feedKg: Double,
    val feedType: String,
    val cost: Double,
    val notes: String? = null,
    val createdAt: String,
    val updatedAt: String? = null,
)

@Serializable
data class MortalityLog(
    val id: String,
    val flockId: String,
    val date: String,
    val count: Int,
    val cause: String,
    val notes: String? = null,
    val createdAt: String,
)

@Serializable
data class EggLog(
    val id: String,
    val flockId: String,
    val date: String,
    val collected: Int,
    val cracked: Int,
    val notes: String? = null,
    val createdAt: String,
)

@Serializable
data class TreatmentLog(
    val id: String,
    val flockId: String,
    val date: String,
    val treatment: String,
    val dosage: String,
    val administeredBy: String,
    val notes: String? = null,
    val createdAt: String,
)

@Serializable
data class EnvLog(
    val id: String,
    val flockId: String,
    val date: String,
    val temperatureC: Double,
    val humidityPercent: Double,
    val notes: String? = null,
    val createdAt: String,
)

@Serializable
data class PendingItem(
    val id: String,
    val kind: String,
    val payloadJson: String,
    val createdAt: String,
)

@Serializable
data class Logs(
    val feed: List<FeedLog> = emptyList(),
    val mortality: List<MortalityLog> = emptyList(),
    val eggs: List<EggLog> = emptyList(),
    val treatments: List<TreatmentLog> = emptyList(),
    val environment: List<EnvLog> = emptyList(),
)

@Serializable
data class AppState(
    val farmName: String = "Poultry Demo Farm",
    val users: List<User> = emptyList(),
    val flocks: List<Flock> = emptyList(),
    val logs: Logs = Logs(),
    val pendingQueue: List<PendingItem> = emptyList(),
    val selectedFlockId: String? = null,
    val lastSyncAt: String? = null,
)
