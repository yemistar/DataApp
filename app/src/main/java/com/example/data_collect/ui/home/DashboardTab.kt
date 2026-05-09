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
import com.example.data_collect.ui.components.TrendCard
import com.example.data_collect.util.parseDate
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale

@Composable
fun DashboardTab(appState: AppState, selectedFlock: Flock) {
    val feedLogs = appState.logs.feed.filter { it.flockId == selectedFlock.id }
    val mortalityLogs = appState.logs.mortality.filter { it.flockId == selectedFlock.id }
    val eggLogs = appState.logs.eggs.filter { it.flockId == selectedFlock.id }
    val treatmentLogs = appState.logs.treatments.filter { it.flockId == selectedFlock.id }
    val environmentLogs = appState.logs.environment.filter { it.flockId == selectedFlock.id }
    val isLayerFlock = selectedFlock.type.equals("layers", ignoreCase = true)

    val todayDate = LocalDate.now()
    val lastSevenDates = (0L..6L).map { offset -> todayDate.minusDays(6L - offset) }
    val sevenDaysAgo = todayDate.minusDays(6)
    val feedLast7 = feedLogs.filter { parseDate(it.date)?.let { d -> !d.isBefore(sevenDaysAgo) } == true }
    val eggsLast7 = eggLogs.filter { parseDate(it.date)?.let { d -> !d.isBefore(sevenDaysAgo) } == true }

    val feedTotal7 = feedLast7.sumOf { it.feedKg }
    val eggsTotal7 = eggsLast7.sumOf { it.collected }
    val liveBirds = (selectedFlock.initialCount - mortalityLogs.sumOf { it.count }).coerceAtLeast(0)
    val feedTrend = lastSevenDates.map { date ->
        feedLogs.filter { it.date == date.toString() }.sumOf { it.feedKg }
    }
    val eggTrend = lastSevenDates.map { date ->
        eggLogs.filter { it.date == date.toString() }.sumOf { it.collected }.toDouble()
    }

    val numberFormat = NumberFormat.getNumberInstance(Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FieldChip(label = selectedFlock.name)
            FieldChip(label = selectedFlock.type.replaceFirstChar { it.titlecase() })
            FieldChip(label = "7 days")
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard(
                title = "Live birds",
                value = numberFormat.format(liveBirds),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Feed last 7d",
                value = String.format(Locale.getDefault(), "%.1f kg", feedTotal7),
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard(
                title = "Eggs last 7d",
                value = if (isLayerFlock) numberFormat.format(eggsTotal7) else "0",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Open queue",
                value = numberFormat.format(appState.pendingQueue.size),
                modifier = Modifier.weight(1f)
            )
        }

        TrendCard(
            title = "Feed trend",
            label = "Daily totals",
            values = feedTrend,
            valueFormatter = {
                String.format(Locale.getDefault(), "%.1f kg", it)
            }
        )
        if (isLayerFlock) {
            TrendCard(
                title = "Egg trend",
                label = "Daily totals",
                values = eggTrend,
                valueFormatter = { numberFormat.format(it.toInt()) + " eggs" }
            )
        }

        FieldSectionHeader(title = "Recent feed", detail = "Latest records")
        Table(
            headers = listOf("Date", "Feed", "Kg", "Cost"),
            rows = feedLogs
                .sortedByDescending { it.date }
                .take(20)
                .map {
                    listOf(
                        it.date,
                        it.feedType,
                        String.format(Locale.getDefault(), "%.1f", it.feedKg),
                        "₦" + numberFormat.format(it.cost)
                    )
                }
        )

        FieldSectionHeader(title = "Mortality", detail = "Loss records")
        Table(
            headers = listOf("Date", "Count", "Cause", "Notes"),
            rows = mortalityLogs
                .sortedByDescending { it.date }
                .take(20)
                .map {
                    listOf(
                        it.date,
                        it.count.toString(),
                        it.cause,
                        it.notes ?: "-"
                    )
                }
        )

        if (isLayerFlock) {
            FieldSectionHeader(title = "Egg production", detail = "Layer records")
            Table(
                headers = listOf("Date", "Collected", "Cracked", "Notes"),
                rows = eggLogs
                    .sortedByDescending { it.date }
                    .take(20)
                    .map {
                        listOf(
                            it.date,
                            it.collected.toString(),
                            it.cracked.toString(),
                            it.notes ?: "-"
                        )
                    }
            )
        }

        FieldSectionHeader(title = "Treatments", detail = "Health actions")
        Table(
            headers = listOf("Date", "Treatment", "Dosage", "By"),
            rows = treatmentLogs
                .sortedByDescending { it.date }
                .take(20)
                .map {
                    listOf(
                        it.date,
                        it.treatment,
                        it.dosage,
                        it.administeredBy
                    )
                }
        )

        FieldSectionHeader(title = "Environment", detail = "House readings")
        Table(
            headers = listOf("Date", "Temp °C", "Humidity %", "Notes"),
            rows = environmentLogs
                .sortedByDescending { it.date }
                .take(20)
                .map {
                    listOf(
                        it.date,
                        String.format(Locale.getDefault(), "%.1f", it.temperatureC),
                        String.format(Locale.getDefault(), "%.1f", it.humidityPercent),
                        it.notes ?: "-"
                    )
                }
        )
    }
}
