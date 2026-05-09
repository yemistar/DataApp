package com.example.data_collect.ui.home.capture

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data_collect.data.model.Flock
import com.example.data_collect.ui.components.FieldSectionHeader
import com.example.data_collect.ui.components.SectionCard
import com.example.data_collect.util.parseDate
import com.example.data_collect.util.today

@Composable
internal fun TreatmentLogForm(
    selectedFlock: Flock,
    onAddTreatment: (String, String, String, String, String, String?) -> Unit,
    context: Context,
) {
    SectionCard(title = "Treatment log") {
        var treatmentDate by rememberSaveable(selectedFlock.id) { mutableStateOf(today()) }
        var treatmentName by rememberSaveable(selectedFlock.id) { mutableStateOf("Vitamin boost") }
        var treatmentDose by rememberSaveable(selectedFlock.id) { mutableStateOf("") }
        var administeredBy by rememberSaveable(selectedFlock.id) { mutableStateOf("Farm Vet") }
        var treatmentNotes by rememberSaveable(selectedFlock.id) { mutableStateOf("") }

        FieldSectionHeader(title = "Health action", detail = "Treatment, dose, and owner")
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
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = treatmentDose,
                onValueChange = { treatmentDose = it },
                modifier = Modifier.weight(1f),
                label = { Text("Dosage") },
                singleLine = true
            )
            OutlinedTextField(
                value = administeredBy,
                onValueChange = { administeredBy = it },
                modifier = Modifier.weight(1f),
                label = { Text("By") },
                singleLine = true
            )
        }
        OutlinedTextField(
            value = treatmentNotes,
            onValueChange = { treatmentNotes = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Notes (optional)") }
        )
        SaveButton(label = "Save treatment log") {
            if (parseDate(treatmentDate) == null) {
                Toast.makeText(context, "Use date format YYYY-MM-DD", Toast.LENGTH_SHORT).show()
            } else if (treatmentName.isBlank()) {
                Toast.makeText(context, "Enter treatment name", Toast.LENGTH_SHORT).show()
            } else if (treatmentDose.isBlank()) {
                Toast.makeText(context, "Enter dosage", Toast.LENGTH_SHORT).show()
            } else if (administeredBy.isBlank()) {
                Toast.makeText(context, "Enter who administered it", Toast.LENGTH_SHORT).show()
            } else {
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
            }
        }
    }
}
