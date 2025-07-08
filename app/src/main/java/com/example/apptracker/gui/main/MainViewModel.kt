// MainViewModel.kt
package com.example.apptracker.gui.main

import android.util.Log // Import for Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.apptracker.data.AppUsage
import com.example.apptracker.data.AppUsageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class MainViewModel(private val repository: AppUsageRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<List<AppUsage>>(emptyList())
    val uiState: StateFlow<List<AppUsage>> = _uiState.asStateFlow()

    init {
        // Start observing today's data when the ViewModel is created
        viewModelScope.launch {
            val today = getTodayStartTimestamp()
            Log.d("MainViewModel", "Observing usage for date: $today") // Log: Observing date
            repository.getUsageForDate(today).collect { usageList ->
                Log.d("MainViewModel", "Received ${usageList.size} items from DB.") // Log: Data received from DB

                // Filter to show only apps with usage time > 0 (reverted filter)
                val filteredList = usageList.filter { it.usageTimeInMillis > 0 }
                _uiState.value = filteredList
                Log.d("MainViewModel", "UI State updated with ${filteredList.size} filtered items.") // Log: UI State updated

                // Log specific app's usage after UI state update
                filteredList.find { it.packageName == "com.example.apptracker" }?.let { appTrackerUsage ->
                    Log.d("MainViewModel", "AppTracker usage in UI state: ${appTrackerUsage.usageTimeInMillis} ms")
                }
            }
        }
    }

    // Function to manually refresh data
    fun refreshData() {
        viewModelScope.launch {
            Log.d("MainViewModel", "refreshData() called. Triggering repository refresh.") // Log: Refresh triggered
            repository.refreshUsageStatsForToday()
        }
    }

    private fun getTodayStartTimestamp(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}

// Factory class to inject the Repository into the ViewModel
class MainViewModelFactory(private val repository: AppUsageRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
