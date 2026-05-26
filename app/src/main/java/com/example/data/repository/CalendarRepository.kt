package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.database.CalendarEntry
import com.example.data.database.EntryDao
import com.example.data.sync.RemoteRequest
import com.example.data.sync.SyncApi
import com.example.data.sync.SyncDataWrapper
import com.example.util.ReminderScheduler
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class CalendarRepository(
    private val entryDao: EntryDao,
    private val syncApi: SyncApi = SyncApi.create()
) {
    private val TAG = "CalendarRepository"

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val listType = Types.newParameterizedType(List::class.java, CalendarEntry::class.java)
    private val listAdapter = moshi.adapter<List<CalendarEntry>>(listType)

    val allEntriesFlow: Flow<List<CalendarEntry>> = entryDao.getAllEntriesFlow()

    fun getEntriesForDateFlow(date: String): Flow<List<CalendarEntry>> = entryDao.getEntriesForDate(date)

    suspend fun insertEntry(context: Context, entry: CalendarEntry) {
        withContext(Dispatchers.IO) {
            val insertedId = entryDao.insertEntry(entry)
            val updatedEntry = entry.copy(id = insertedId.toInt())
            ReminderScheduler.scheduleReminder(context, updatedEntry)
        }
    }

    suspend fun deleteEntry(context: Context, entry: CalendarEntry) {
        withContext(Dispatchers.IO) {
            ReminderScheduler.cancelReminder(context, entry)
            entryDao.deleteEntry(entry)
        }
    }

    suspend fun updateEntryCompletion(context: Context, entry: CalendarEntry, isCompleted: Boolean) {
        withContext(Dispatchers.IO) {
            val updated = entry.copy(isCompleted = isCompleted)
            entryDao.insertEntry(updated)
            if (isCompleted) {
                ReminderScheduler.cancelReminder(context, updated)
            } else {
                ReminderScheduler.scheduleReminder(context, updated)
            }
        }
    }

    suspend fun pushSync(context: Context): Result<String> = withContext(Dispatchers.IO) {
        try {
            val entries = entryDao.getAllEntries()
            val entriesJson = listAdapter.toJson(entries)
            
            val request = RemoteRequest(
                name = "ChronosSync_CloudData_" + System.currentTimeMillis(),
                data = SyncDataWrapper(entriesJson = entriesJson)
            )

            val response = syncApi.createSyncObject(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Log.d(TAG, "Successfully pushed sync. Remote ID: ${body.id}")
                    Result.success(body.id)
                } else {
                    Result.failure(Exception("Sync payload empty"))
                }
            } else {
                Result.failure(Exception("HTTP Error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Push sync failed", e)
            Result.failure(e)
        }
    }

    suspend fun pullSync(context: Context, code: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = syncApi.getSyncObject(code.trim())
            if (response.isSuccessful) {
                val body = response.body()
                val entriesJson = body?.data?.entriesJson
                if (entriesJson != null) {
                    val entries = listAdapter.fromJson(entriesJson)
                    if (entries != null) {
                        // Cancel all current scheduled alarms first
                        val currentEntries = entryDao.getAllEntries()
                        currentEntries.forEach { ReminderScheduler.cancelReminder(context, it) }

                        // Overwrite local db
                        entryDao.clearAllEntries()
                        val entriesWithResetIds = entries.map { it.copy(id = 0) } // Insert as new to allow automatic ID incrementation safely
                        entryDao.insertAllEntries(entriesWithResetIds)

                        // Reschedule all newly synced alarms
                        val newlySavedEntries = entryDao.getAllEntries()
                        ReminderScheduler.rescheduleAllReminders(context, newlySavedEntries)

                        Log.d(TAG, "Successfully pulled and updated local entries. Count: ${newlySavedEntries.size}")
                        Result.success(Unit)
                    } else {
                        Result.failure(Exception("Failed to decode entry payload"))
                    }
                } else {
                    Result.failure(Exception("No calendar payload found in sync object"))
                }
            } else {
                Result.failure(Exception("Sync Code not found (HTTP ${response.code()})"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Pull sync failed", e)
            Result.failure(e)
        }
    }
}
