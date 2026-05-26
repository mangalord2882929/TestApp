package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.database.CalendarEntry
import com.example.data.repository.CalendarRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CalendarViewModel(
    application: Application,
    private val repository: CalendarRepository
) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("chrono_prefs", Context.MODE_PRIVATE)
    
    val selectedDate = MutableStateFlow(getCurrentDateString())
    val currentMonth = MutableStateFlow(Calendar.getInstance())
    
    val syncCode = MutableStateFlow(prefs.getString("sync_code", "") ?: "")
    
    val isSyncing = MutableStateFlow(false)
    val syncMessage = MutableStateFlow<String?>(null)

    val allEntries: StateFlow<List<CalendarEntry>> = repository.allEntriesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val activeEntriesForSelectedDate: StateFlow<List<CalendarEntry>> = combine(selectedDate, allEntries) { date, entries ->
        entries.filter { it.date == date }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun selectDate(date: String) {
        selectedDate.value = date
    }

    fun nextMonth() {
        val next = currentMonth.value.clone() as Calendar
        next.add(Calendar.MONTH, 1)
        currentMonth.value = next
    }

    fun prevMonth() {
        val prev = currentMonth.value.clone() as Calendar
        prev.add(Calendar.MONTH, -1)
        currentMonth.value = prev
    }

    fun saveEntry(entry: CalendarEntry) {
        viewModelScope.launch {
            repository.insertEntry(getApplication(), entry)
        }
    }

    fun deleteEntry(entry: CalendarEntry) {
        viewModelScope.launch {
            repository.deleteEntry(getApplication(), entry)
        }
    }

    fun toggleEntryCompletion(entry: CalendarEntry, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.updateEntryCompletion(getApplication(), entry, isCompleted)
        }
    }

    fun pushSync() {
        viewModelScope.launch {
            isSyncing.value = true
            syncMessage.value = "Pushing calendar data to cloud..."
            val result = repository.pushSync(getApplication())
            result.onSuccess { code ->
                updateSyncCode(code)
                syncMessage.value = "Success! Data backed up to cloud. Sync Code: $code"
            }.onFailure { e ->
                syncMessage.value = "Push failed: ${e.message}"
            }
            isSyncing.value = false
        }
    }

    fun pullSync(codeToUse: String) {
        if (codeToUse.isBlank()) {
            syncMessage.value = "Please enter a valid Sync Code"
            return
        }
        viewModelScope.launch {
            isSyncing.value = true
            syncMessage.value = "Restoring calendar data from cloud..."
            val result = repository.pullSync(getApplication(), codeToUse)
            result.onSuccess {
                updateSyncCode(codeToUse)
                syncMessage.value = "Success! Calendar restored and synced successfully!"
            }.onFailure { e ->
                syncMessage.value = "Restore failed: ${e.message}"
            }
            isSyncing.value = false
        }
    }

    fun clearSyncMessage() {
        syncMessage.value = null
    }

    private fun updateSyncCode(code: String) {
        syncCode.value = code.trim()
        prefs.edit().putString("sync_code", code.trim()).apply()
    }

    private fun getCurrentDateString(): String {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return format.format(Calendar.getInstance().time)
    }

    class Factory(
        private val application: Application,
        private val repository: CalendarRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CalendarViewModel::class.java)) {
                return CalendarViewModel(application, repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
