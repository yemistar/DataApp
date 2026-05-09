package com.example.data_collect.ui.home.capture

import android.content.Context
import android.widget.Toast
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
import com.example.data_collect.data.model.Flock
import com.example.data_collect.ui.components.FieldSectionHeader
import com.example.data_collect.ui.components.SectionCard
import com.example.data_collect.util.parseDate
import com.example.data_collect.util.today

@Composable
internal fun MortalityLogForm(
    selectedFlock: Flock,
    onAddMortality: (String, String, Int, String, String?) -> Unit,
    context: Context,
) {
    SectionCard(title = "Mortality log") {
        var mortalityDate by rememberSaveable(selectedFlock.id) { mutableStateOf(today()) }
        var mortalityCountText by rememberSaveable(selectedFlock.id) { mutableStateOf("") }
        var mortalityCause by rememberSaveable(selectedFlock.id) { mutableStateOf("Weakness") }
        var mortalityNotes by rememberSaveable(selectedFlock.id) { mutableStateOf("") }

        FieldSectionHeader(title = "Bird losses", detail = "Count and likely cause")
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
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
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
        SaveButton(label = "Save mortality log") {
            val count = mortalityCountText.toIntOrNull()
            if (parseDate(mortalityDate) == null) {
                Toast.makeText(context, "Use date format YYYY-MM-DD", Toast.LENGTH_SHORT).show()
            } else if (count == null || count <= 0) {
                Toast.makeText(context, "Enter birds lost above 0", Toast.LENGTH_SHORT).show()
            } else if (mortalityCause.isBlank()) {
                Toast.makeText(context, "Enter mortality cause", Toast.LENGTH_SHORT).show()
            } else {
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
            }
        }
    }
}
