package com.example.data_collect.ui

import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data_collect.ui.home.MainScreen
import com.example.data_collect.util.today
import com.example.data_collect.viewmodel.AppViewModel

@RequiresApi(Build.VERSION_CODES.O)
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
