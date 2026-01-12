package com.example.data_collect.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "app_meta")
data class AppMetaEntity(
    @PrimaryKey val id: Int = 1,
    val farmName: String,
    val selectedFlockId: String?,
    val lastSyncAt: String?
)

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val role: String,
    val contact: String?
)

@Entity(tableName = "flocks")
data class FlockEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String,
    val startDate: String,
    val initialCount: Int,
    val notes: String?
)

@Entity(
    tableName = "feed_logs",
    indices = [Index(value = ["flockId"]), Index(value = ["date"])]
)
data class FeedLogEntity(
    @PrimaryKey val id: String,
    val flockId: String,
    val date: String,
    val feedKg: Double,
    val feedType: String,
    val cost: Double,
    val notes: String?,
    val createdAt: String,
    val updatedAt: String?
)

@Entity(
    tableName = "mortality_logs",
    indices = [Index(value = ["flockId"]), Index(value = ["date"])]
)
data class MortalityLogEntity(
    @PrimaryKey val id: String,
    val flockId: String,
    val date: String,
    val count: Int,
    val cause: String,
    val notes: String?,
    val createdAt: String
)

@Entity(
    tableName = "egg_logs",
    indices = [Index(value = ["flockId"]), Index(value = ["date"])]
)
data class EggLogEntity(
    @PrimaryKey val id: String,
    val flockId: String,
    val date: String,
    val collected: Int,
    val cracked: Int,
    val notes: String?,
    val createdAt: String
)

@Entity(
    tableName = "treatment_logs",
    indices = [Index(value = ["flockId"]), Index(value = ["date"])]
)
data class TreatmentLogEntity(
    @PrimaryKey val id: String,
    val flockId: String,
    val date: String,
    val treatment: String,
    val dosage: String,
    val administeredBy: String,
    val notes: String?,
    val createdAt: String
)

@Entity(
    tableName = "env_logs",
    indices = [Index(value = ["flockId"]), Index(value = ["date"])]
)
data class EnvLogEntity(
    @PrimaryKey val id: String,
    val flockId: String,
    val date: String,
    val temperatureC: Double,
    val humidityPercent: Double,
    val notes: String?,
    val createdAt: String
)

@Entity(tableName = "pending_items")
data class PendingItemEntity(
    @PrimaryKey val id: String,
    val kind: String,
    val payloadJson: String,
    val createdAt: String
)
