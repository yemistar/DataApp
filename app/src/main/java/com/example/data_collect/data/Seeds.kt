package com.example.data_collect.data

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.data_collect.data.model.AppState
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
    @RequiresApi(Build.VERSION_CODES.O)
    fun defaultAppState(): AppState {
        val flockId = uid()
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

        val feedLogs = (0 until 10).map { index ->
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
        }

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
            name = "Demo Manager",
            role = "manager",
            contact = "0800-000-0000",
        )

        return AppState(
            farmName = "Poultry Demo Farm",
            users = listOf(user),
            flocks = listOf(flock),
            logs = Logs(
                feed = feedLogs,
                mortality = mortalityLogs,
                eggs = emptyList(),
                treatments = treatmentLogs,
                environment = envLogs,
            ),
            pendingQueue = emptyList(),
            selectedFlockId = flockId,
            lastSyncAt = null,
        )
    }
}
