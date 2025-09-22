package com.example.data_collect

import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
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
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.nio.charset.Charset
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.util.LinkedHashMap
import java.util.Locale
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                PoultryApp()
            }
        }
    }
}

@Composable
fun PoultryApp(viewModel: AppViewModel = viewModel()) {
    val appState by viewModel.appState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            viewModel.exportToUri(uri) { success ->
                Toast.makeText(
                    context,
                    if (success) "Export complete" else "Export failed",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.importFromUri(uri) { success ->
                Toast.makeText(
                    context,
                    if (success) "Import complete" else "Import failed",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        MainScreen(
            appState = appState,
            contentPadding = padding,
            onSelectFlock = { viewModel.setSelectedFlock(it) },
            onAddFeed = { flockId, date, feedKg, feedType, cost, notes ->
                viewModel.addFeedLog(flockId, date, feedKg, feedType, cost, notes)
            },
            onAddMortality = { flockId, date, count, cause, notes ->
                viewModel.addMortalityLog(flockId, date, count, cause, notes)
            },
            onAddEggs = { flockId, date, collected, cracked, notes ->
                viewModel.addEggLog(flockId, date, collected, cracked, notes)
            },
            onAddTreatment = { flockId, date, treatment, dosage, administeredBy, notes ->
                viewModel.addTreatmentLog(flockId, date, treatment, dosage, administeredBy, notes)
            },
            onAddEnvironment = { flockId, date, temperature, humidity, notes ->
                viewModel.addEnvLog(flockId, date, temperature, humidity, notes)
            },
            onSync = { viewModel.simulateSync() },
            onExport = { exportLauncher.launch("poultry_app_${today()}.json") },
            onImport = { importLauncher.launch(arrayOf("application/json")) }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    appState: AppState,
    contentPadding: PaddingValues,
    onSelectFlock: (String) -> Unit,
    onAddFeed: (String, String, Double, String, Double, String?) -> Unit,
    onAddMortality: (String, String, Int, String, String?) -> Unit,
    onAddEggs: (String, String, Int, Int, String?) -> Unit,
    onAddTreatment: (String, String, String, String, String, String?) -> Unit,
    onAddEnvironment: (String, String, Double, Double, String?) -> Unit,
    onSync: () -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    val tabs = listOf("Capture", "Dashboard", "Vet View")
    val selectedFlock = remember(appState.flocks, appState.selectedFlockId) {
        appState.flocks.firstOrNull { it.id == appState.selectedFlockId } ?: appState.flocks.firstOrNull()
    }
    val context = LocalContext.current

    LaunchedEffect(appState.selectedFlockId) {
        if (appState.selectedFlockId == null && selectedFlock != null) {
            onSelectFlock(selectedFlock.id)
        }
    }

    Column(
        modifier = Modifier
            .padding(contentPadding)
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        TopAppBar(
            title = { Text(text = "🐔 Poultry Data (Compose Demo)") },
            actions = {
                TextButton(onClick = {
                    onSync()
                    Toast.makeText(context, "Sync simulated", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Sync")
                }
                TextButton(onClick = onExport) {
                    Text("Export")
                }
                TextButton(onClick = onImport) {
                    Text("Import")
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedFlock == null) {
            Text(
                text = "No flocks found",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            return@Column
        }

        SectionCard(title = "Active Flock") {
            FlockSelector(
                flocks = appState.flocks,
                selectedFlock = selectedFlock,
                onSelect = { onSelectFlock(it.id) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, label ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(label) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTab) {
            0 -> CaptureTab(
                selectedFlock = selectedFlock,
                onAddFeed = onAddFeed,
                onAddMortality = onAddMortality,
                onAddEggs = onAddEggs,
                onAddTreatment = onAddTreatment,
                onAddEnvironment = onAddEnvironment
            )
            1 -> DashboardTab(appState = appState, selectedFlock = selectedFlock)
            else -> VetViewTab(appState = appState, selectedFlock = selectedFlock)
        }
    }
}

@Composable
fun CaptureTab(
    selectedFlock: Flock,
    onAddFeed: (String, String, Double, String, Double, String?) -> Unit,
    onAddMortality: (String, String, Int, String, String?) -> Unit,
    onAddEggs: (String, String, Int, Int, String?) -> Unit,
    onAddTreatment: (String, String, String, String, String, String?) -> Unit,
    onAddEnvironment: (String, String, Double, Double, String?) -> Unit
) {
    val context = LocalContext.current
    val isLayerFlock = selectedFlock.type.equals("layers", ignoreCase = true)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionCard(title = "Feed") {
            var feedDate by rememberSaveable(selectedFlock.id) { mutableStateOf(today()) }
            var feedKgText by rememberSaveable(selectedFlock.id) { mutableStateOf("") }
            var feedType by rememberSaveable(selectedFlock.id) { mutableStateOf("Starter") }
            var feedCostText by rememberSaveable(selectedFlock.id) { mutableStateOf("") }
            var feedNotes by rememberSaveable(selectedFlock.id) { mutableStateOf("") }

            OutlinedTextField(
                value = feedDate,
                onValueChange = { feedDate = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Date (YYYY-MM-DD)") },
                singleLine = true
            )
            OutlinedTextField(
                value = feedKgText,
                onValueChange = { feedKgText = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Feed quantity (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            OutlinedTextField(
                value = feedType,
                onValueChange = { feedType = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Feed type") },
                singleLine = true
            )
            OutlinedTextField(
                value = feedCostText,
                onValueChange = { feedCostText = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Feed cost (₦)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            OutlinedTextField(
                value = feedNotes,
                onValueChange = { feedNotes = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Notes (optional)") }
            )
            Button(
                onClick = {
                    val kg = feedKgText.toDoubleOrNull()
                    val cost = feedCostText.toDoubleOrNull()
                    if (kg != null && cost != null) {
                        onAddFeed(selectedFlock.id, feedDate, kg, feedType, cost, feedNotes.ifBlank { null })
                        Toast.makeText(context, "Feed log saved", Toast.LENGTH_SHORT).show()
                        feedKgText = ""
                        feedCostText = ""
                        feedNotes = ""
                    } else {
                        Toast.makeText(context, "Enter valid feed data", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Save Feed")
            }
        }

        SectionCard(title = "Mortality") {
            var mortalityDate by rememberSaveable(selectedFlock.id) { mutableStateOf(today()) }
            var mortalityCountText by rememberSaveable(selectedFlock.id) { mutableStateOf("") }
            var mortalityCause by rememberSaveable(selectedFlock.id) { mutableStateOf("Weakness") }
            var mortalityNotes by rememberSaveable(selectedFlock.id) { mutableStateOf("") }

            OutlinedTextField(
                value = mortalityDate,
                onValueChange = { mortalityDate = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Date (YYYY-MM-DD)") },
                singleLine = true
            )
            OutlinedTextField(
                value = mortalityCountText,
                onValueChange = { mortalityCountText = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Birds lost") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = mortalityCause,
                onValueChange = { mortalityCause = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Cause") }
            )
            OutlinedTextField(
                value = mortalityNotes,
                onValueChange = { mortalityNotes = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Notes (optional)") }
            )
            Button(
                onClick = {
                    val count = mortalityCountText.toIntOrNull()
                    if (count != null && count >= 0) {
                        onAddMortality(
                            selectedFlock.id,
                            mortalityDate,
                            count,
                            mortalityCause,
                            mortalityNotes.ifBlank { null }
                        )
                        Toast.makeText(context, "Mortality logged", Toast.LENGTH_SHORT).show()
                        mortalityCountText = ""
                        mortalityNotes = ""
                    } else {
                        Toast.makeText(context, "Enter a valid count", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Save Mortality")
            }
        }

        if (isLayerFlock) {
            SectionCard(title = "Eggs") {
                var eggDate by rememberSaveable(selectedFlock.id) { mutableStateOf(today()) }
                var eggsCollectedText by rememberSaveable(selectedFlock.id) { mutableStateOf("") }
                var eggsCrackedText by rememberSaveable(selectedFlock.id) { mutableStateOf("0") }
                var eggNotes by rememberSaveable(selectedFlock.id) { mutableStateOf("") }

                OutlinedTextField(
                    value = eggDate,
                    onValueChange = { eggDate = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Date (YYYY-MM-DD)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = eggsCollectedText,
                    onValueChange = { eggsCollectedText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Eggs collected") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = eggsCrackedText,
                    onValueChange = { eggsCrackedText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Cracked eggs") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = eggNotes,
                    onValueChange = { eggNotes = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Notes (optional)") }
                )
                Button(
                    onClick = {
                        val collected = eggsCollectedText.toIntOrNull()
                        val cracked = eggsCrackedText.toIntOrNull()
                        if (collected != null && cracked != null) {
                            onAddEggs(
                                selectedFlock.id,
                                eggDate,
                                collected,
                                cracked,
                                eggNotes.ifBlank { null }
                            )
                            Toast.makeText(context, "Egg record saved", Toast.LENGTH_SHORT).show()
                            eggsCollectedText = ""
                            eggsCrackedText = "0"
                            eggNotes = ""
                        } else {
                            Toast.makeText(context, "Enter valid egg counts", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text("Save Eggs")
                }
            }
        }

        SectionCard(title = "Treatment") {
            var treatmentDate by rememberSaveable(selectedFlock.id) { mutableStateOf(today()) }
            var treatmentName by rememberSaveable(selectedFlock.id) { mutableStateOf("Vitamin boost") }
            var treatmentDose by rememberSaveable(selectedFlock.id) { mutableStateOf("") }
            var administeredBy by rememberSaveable(selectedFlock.id) { mutableStateOf("Farm Vet") }
            var treatmentNotes by rememberSaveable(selectedFlock.id) { mutableStateOf("") }

            OutlinedTextField(
                value = treatmentDate,
                onValueChange = { treatmentDate = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Date (YYYY-MM-DD)") },
                singleLine = true
            )
            OutlinedTextField(
                value = treatmentName,
                onValueChange = { treatmentName = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Treatment") }
            )
            OutlinedTextField(
                value = treatmentDose,
                onValueChange = { treatmentDose = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Dosage") }
            )
            OutlinedTextField(
                value = administeredBy,
                onValueChange = { administeredBy = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Administered by") }
            )
            OutlinedTextField(
                value = treatmentNotes,
                onValueChange = { treatmentNotes = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Notes (optional)") }
            )
            Button(
                onClick = {
                    onAddTreatment(
                        selectedFlock.id,
                        treatmentDate,
                        treatmentName,
                        treatmentDose,
                        administeredBy,
                        treatmentNotes.ifBlank { null }
                    )
                    Toast.makeText(context, "Treatment recorded", Toast.LENGTH_SHORT).show()
                    treatmentNotes = ""
                    treatmentDose = ""
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Save Treatment")
            }
        }

        SectionCard(title = "Environment") {
            var envDate by rememberSaveable(selectedFlock.id) { mutableStateOf(today()) }
            var temperatureText by rememberSaveable(selectedFlock.id) { mutableStateOf("") }
            var humidityText by rememberSaveable(selectedFlock.id) { mutableStateOf("") }
            var envNotes by rememberSaveable(selectedFlock.id) { mutableStateOf("") }

            OutlinedTextField(
                value = envDate,
                onValueChange = { envDate = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Date (YYYY-MM-DD)") },
                singleLine = true
            )
            OutlinedTextField(
                value = temperatureText,
                onValueChange = { temperatureText = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Temperature (°C)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            OutlinedTextField(
                value = humidityText,
                onValueChange = { humidityText = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Humidity (%)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            OutlinedTextField(
                value = envNotes,
                onValueChange = { envNotes = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Notes (optional)") }
            )
            Button(
                onClick = {
                    val temperature = temperatureText.toDoubleOrNull()
                    val humidity = humidityText.toDoubleOrNull()
                    if (temperature != null && humidity != null) {
                        onAddEnvironment(
                            selectedFlock.id,
                            envDate,
                            temperature,
                            humidity,
                            envNotes.ifBlank { null }
                        )
                        Toast.makeText(context, "Environment logged", Toast.LENGTH_SHORT).show()
                        temperatureText = ""
                        humidityText = ""
                        envNotes = ""
                    } else {
                        Toast.makeText(context, "Enter valid environment data", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Save Environment")
            }
        }
    }
}

@Composable
fun DashboardTab(appState: AppState, selectedFlock: Flock) {
    val feedLogs = appState.logs.feed.filter { it.flockId == selectedFlock.id }
    val mortalityLogs = appState.logs.mortality.filter { it.flockId == selectedFlock.id }
    val eggLogs = appState.logs.eggs.filter { it.flockId == selectedFlock.id }
    val isLayerFlock = selectedFlock.type.equals("layers", ignoreCase = true)

    val todayDate = LocalDate.now()
    val sevenDaysAgo = todayDate.minusDays(6)
    val feedLast7 = feedLogs.filter { parseDate(it.date)?.let { d -> !d.isBefore(sevenDaysAgo) } == true }
    val eggsLast7 = eggLogs.filter { parseDate(it.date)?.let { d -> !d.isBefore(sevenDaysAgo) } == true }

    val feedTotal7 = feedLast7.sumOf { it.feedKg }
    val eggsTotal7 = eggsLast7.sumOf { it.collected }
    val liveBirds = (selectedFlock.initialCount - mortalityLogs.sumOf { it.count }).coerceAtLeast(0)

    val numberFormat = NumberFormat.getNumberInstance(Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StatCard(
            title = "Live birds",
            value = numberFormat.format(liveBirds)
        )
        StatCard(
            title = "Feed last 7d",
            value = String.format(Locale.getDefault(), "%.1f kg", feedTotal7)
        )
        StatCard(
            title = "Eggs last 7d",
            value = if (isLayerFlock) numberFormat.format(eggsTotal7) else "0"
        )

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

        Table(
            headers = listOf("Date", "Treatment", "Dosage", "By"),
            rows = appState.logs.treatments
                .filter { it.flockId == selectedFlock.id }
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

        Table(
            headers = listOf("Date", "Temp °C", "Humidity %", "Notes"),
            rows = appState.logs.environment
                .filter { it.flockId == selectedFlock.id }
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

@Composable
fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            content()
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.labelLarge)
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun Table(
    headers: List<String>,
    rows: List<List<String>>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                headers.forEach { header ->
                    Text(
                        text = header,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            if (rows.isEmpty()) {
                Text(
                    text = "No data yet",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                rows.forEachIndexed { index, row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        row.forEach { cell ->
                            Text(
                                text = cell,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    if (index != rows.lastIndex) {
                        Divider()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlockSelector(
    flocks: List<Flock>,
    selectedFlock: Flock,
    onSelect: (Flock) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedFlock.name,
            onValueChange = {},
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            readOnly = true,
            label = { Text("Select flock") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            flocks.forEach { flock ->
                DropdownMenuItem(
                    text = { Text("${flock.name} • ${flock.type}") },
                    onClick = {
                        expanded = false
                        onSelect(flock)
                    }
                )
            }
        }
    }
}

@Serializable
data class User(
    val id: String,
    val name: String,
    val role: String,
    val contact: String? = null
)

@Serializable
data class Flock(
    val id: String,
    val name: String,
    val type: String,
    val startDate: String,
    val initialCount: Int,
    val notes: String? = null
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
    val updatedAt: String? = null
)

@Serializable
data class MortalityLog(
    val id: String,
    val flockId: String,
    val date: String,
    val count: Int,
    val cause: String,
    val notes: String? = null,
    val createdAt: String
)

@Serializable
data class EggLog(
    val id: String,
    val flockId: String,
    val date: String,
    val collected: Int,
    val cracked: Int,
    val notes: String? = null,
    val createdAt: String
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
    val createdAt: String
)

@Serializable
data class EnvLog(
    val id: String,
    val flockId: String,
    val date: String,
    val temperatureC: Double,
    val humidityPercent: Double,
    val notes: String? = null,
    val createdAt: String
)

@Serializable
data class PendingItem(
    val id: String,
    val kind: String,
    val payloadJson: String,
    val createdAt: String
)

@Serializable
data class Logs(
    val feed: List<FeedLog> = emptyList(),
    val mortality: List<MortalityLog> = emptyList(),
    val eggs: List<EggLog> = emptyList(),
    val treatments: List<TreatmentLog> = emptyList(),
    val environment: List<EnvLog> = emptyList()
)

@Serializable
data class AppState(
    val farmName: String = "Poultry Demo Farm",
    val users: List<User> = emptyList(),
    val flocks: List<Flock> = emptyList(),
    val logs: Logs = Logs(),
    val pendingQueue: List<PendingItem> = emptyList(),
    val selectedFlockId: String? = null,
    val lastSyncAt: String? = null
)

private val Context.poultryDataStore by preferencesDataStore(name = "poultry_demo_store")

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
        notes: String?
    ) {
        val feedLog = FeedLog(
            id = uid(),
            flockId = flockId,
            date = date,
            feedKg = feedKg,
            feedType = feedType,
            cost = cost,
            notes = notes,
            createdAt = nowIso()
        )
        val pending = PendingItem(
            id = uid(),
            kind = "feed",
            payloadJson = json.encodeToString(feedLog),
            createdAt = nowIso()
        )
        update { state ->
            state.copy(
                logs = state.logs.copy(feed = listOf(feedLog) + state.logs.feed),
                pendingQueue = listOf(pending) + state.pendingQueue
            )
        }
    }

    suspend fun addMortality(
        flockId: String,
        date: String,
        count: Int,
        cause: String,
        notes: String?
    ) {
        val mortalityLog = MortalityLog(
            id = uid(),
            flockId = flockId,
            date = date,
            count = count,
            cause = cause,
            notes = notes,
            createdAt = nowIso()
        )
        val pending = PendingItem(
            id = uid(),
            kind = "mortality",
            payloadJson = json.encodeToString(mortalityLog),
            createdAt = nowIso()
        )
        update { state ->
            state.copy(
                logs = state.logs.copy(mortality = listOf(mortalityLog) + state.logs.mortality),
                pendingQueue = listOf(pending) + state.pendingQueue
            )
        }
    }

    suspend fun addEggs(
        flockId: String,
        date: String,
        collected: Int,
        cracked: Int,
        notes: String?
    ) {
        val eggLog = EggLog(
            id = uid(),
            flockId = flockId,
            date = date,
            collected = collected,
            cracked = cracked,
            notes = notes,
            createdAt = nowIso()
        )
        val pending = PendingItem(
            id = uid(),
            kind = "eggs",
            payloadJson = json.encodeToString(eggLog),
            createdAt = nowIso()
        )
        update { state ->
            state.copy(
                logs = state.logs.copy(eggs = listOf(eggLog) + state.logs.eggs),
                pendingQueue = listOf(pending) + state.pendingQueue
            )
        }
    }

    suspend fun addTreatment(
        flockId: String,
        date: String,
        treatment: String,
        dosage: String,
        administeredBy: String,
        notes: String?
    ) {
        val treatmentLog = TreatmentLog(
            id = uid(),
            flockId = flockId,
            date = date,
            treatment = treatment,
            dosage = dosage,
            administeredBy = administeredBy,
            notes = notes,
            createdAt = nowIso()
        )
        val pending = PendingItem(
            id = uid(),
            kind = "treatment",
            payloadJson = json.encodeToString(treatmentLog),
            createdAt = nowIso()
        )
        update { state ->
            state.copy(
                logs = state.logs.copy(treatments = listOf(treatmentLog) + state.logs.treatments),
                pendingQueue = listOf(pending) + state.pendingQueue
            )
        }
    }

    suspend fun addEnv(
        flockId: String,
        date: String,
        temperature: Double,
        humidity: Double,
        notes: String?
    ) {
        val envLog = EnvLog(
            id = uid(),
            flockId = flockId,
            date = date,
            temperatureC = temperature,
            humidityPercent = humidity,
            notes = notes,
            createdAt = nowIso()
        )
        val pending = PendingItem(
            id = uid(),
            kind = "environment",
            payloadJson = json.encodeToString(envLog),
            createdAt = nowIso()
        )
        update { state ->
            state.copy(
                logs = state.logs.copy(environment = listOf(envLog) + state.logs.environment),
                pendingQueue = listOf(pending) + state.pendingQueue
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
                environment = mergeLists(current.logs.environment, incoming.logs.environment) { it.id }
            ),
            pendingQueue = mergeLists(current.pendingQueue, incoming.pendingQueue) { it.id },
            lastSyncAt = current.lastSyncAt ?: incoming.lastSyncAt,
            selectedFlockId = current.selectedFlockId ?: incoming.selectedFlockId
        )
    }
}

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AppRepository(application.applicationContext)
    val appState: StateFlow<AppState> = repository.appState

    fun setSelectedFlock(flockId: String) {
        viewModelScope.launch { repository.setSelectedFlock(flockId) }
    }

    fun addFeedLog(
        flockId: String,
        date: String,
        feedKg: Double,
        feedType: String,
        cost: Double,
        notes: String?
    ) {
        viewModelScope.launch { repository.addFeed(flockId, date, feedKg, feedType, cost, notes) }
    }

    fun addMortalityLog(
        flockId: String,
        date: String,
        count: Int,
        cause: String,
        notes: String?
    ) {
        viewModelScope.launch { repository.addMortality(flockId, date, count, cause, notes) }
    }

    fun addEggLog(
        flockId: String,
        date: String,
        collected: Int,
        cracked: Int,
        notes: String?
    ) {
        viewModelScope.launch { repository.addEggs(flockId, date, collected, cracked, notes) }
    }

    fun addTreatmentLog(
        flockId: String,
        date: String,
        treatment: String,
        dosage: String,
        administeredBy: String,
        notes: String?
    ) {
        viewModelScope.launch {
            repository.addTreatment(flockId, date, treatment, dosage, administeredBy, notes)
        }
    }

    fun addEnvLog(
        flockId: String,
        date: String,
        temperature: Double,
        humidity: Double,
        notes: String?
    ) {
        viewModelScope.launch { repository.addEnv(flockId, date, temperature, humidity, notes) }
    }

    fun simulateSync() {
        viewModelScope.launch { repository.simulateSync() }
    }

    fun exportToUri(uri: Uri, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.exportToUri(uri)
            onResult(success)
        }
    }

    fun importFromUri(uri: Uri, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.importFromUri(uri)
            onResult(success)
        }
    }
}

object Seeds {
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
            notes = "Starter batch"
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
                createdAt = nowIso()
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
                createdAt = nowIso()
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
                createdAt = nowIso()
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
                createdAt = nowIso()
            ),
            TreatmentLog(
                id = uid(),
                flockId = flockId,
                date = today.minusDays(2).toString(),
                treatment = "Coccidiostat",
                dosage = "15 ml",
                administeredBy = "Farm Vet",
                createdAt = nowIso()
            )
        )

        val user = User(id = uid(), name = "Demo Manager", role = "manager", contact = "0800-000-0000")

        return AppState(
            farmName = "Poultry Demo Farm",
            users = listOf(user),
            flocks = listOf(flock),
            logs = Logs(
                feed = feedLogs,
                mortality = mortalityLogs,
                eggs = emptyList(),
                treatments = treatmentLogs,
                environment = envLogs
            ),
            pendingQueue = emptyList(),
            selectedFlockId = flockId,
            lastSyncAt = null
        )
    }
}

fun uid(): String = UUID.randomUUID().toString().replace("-", "").take(8)

fun today(): String = LocalDate.now().toString()

fun nowIso(): String = Instant.now().toString()

fun parseDate(date: String): LocalDate? = runCatching { LocalDate.parse(date) }.getOrNull()

