package com.example.data_collect.ui.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data_collect.data.model.AppState
import com.example.data_collect.data.model.Flock
import com.example.data_collect.ui.components.SectionCard
import com.example.data_collect.ui.components.Table
import com.example.data_collect.util.parseDate
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun VetViewTab(appState: AppState, selectedFlock: Flock) {
    val cutoff = LocalDate.now().minusDays(13)
    val flockMap = appState.flocks.associateBy { it.id }
    val numberFormat = NumberFormat.getNumberInstance(Locale.getDefault())

    val totalBirds = appState.flocks.sumOf { it.initialCount }
    val totalMortality = appState.logs.mortality.sumOf { it.count }
    val liveBirds = (totalBirds - totalMortality).coerceAtLeast(0)

    val mortalityRows = appState.logs.mortality
        .filter { parseDate(it.date)?.let { d -> !d.isBefore(cutoff) } == true }
        .sortedByDescending { it.date }
        .map {
            listOf(
                it.date,
                flockMap[it.flockId]?.name ?: "-",
                it.count.toString(),
                it.cause
            )
        }

    val treatmentRows = appState.logs.treatments
        .filter { parseDate(it.date)?.let { d -> !d.isBefore(cutoff) } == true }
        .sortedByDescending { it.date }
        .map {
            listOf(
                it.date,
                flockMap[it.flockId]?.name ?: "-",
                it.treatment,
                it.dosage
            )
        }

    val environmentRows = appState.logs.environment
        .filter { parseDate(it.date)?.let { d -> !d.isBefore(cutoff) } == true }
        .sortedByDescending { it.date }
        .map {
            listOf(
                it.date,
                flockMap[it.flockId]?.name ?: "-",
                String.format(Locale.getDefault(), "%.1f", it.temperatureC),
                String.format(Locale.getDefault(), "%.1f", it.humidityPercent)
            )
        }

    val feedByDate = appState.logs.feed
        .filter { parseDate(it.date)?.let { d -> !d.isBefore(cutoff) } == true }
        .groupBy { it.date }
        .mapValues { entry -> entry.value.sumOf { it.feedKg } }

    val eggsByDate = appState.logs.eggs
        .filter { parseDate(it.date)?.let { d -> !d.isBefore(cutoff) } == true }
        .groupBy { it.date }
        .mapValues { entry ->
            entry.value.sumOf { it.collected } to entry.value.sumOf { it.cracked }
        }

    val productionRows = (feedByDate.keys + eggsByDate.keys)
        .mapNotNull { parseDate(it) }
        .filter { !it.isBefore(cutoff) }
        .distinct()
        .sortedDescending()
        .map { date ->
            val feedKg = feedByDate[date.toString()] ?: 0.0
            val eggsPair = eggsByDate[date.toString()] ?: (0 to 0)
            listOf(
                date.toString(),
                String.format(Locale.getDefault(), "%.1f", feedKg),
                numberFormat.format(eggsPair.first),
                numberFormat.format(eggsPair.second)
            )
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionCard(title = "Farm summary") {
            Text(text = appState.farmName, style = MaterialTheme.typography.titleMedium)
            Text(text = "Flocks: ${appState.flocks.size}")
            Text(text = "Total birds: ${numberFormat.format(totalBirds)}")
            Text(text = "Live birds: ${numberFormat.format(liveBirds)}")
            Text(text = "Pending items: ${appState.pendingQueue.size}")
            Text(text = "Last sync: ${appState.lastSyncAt ?: "Never"}")
            Text(text = "Focus flock: ${selectedFlock.name} (${selectedFlock.type})")
        }

        Table(
            headers = listOf("Date", "Flock", "Count", "Cause"),
            rows = mortalityRows
        )

        Table(
            headers = listOf("Date", "Flock", "Treatment", "Dosage"),
            rows = treatmentRows
        )

        Table(
            headers = listOf("Date", "Flock", "Temp °C", "Humidity %"),
            rows = environmentRows
        )

        Table(
            headers = listOf("Date", "Feed kg", "Eggs", "Cracked"),
            rows = productionRows
        )
    }
}
