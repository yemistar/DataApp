package com.example.data_collect.data

import com.example.data_collect.data.model.AppState
import com.example.data_collect.data.model.EggLog
import com.example.data_collect.data.model.EnvLog
import com.example.data_collect.data.model.FeedLog
import com.example.data_collect.data.model.Flock
import com.example.data_collect.data.model.Logs
import com.example.data_collect.data.model.MortalityLog
import com.example.data_collect.data.model.TreatmentLog
import com.example.data_collect.data.model.User
import com.example.data_collect.util.nowIso
import com.example.data_collect.util.uid
import java.time.LocalDate

object Seeds {
    fun defaultAppState(): AppState {
        val flockId = uid()
        val layerFlockId = uid()
        val today = LocalDate.now()
        val startDate = today.minusDays(14)
        val flock = Flock(
            id = flockId,
            name = "Flock A",
            type = "broilers",
            startDate = startDate.toString(),
            initialCount = 200,
            notes = "Starter batch",
        )
        val layerFlock = Flock(
            id = layerFlockId,
            name = "Layer House",
            type = "layers",
            startDate = today.minusDays(62).toString(),
            initialCount = 120,
            notes = "Demo layer flock for egg production logs",
        )

        val broilerFeedLogs = (0 until 10).map { index ->
            val date = today.minusDays((9 - index).toLong()).toString()
            FeedLog(
                id = uid(),
                flockId = flockId,
                date = date,
                feedKg = 18.0 + (0.5 * index),
                feedType = "Starter",
                cost = 14000.0 + (200 * index),
                createdAt = nowIso(),
            )
        }
        val layerFeedLogs = (0 until 7).map { index ->
            val date = today.minusDays((6 - index).toLong()).toString()
            FeedLog(
                id = uid(),
                flockId = layerFlockId,
                date = date,
                feedKg = 14.0 + (0.25 * index),
                feedType = "Layer mash",
                cost = 11200.0 + (120 * index),
                createdAt = nowIso(),
            )
        }
        val feedLogs = broilerFeedLogs + layerFeedLogs

        val mortalityDays = listOf(0, 3, 6, 9)
        val mortalityLogs = mortalityDays.map { offset ->
            val date = today.minusDays((9 - offset).toLong()).toString()
            MortalityLog(
                id = uid(),
                flockId = flockId,
                date = date,
                count = 1,
                cause = "Weakness",
                createdAt = nowIso(),
            )
        } + MortalityLog(
            id = uid(),
            flockId = layerFlockId,
            date = today.minusDays(5).toString(),
            count = 1,
            cause = "Injury",
            createdAt = nowIso(),
        )

        val envLogs = (0 until 10 step 2).map { index ->
            val date = today.minusDays((9 - index).toLong()).toString()
            EnvLog(
                id = uid(),
                flockId = flockId,
                date = date,
                temperatureC = 29.0 + ((index / 2) % 3),
                humidityPercent = 65.0 + (index / 2),
                createdAt = nowIso(),
            )
        }
        val eggLogs = (0 until 7).map { index ->
            val date = today.minusDays((6 - index).toLong()).toString()
            EggLog(
                id = uid(),
                flockId = layerFlockId,
                date = date,
                collected = 88 + index,
                cracked = if (index % 3 == 0) 2 else 1,
                createdAt = nowIso(),
            )
        }

        val treatmentLogs = listOf(
            TreatmentLog(
                id = uid(),
                flockId = flockId,
                date = today.minusDays(8).toString(),
                treatment = "Multi-vitamin",
                dosage = "10 ml",
                administeredBy = "Farm Vet",
                createdAt = nowIso(),
            ),
            TreatmentLog(
                id = uid(),
                flockId = flockId,
                date = today.minusDays(2).toString(),
                treatment = "Coccidiostat",
                dosage = "15 ml",
                administeredBy = "Farm Vet",
                createdAt = nowIso(),
            ),
        )

        val user = User(
            id = uid(),
            name = "Farm Manager",
            role = "manager",
            contact = "0800-000-0000",
        )

        return AppState(
            farmName = "Poultry Farm",
            users = listOf(user),
            flocks = listOf(flock, layerFlock),
            logs = Logs(
                feed = feedLogs,
                mortality = mortalityLogs,
                eggs = eggLogs,
                treatments = treatmentLogs,
                environment = envLogs,
            ),
            pendingQueue = emptyList(),
            selectedFlockId = flockId,
            lastSyncAt = null,
        )
    }
}
