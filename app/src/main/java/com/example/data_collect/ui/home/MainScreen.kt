package com.example.data_collect.ui.home

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data_collect.data.model.AppState
import com.example.data_collect.ui.components.FlockSelector
import com.example.data_collect.ui.components.SectionCard

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
