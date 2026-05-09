package com.example.data_collect.ui.home.capture

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data_collect.data.model.Flock
import com.example.data_collect.ui.components.FieldChip
import com.example.data_collect.ui.components.FieldSectionHeader
import com.example.data_collect.ui.components.SectionCard
import com.example.data_collect.util.parseDate
import com.example.data_collect.util.today

@Composable
internal fun EggLogForm(
    selectedFlock: Flock,
    onAddEggs: (String, String, Int, Int, String?) -> Unit,
    context: Context,
) {
    SectionCard(title = "Egg log") {
        var eggDate by rememberSaveable(selectedFlock.id) { mutableStateOf(today()) }
        var eggsCollectedText by rememberSaveable(selectedFlock.id) { mutableStateOf("") }
        var eggsCrackedText by rememberSaveable(selectedFlock.id) { mutableStateOf("0") }
        var eggNotes by rememberSaveable(selectedFlock.id) { mutableStateOf("") }

        FieldSectionHeader(title = "Layer production", detail = "Collected and cracked counts")
        OutlinedTextField(
            value = eggDate,
            onValueChange = { eggDate = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Date (YYYY-MM-DD)") },
            singleLine = true
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = eggsCollectedText,
                onValueChange = { eggsCollectedText = it },
                modifier = Modifier.weight(1f),
                label = { Text("Collected") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            OutlinedTextField(
                value = eggsCrackedText,
                onValueChange = { eggsCrackedText = it },
                modifier = Modifier.weight(1f),
                label = { Text("Cracked") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
        }
        val collectedPreview = eggsCollectedText.toIntOrNull()
        val crackedPreview = eggsCrackedText.toIntOrNull()
        val productionRate = if (collectedPreview != null && selectedFlock.initialCount > 0) {
            "${((collectedPreview.toDouble() / selectedFlock.initialCount) * 100).toInt()}% lay rate"
        } else {
            "Lay rate pending"
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FieldChip(label = productionRate)
            FieldChip(label = "${crackedPreview ?: 0} cracked")
        }
        OutlinedTextField(
            value = eggNotes,
            onValueChange = { eggNotes = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Notes (optional)") }
        )
        SaveButton(label = "Save egg log") {
            val collected = eggsCollectedText.toIntOrNull()
            val cracked = eggsCrackedText.toIntOrNull()
            if (parseDate(eggDate) == null) {
                Toast.makeText(context, "Use date format YYYY-MM-DD", Toast.LENGTH_SHORT).show()
            } else if (collected == null || collected < 0) {
                Toast.makeText(context, "Enter eggs collected 0 or above", Toast.LENGTH_SHORT).show()
            } else if (cracked == null || cracked < 0) {
                Toast.makeText(context, "Enter cracked eggs 0 or above", Toast.LENGTH_SHORT).show()
            } else if (cracked > collected) {
                Toast.makeText(context, "Cracked eggs cannot exceed collected eggs", Toast.LENGTH_SHORT).show()
            } else {
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
            }
        }
    }
}
