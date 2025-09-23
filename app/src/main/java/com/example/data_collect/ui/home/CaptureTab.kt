package com.example.data_collect.ui.home

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data_collect.data.model.Flock
import com.example.data_collect.ui.components.SectionCard
import com.example.data_collect.util.today

@RequiresApi(Build.VERSION_CODES.O)
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
    val isLayerFlock = selectedFlock.type.equals("layers", ignoreCase = true)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionCard(title = "Feed") {
            var feedDate by rememberSaveable(selectedFlock.id) { mutableStateOf(today()) }
            var feedKgText by rememberSaveable(selectedFlock.id) { mutableStateOf("") }
            var feedType by rememberSaveable(selectedFlock.id) { mutableStateOf("Starter") }
            var feedCostText by rememberSaveable(selectedFlock.id) { mutableStateOf("") }
            var feedNotes by rememberSaveable(selectedFlock.id) { mutableStateOf("") }

            OutlinedTextField(
                value = feedDate,
                onValueChange = { feedDate = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Date (YYYY-MM-DD)") },
                singleLine = true
            )
            OutlinedTextField(
                value = feedKgText,
                onValueChange = { feedKgText = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Feed quantity (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            OutlinedTextField(
                value = feedType,
                onValueChange = { feedType = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Feed type") },
                singleLine = true
            )
            OutlinedTextField(
                value = feedCostText,
                onValueChange = { feedCostText = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Feed cost (₦)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            OutlinedTextField(
                value = feedNotes,
                onValueChange = { feedNotes = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Notes (optional)") }
            )
            Button(
                onClick = {
                    val kg = feedKgText.toDoubleOrNull()
                    val cost = feedCostText.toDoubleOrNull()
                    if (kg != null && cost != null) {
                        onAddFeed(selectedFlock.id, feedDate, kg, feedType, cost, feedNotes.ifBlank { null })
                        Toast.makeText(context, "Feed log saved", Toast.LENGTH_SHORT).show()
                        feedKgText = ""
                        feedCostText = ""
                        feedNotes = ""
                    } else {
                        Toast.makeText(context, "Enter valid feed data", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Save Feed")
            }
        }

        SectionCard(title = "Mortality") {
            var mortalityDate by rememberSaveable(selectedFlock.id) { mutableStateOf(today()) }
            var mortalityCountText by rememberSaveable(selectedFlock.id) { mutableStateOf("") }
            var mortalityCause by rememberSaveable(selectedFlock.id) { mutableStateOf("Weakness") }
            var mortalityNotes by rememberSaveable(selectedFlock.id) { mutableStateOf("") }

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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
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
            Button(
                onClick = {
                    val count = mortalityCountText.toIntOrNull()
                    if (count != null && count >= 0) {
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
                    } else {
                        Toast.makeText(context, "Enter a valid count", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Save Mortality")
            }
        }

        if (isLayerFlock) {
            SectionCard(title = "Eggs") {
                var eggDate by rememberSaveable(selectedFlock.id) { mutableStateOf(today()) }
                var eggsCollectedText by rememberSaveable(selectedFlock.id) { mutableStateOf("") }
                var eggsCrackedText by rememberSaveable(selectedFlock.id) { mutableStateOf("0") }
                var eggNotes by rememberSaveable(selectedFlock.id) { mutableStateOf("") }

                OutlinedTextField(
                    value = eggDate,
                    onValueChange = { eggDate = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Date (YYYY-MM-DD)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = eggsCollectedText,
                    onValueChange = { eggsCollectedText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Eggs collected") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = eggsCrackedText,
                    onValueChange = { eggsCrackedText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Cracked eggs") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = eggNotes,
                    onValueChange = { eggNotes = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Notes (optional)") }
                )
                Button(
                    onClick = {
                        val collected = eggsCollectedText.toIntOrNull()
                        val cracked = eggsCrackedText.toIntOrNull()
                        if (collected != null && cracked != null) {
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
                        } else {
                            Toast.makeText(context, "Enter valid egg counts", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text("Save Eggs")
                }
            }
        }

        SectionCard(title = "Treatment") {
            var treatmentDate by rememberSaveable(selectedFlock.id) { mutableStateOf(today()) }
            var treatmentName by rememberSaveable(selectedFlock.id) { mutableStateOf("Vitamin boost") }
            var treatmentDose by rememberSaveable(selectedFlock.id) { mutableStateOf("") }
            var administeredBy by rememberSaveable(selectedFlock.id) { mutableStateOf("Farm Vet") }
            var treatmentNotes by rememberSaveable(selectedFlock.id) { mutableStateOf("") }

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
            OutlinedTextField(
                value = treatmentDose,
                onValueChange = { treatmentDose = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Dosage") }
            )
            OutlinedTextField(
                value = administeredBy,
                onValueChange = { administeredBy = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Administered by") }
            )
            OutlinedTextField(
                value = treatmentNotes,
                onValueChange = { treatmentNotes = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Notes (optional)") }
            )
            Button(
                onClick = {
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
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Save Treatment")
            }
        }

        SectionCard(title = "Environment") {
            var envDate by rememberSaveable(selectedFlock.id) { mutableStateOf(today()) }
            var temperatureText by rememberSaveable(selectedFlock.id) { mutableStateOf("") }
            var humidityText by rememberSaveable(selectedFlock.id) { mutableStateOf("") }
            var envNotes by rememberSaveable(selectedFlock.id) { mutableStateOf("") }

            OutlinedTextField(
                value = envDate,
                onValueChange = { envDate = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Date (YYYY-MM-DD)") },
                singleLine = true
            )
            OutlinedTextField(
                value = temperatureText,
                onValueChange = { temperatureText = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Temperature") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            OutlinedTextField(
                value = humidityText,
                onValueChange = { humidityText = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Humidity") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            OutlinedTextField(
                value = envNotes,
                onValueChange = { envNotes = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Notes (optional)") }
            )
            Button(
                onClick = {
                    val temperature = temperatureText.toDoubleOrNull()
                    val humidity = humidityText.toDoubleOrNull()
                    if (temperature != null && humidity != null) {
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
                    } else {
                        Toast.makeText(context, "Enter valid environment data", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Save Environment")
            }
        }
    }
}
