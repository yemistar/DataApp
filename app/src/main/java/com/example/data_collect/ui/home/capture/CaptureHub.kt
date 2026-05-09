package com.example.data_collect.ui.home.capture

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data_collect.data.model.Flock
import com.example.data_collect.ui.components.FieldChip
import com.example.data_collect.ui.components.ModeChip
import com.example.data_collect.ui.components.SectionCard

@Composable
internal fun CaptureHub(
    modes: List<CaptureMode>,
    selectedMode: CaptureMode,
    selectedFlock: Flock,
    onSelectMode: (CaptureMode) -> Unit,
) {
    SectionCard(title = "Capture hub") {
        modes.chunked(2).forEach { rowModes ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowModes.forEach { mode ->
                    ModeChip(
                        label = mode.label,
                        selected = mode == selectedMode,
                        onClick = { onSelectMode(mode) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowModes.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FieldChip(label = "Today")
            FieldChip(label = selectedFlock.type.replaceFirstChar { it.titlecase() })
            FieldChip(label = "Offline")
            FieldChip(label = "Validated")
        }
    }
}
