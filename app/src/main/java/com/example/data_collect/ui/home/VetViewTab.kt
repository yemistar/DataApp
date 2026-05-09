package com.example.data_collect.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data_collect.data.model.AppState
import com.example.data_collect.data.model.Flock
import com.example.data_collect.ui.components.FieldChip
import com.example.data_collect.ui.components.FieldSectionHeader
import com.example.data_collect.ui.components.StatCard
import com.example.data_collect.ui.components.Table
import com.example.data_collect.ui.components.TimelineCard
import com.example.data_collect.ui.components.TrendCard
import com.example.data_collect.util.parseDate
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale

@Composable
fun VetViewTab(appState: AppState, selectedFlock: Flock) {
    val cutoff = LocalDate.now().minusDays(13)
    val todayDate = LocalDate.now()
    val trendDates = (0L..6L).map { offset -> todayDate.minusDays(6L - offset) }
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

    val mortalityTrend = trendDates.map { date ->
        appState.logs.mortality.filter { it.date == date.toString() }.sumOf { it.count }.toDouble()
    }
    val treatmentTimeline = appState.logs.treatments
        .sortedByDescending { it.date }
        .take(5)
        .map {
            it.date to "${flockMap[it.flockId]?.name ?: "-"} • ${it.treatment} • ${it.dosage}"
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
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FieldChip(label = "Vet handoff")
            FieldChip(label = "14 days")
            FieldChip(label = selectedFlock.name)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard(
                title = "Flocks",
                value = numberFormat.format(appState.flocks.size),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Live birds",
                value = numberFormat.format(liveBirds),
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard(
                title = "Mortality",
                value = numberFormat.format(totalMortality),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Open queue",
                value = numberFormat.format(appState.pendingQueue.size),
                modifier = Modifier.weight(1f)
            )
        }

        TrendCard(
            title = "Mortality trend",
            label = "Daily count",
            values = mortalityTrend,
            valueFormatter = { numberFormat.format(it.toInt()) + " birds" }
        )

        TimelineCard(
            title = "Treatment timeline",
            rows = treatmentTimeline
        )

        FieldSectionHeader(title = "Mortality detail", detail = "Recent losses")
        Table(
            headers = listOf("Date", "Flock", "Count", "Cause"),
            rows = mortalityRows
        )

        FieldSectionHeader(title = "Treatments", detail = "Medication and care")
        Table(
            headers = listOf("Date", "Flock", "Treatment", "Dosage"),
            rows = treatmentRows
        )

        FieldSectionHeader(title = "Environment", detail = "House readings")
        Table(
            headers = listOf("Date", "Flock", "Temp °C", "Humidity %"),
            rows = environmentRows
        )

        FieldSectionHeader(title = "Production", detail = "Feed and egg totals")
        Table(
            headers = listOf("Date", "Feed kg", "Eggs", "Cracked"),
            rows = productionRows
        )
    }
}
