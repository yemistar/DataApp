 package com.example.data_collect

 import android.os.Build
 import androidx.annotation.RequiresApi
 import androidx.compose.runtime.Composable
 import androidx.compose.ui.tooling.preview.Preview
 import com.example.data_collect.ui.FarmTheme

 @RequiresApi(Build.VERSION_CODES.O)
 @Preview(showBackground = true)
 @Composable
 fun CapturePreview() {
     FarmTheme {
         val state = Seeds.defaultAppState()
         MainScreen(
             appState = state,
             contentPadding = androidx.compose.foundation.layout.PaddingValues(),
             onSelectFlock = {},
             onAddFeed = { _, _, _, _, _, _ -> },
             onAddMortality = { _, _, _, _, _ -> },
             onAddEggs = { _, _, _, _, _ -> },
             onAddTreatment = { _, _, _, _, _, _ -> },
             onAddEnvironment = { _, _, _, _, _ -> },
             onSync = {},
             onExport = {},
             onImport = {}
         )
     }
 }

 @RequiresApi(Build.VERSION_CODES.O)
 @Preview(showBackground = true)
 @Composable
 fun DashboardPreview() {
     FarmTheme {
         DashboardTab(appState = Seeds.defaultAppState(), selectedFlock = Seeds.defaultAppState().flocks.first())
     }
 }

 @RequiresApi(Build.VERSION_CODES.O)
 @Preview(showBackground = true)
 @Composable
 fun VetViewPreview() {
     FarmTheme {
         val state = Seeds.defaultAppState()
         VetViewTab(appState = state, selectedFlock = state.flocks.first())
     }
 }
