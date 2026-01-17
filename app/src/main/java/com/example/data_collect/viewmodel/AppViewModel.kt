package com.example.data_collect.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data_collect.data.AppRepository
import com.example.data_collect.data.model.AppState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AppRepository(application.applicationContext)
    val appState: StateFlow<AppState> = repository.appState

    fun setSelectedFlock(flockId: String) {
        viewModelScope.launch { repository.setSelectedFlock(flockId) }
    }

    fun addFeedLog(
        flockId: String,
        date: String,
        feedKg: Double,
        feedType: String,
        cost: Double,
        notes: String?,
    ) {
        viewModelScope.launch { repository.addFeed(flockId, date, feedKg, feedType, cost, notes) }
    }

    fun addMortalityLog(
        flockId: String,
        date: String,
        count: Int,
        cause: String,
        notes: String?,
    ) {
        viewModelScope.launch { repository.addMortality(flockId, date, count, cause, notes) }
    }

    fun addEggLog(
        flockId: String,
        date: String,
        collected: Int,
        cracked: Int,
        notes: String?,
    ) {
        viewModelScope.launch { repository.addEggs(flockId, date, collected, cracked, notes) }
    }

    fun addTreatmentLog(
        flockId: String,
        date: String,
        treatment: String,
        dosage: String,
        administeredBy: String,
        notes: String?,
    ) {
        viewModelScope.launch {
            repository.addTreatment(flockId, date, treatment, dosage, administeredBy, notes)
        }
    }

    fun addEnvLog(
        flockId: String,
        date: String,
        temperature: Double,
        humidity: Double,
        notes: String?,
    ) {
        viewModelScope.launch { repository.addEnv(flockId, date, temperature, humidity, notes) }
    }

    fun simulateSync() {
        viewModelScope.launch { repository.simulateSync() }
    }

    fun exportToUri(uri: Uri, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.exportToUri(uri)
            onResult(success)
        }
    }

    fun importFromUri(uri: Uri, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.importFromUri(uri)
            onResult(success)
        }
    }
}
