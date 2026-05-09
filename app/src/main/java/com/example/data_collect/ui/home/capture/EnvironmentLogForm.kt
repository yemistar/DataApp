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
import com.example.data_collect.ui.components.FieldSectionHeader
import com.example.data_collect.ui.components.SectionCard
import com.example.data_collect.util.parseDate
import com.example.data_collect.util.today

@Composable
internal fun EnvironmentLogForm(
    selectedFlock: Flock,
    onAddEnvironment: (String, String, Double, Double, String?) -> Unit,
    context: Context,
) {
    SectionCard(title = "Environment log") {
        var envDate by rememberSaveable(selectedFlock.id) { mutableStateOf(today()) }
        var temperatureText by rememberSaveable(selectedFlock.id) { mutableStateOf("") }
        var humidityText by rememberSaveable(selectedFlock.id) { mutableStateOf("") }
        var envNotes by rememberSaveable(selectedFlock.id) { mutableStateOf("") }

        FieldSectionHeader(title = "House conditions", detail = "Temperature and humidity")
        OutlinedTextField(
            value = envDate,
            onValueChange = { envDate = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Date (YYYY-MM-DD)") },
            singleLine = true
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = temperatureText,
                onValueChange = { temperatureText = it },
                modifier = Modifier.weight(1f),
                label = { Text("Temp °C") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
            OutlinedTextField(
                value = humidityText,
                onValueChange = { humidityText = it },
                modifier = Modifier.weight(1f),
                label = { Text("Humidity %") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
        }
        OutlinedTextField(
            value = envNotes,
            onValueChange = { envNotes = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Notes (optional)") }
        )
        SaveButton(label = "Save environment log") {
            val temperature = temperatureText.toDoubleOrNull()
            val humidity = humidityText.toDoubleOrNull()
            if (parseDate(envDate) == null) {
                Toast.makeText(context, "Use date format YYYY-MM-DD", Toast.LENGTH_SHORT).show()
            } else if (temperature == null || temperature <= 0.0) {
                Toast.makeText(context, "Enter temperature above 0", Toast.LENGTH_SHORT).show()
            } else if (humidity == null || humidity !in 0.0..100.0) {
                Toast.makeText(context, "Enter humidity from 0 to 100", Toast.LENGTH_SHORT).show()
            } else {
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
            }
        }
    }
}
