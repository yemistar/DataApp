package com.example.data_collect.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.data_collect.data.model.Flock
import com.example.data_collect.ui.home.capture.CaptureHub
import com.example.data_collect.ui.home.capture.CaptureMode
import com.example.data_collect.ui.home.capture.EggLogForm
import com.example.data_collect.ui.home.capture.EnvironmentLogForm
import com.example.data_collect.ui.home.capture.FeedLogForm
import com.example.data_collect.ui.home.capture.MortalityLogForm
import com.example.data_collect.ui.home.capture.TreatmentLogForm
import com.example.data_collect.ui.home.capture.captureModesForFlock

@Composable
fun CaptureTab(
    selectedFlock: Flock,
    onAddFeed: (String, String, Double, String, Double, String?) -> Unit,
    onAddMortality: (String, String, Int, String, String?) -> Unit,
    onAddEggs: (String, String, Int, Int, String?) -> Unit,
    onAddTreatment: (String, String, String, String, String, String?) -> Unit,
    onAddEnvironment: (String, String, Double, Double, String?) -> Unit,
) {
    val context = LocalContext.current
    val modes = captureModesForFlock(selectedFlock.type)
    var selectedModeName by rememberSaveable(selectedFlock.id) {
        mutableStateOf(CaptureMode.Feed.name)
    }
    val selectedMode = modes.firstOrNull { it.name == selectedModeName } ?: CaptureMode.Feed

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CaptureHub(
            modes = modes,
            selectedMode = selectedMode,
            selectedFlock = selectedFlock,
            onSelectMode = { selectedModeName = it.name }
        )

        when (selectedMode) {
            CaptureMode.Feed -> FeedLogForm(
                selectedFlock = selectedFlock,
                onAddFeed = onAddFeed,
                context = context
            )
            CaptureMode.Mortality -> MortalityLogForm(
                selectedFlock = selectedFlock,
                onAddMortality = onAddMortality,
                context = context
            )
            CaptureMode.Eggs -> EggLogForm(
                selectedFlock = selectedFlock,
                onAddEggs = onAddEggs,
                context = context
            )
            CaptureMode.Treatment -> TreatmentLogForm(
                selectedFlock = selectedFlock,
                onAddTreatment = onAddTreatment,
                context = context
            )
            CaptureMode.Environment -> EnvironmentLogForm(
                selectedFlock = selectedFlock,
                onAddEnvironment = onAddEnvironment,
                context = context
            )
        }
    }
}
