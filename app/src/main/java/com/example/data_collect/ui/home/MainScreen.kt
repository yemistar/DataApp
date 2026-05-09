package com.example.data_collect.ui.home

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data_collect.data.model.AppState
import com.example.data_collect.data.model.Flock
import com.example.data_collect.util.today
import java.text.NumberFormat
import java.util.Locale

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
    onImport: () -> Unit,
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
        PoultryTopBar(
            pendingCount = appState.pendingQueue.size,
            lastSyncAt = appState.lastSyncAt,
            onSync = {
                onSync()
                Toast.makeText(context, "Sync simulated", Toast.LENGTH_SHORT).show()
            },
            onExport = onExport,
            onImport = onImport
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

        FieldOpsHeader(
            appState = appState,
            selectedFlock = selectedFlock,
            onSelectFlock = onSelectFlock,
            onOpenCapture = { selectedTab = 0 },
            onOpenDashboard = { selectedTab = 1 },
            onOpenVetView = { selectedTab = 2 }
        )

        Spacer(modifier = Modifier.height(16.dp))

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
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
private fun FieldOpsHeader(
    appState: AppState,
    selectedFlock: Flock,
    onSelectFlock: (String) -> Unit,
    onOpenCapture: () -> Unit,
    onOpenDashboard: () -> Unit,
    onOpenVetView: () -> Unit,
) {
    val numberFormat = NumberFormat.getNumberInstance(Locale.getDefault())
    val feedLogs = appState.logs.feed.filter { it.flockId == selectedFlock.id }
    val mortalityLogs = appState.logs.mortality.filter { it.flockId == selectedFlock.id }
    val liveBirds = (selectedFlock.initialCount - mortalityLogs.sumOf { it.count }).coerceAtLeast(0)
    val feedToday = feedLogs.filter { it.date == today() }.sumOf { it.feedKg }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = selectedFlock.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${selectedFlock.type.replaceFirstChar { it.titlecase() }} • ${selectedFlock.startDate}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusChip(label = "Offline ready")
            }

            CompactFlockSelector(
                flocks = appState.flocks,
                selectedFlock = selectedFlock,
                onSelect = { onSelectFlock(it.id) }
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusChip(label = "Pending ${appState.pendingQueue.size}")
                StatusChip(label = appState.lastSyncAt?.let { "Synced" } ?: "Not synced")
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricTile(
                    label = "Live birds",
                    value = numberFormat.format(liveBirds),
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    label = "Feed today",
                    value = String.format(Locale.getDefault(), "%.1f kg", feedToday),
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricTile(
                    label = "Mortality",
                    value = numberFormat.format(mortalityLogs.sumOf { it.count }),
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    label = "Pending sync",
                    value = numberFormat.format(appState.pendingQueue.size),
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                text = "Quick actions",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onOpenCapture,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Log")
                }
                Button(
                    onClick = onOpenDashboard,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Review")
                }
                Button(
                    onClick = onOpenVetView,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Vet")
                }
            }
        }
    }
}

@Composable
private fun StatusChip(label: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun CompactFlockSelector(
    flocks: List<Flock>,
    selectedFlock: Flock,
    onSelect: (Flock) -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Active flock",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${selectedFlock.name} • ${selectedFlock.type}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Button(
                onClick = { expanded = true },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Change")
            }
            DropdownMenu(
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
}

@Composable
private fun MetricTile(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}
