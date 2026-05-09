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
import org.junit.Assert.assertEquals
import org.junit.Test

class MappersTest {
    @Test
    fun appStateToMetaEntityPreservesSyncAndSelectionFields() {
        val state = AppState(
            farmName = "Ogun Poultry",
            selectedFlockId = "flock-1",
            lastSyncAt = "2026-05-08T10:15:30Z",
        )

        val entity = state.toMetaEntity()

        assertEquals(1, entity.id)
        assertEquals("Ogun Poultry", entity.farmName)
        assertEquals("flock-1", entity.selectedFlockId)
        assertEquals("2026-05-08T10:15:30Z", entity.lastSyncAt)
    }

    @Test
    fun domainModelsRoundTripThroughRoomEntities() {
        val user = User(
            id = "user-1",
            name = "Farm Manager",
            role = "manager",
            contact = "0800-000-0000",
        )
        val flock = Flock(
            id = "flock-1",
            name = "Flock A",
            type = "broilers",
            startDate = "2026-04-24",
            initialCount = 200,
            notes = "Starter batch",
        )
        val feed = FeedLog(
            id = "feed-1",
            flockId = "flock-1",
            date = "2026-05-01",
            feedKg = 18.5,
            feedType = "Starter",
            cost = 14200.0,
            notes = "Morning feed",
            createdAt = "2026-05-01T08:00:00Z",
            updatedAt = "2026-05-01T09:00:00Z",
        )
        val mortality = MortalityLog(
            id = "mortality-1",
            flockId = "flock-1",
            date = "2026-05-02",
            count = 1,
            cause = "Weakness",
            notes = "Separated for review",
            createdAt = "2026-05-02T08:00:00Z",
        )
        val eggs = EggLog(
            id = "eggs-1",
            flockId = "flock-1",
            date = "2026-05-03",
            collected = 120,
            cracked = 3,
            notes = "Layer pen",
            createdAt = "2026-05-03T08:00:00Z",
        )
        val treatment = TreatmentLog(
            id = "treatment-1",
            flockId = "flock-1",
            date = "2026-05-04",
            treatment = "Multi-vitamin",
            dosage = "10 ml",
            administeredBy = "Farm Vet",
            notes = "After feed",
            createdAt = "2026-05-04T08:00:00Z",
        )
        val environment = EnvLog(
            id = "env-1",
            flockId = "flock-1",
            date = "2026-05-05",
            temperatureC = 29.5,
            humidityPercent = 66.0,
            notes = "Normal",
            createdAt = "2026-05-05T08:00:00Z",
        )
        val pending = PendingItem(
            id = "pending-1",
            kind = "feed",
            payloadJson = "{\"id\":\"feed-1\"}",
            createdAt = "2026-05-01T08:00:00Z",
        )

        assertEquals(user, user.toEntity().toDomain())
        assertEquals(flock, flock.toEntity().toDomain())
        assertEquals(feed, feed.toEntity().toDomain())
        assertEquals(mortality, mortality.toEntity().toDomain())
        assertEquals(eggs, eggs.toEntity().toDomain())
        assertEquals(treatment, treatment.toEntity().toDomain())
        assertEquals(environment, environment.toEntity().toDomain())
        assertEquals(pending, pending.toEntity().toDomain())
    }
}
