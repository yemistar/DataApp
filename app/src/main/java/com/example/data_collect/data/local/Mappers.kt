package com.example.data_collect.data.local

import com.example.data_collect.data.model.AppState
import com.example.data_collect.data.model.EggLog
import com.example.data_collect.data.model.EnvLog
import com.example.data_collect.data.model.FeedLog
import com.example.data_collect.data.model.Flock
import com.example.data_collect.data.model.MortalityLog
import com.example.data_collect.data.model.PendingItem
import com.example.data_collect.data.model.TreatmentLog
import com.example.data_collect.data.model.User

fun AppMetaEntity.toDomain(): AppState = AppState(
    farmName = farmName,
    users = emptyList(),
    flocks = emptyList(),
    selectedFlockId = selectedFlockId,
    lastSyncAt = lastSyncAt
)

fun AppState.toMetaEntity(): AppMetaEntity = AppMetaEntity(
    id = 1,
    farmName = farmName,
    selectedFlockId = selectedFlockId,
    lastSyncAt = lastSyncAt
)

fun UserEntity.toDomain(): User = User(
    id = id,
    name = name,
    role = role,
    contact = contact
)

fun User.toEntity(): UserEntity = UserEntity(
    id = id,
    name = name,
    role = role,
    contact = contact
)

fun FlockEntity.toDomain(): Flock = Flock(
    id = id,
    name = name,
    type = type,
    startDate = startDate,
    initialCount = initialCount,
    notes = notes
)

fun Flock.toEntity(): FlockEntity = FlockEntity(
    id = id,
    name = name,
    type = type,
    startDate = startDate,
    initialCount = initialCount,
    notes = notes
)

fun FeedLogEntity.toDomain(): FeedLog = FeedLog(
    id = id,
    flockId = flockId,
    date = date,
    feedKg = feedKg,
    feedType = feedType,
    cost = cost,
    notes = notes,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun FeedLog.toEntity(): FeedLogEntity = FeedLogEntity(
    id = id,
    flockId = flockId,
    date = date,
    feedKg = feedKg,
    feedType = feedType,
    cost = cost,
    notes = notes,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun MortalityLogEntity.toDomain(): MortalityLog = MortalityLog(
    id = id,
    flockId = flockId,
    date = date,
    count = count,
    cause = cause,
    notes = notes,
    createdAt = createdAt
)

fun MortalityLog.toEntity(): MortalityLogEntity = MortalityLogEntity(
    id = id,
    flockId = flockId,
    date = date,
    count = count,
    cause = cause,
    notes = notes,
    createdAt = createdAt
)

fun EggLogEntity.toDomain(): EggLog = EggLog(
    id = id,
    flockId = flockId,
    date = date,
    collected = collected,
    cracked = cracked,
    notes = notes,
    createdAt = createdAt
)

fun EggLog.toEntity(): EggLogEntity = EggLogEntity(
    id = id,
    flockId = flockId,
    date = date,
    collected = collected,
    cracked = cracked,
    notes = notes,
    createdAt = createdAt
)

fun TreatmentLogEntity.toDomain(): TreatmentLog = TreatmentLog(
    id = id,
    flockId = flockId,
    date = date,
    treatment = treatment,
    dosage = dosage,
    administeredBy = administeredBy,
    notes = notes,
    createdAt = createdAt
)

fun TreatmentLog.toEntity(): TreatmentLogEntity = TreatmentLogEntity(
    id = id,
    flockId = flockId,
    date = date,
    treatment = treatment,
    dosage = dosage,
    administeredBy = administeredBy,
    notes = notes,
    createdAt = createdAt
)

fun EnvLogEntity.toDomain(): EnvLog = EnvLog(
    id = id,
    flockId = flockId,
    date = date,
    temperatureC = temperatureC,
    humidityPercent = humidityPercent,
    notes = notes,
    createdAt = createdAt
)

fun EnvLog.toEntity(): EnvLogEntity = EnvLogEntity(
    id = id,
    flockId = flockId,
    date = date,
    temperatureC = temperatureC,
    humidityPercent = humidityPercent,
    notes = notes,
    createdAt = createdAt
)

fun PendingItemEntity.toDomain(): PendingItem = PendingItem(
    id = id,
    kind = kind,
    payloadJson = payloadJson,
    createdAt = createdAt
)

fun PendingItem.toEntity(): PendingItemEntity = PendingItemEntity(
    id = id,
    kind = kind,
    payloadJson = payloadJson,
    createdAt = createdAt
)
