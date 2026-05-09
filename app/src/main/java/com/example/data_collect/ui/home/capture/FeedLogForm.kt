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
internal fun FeedLogForm(
    selectedFlock: Flock,
    onAddFeed: (String, String, Double, String, Double, String?) -> Unit,
    context: Context,
) {
    SectionCard(title = "Feed log") {
        var feedDate by rememberSaveable(selectedFlock.id) { mutableStateOf(today()) }
        var feedKgText by rememberSaveable(selectedFlock.id) { mutableStateOf("") }
        var feedType by rememberSaveable(selectedFlock.id) { mutableStateOf("Starter") }
        var feedCostText by rememberSaveable(selectedFlock.id) { mutableStateOf("") }
        var feedNotes by rememberSaveable(selectedFlock.id) { mutableStateOf("") }

        FieldSectionHeader(title = "Daily feed", detail = "Quantity, feed type, and cost")
        OutlinedTextField(
            value = feedDate,
            onValueChange = { feedDate = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Date (YYYY-MM-DD)") },
            singleLine = true
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = feedKgText,
                onValueChange = { feedKgText = it },
                modifier = Modifier.weight(1f),
                label = { Text("Kg") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
            OutlinedTextField(
                value = feedCostText,
                onValueChange = { feedCostText = it },
                modifier = Modifier.weight(1f),
                label = { Text("Cost (₦)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
        }
        OutlinedTextField(
            value = feedType,
            onValueChange = { feedType = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Feed type") },
            singleLine = true
        )
        OutlinedTextField(
            value = feedNotes,
            onValueChange = { feedNotes = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Notes (optional)") }
        )
        SaveButton(label = "Save feed log") {
            val kg = feedKgText.toDoubleOrNull()
            val cost = feedCostText.toDoubleOrNull()
            if (parseDate(feedDate) == null) {
                Toast.makeText(context, "Use date format YYYY-MM-DD", Toast.LENGTH_SHORT).show()
            } else if (kg == null || kg <= 0.0) {
                Toast.makeText(context, "Enter feed quantity above 0", Toast.LENGTH_SHORT).show()
            } else if (feedType.isBlank()) {
                Toast.makeText(context, "Enter feed type", Toast.LENGTH_SHORT).show()
            } else if (cost == null || cost < 0.0) {
                Toast.makeText(context, "Enter feed cost 0 or above", Toast.LENGTH_SHORT).show()
            } else {
                onAddFeed(selectedFlock.id, feedDate, kg, feedType, cost, feedNotes.ifBlank { null })
                Toast.makeText(context, "Feed log saved", Toast.LENGTH_SHORT).show()
                feedKgText = ""
                feedCostText = ""
                feedNotes = ""
            }
        }
    }
}
