package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.database.AppDatabase
import com.example.util.NotificationHelper
import com.example.util.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "ReminderReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d(TAG, "onReceive invoked. Action: $action")

        val goAsyncResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (action == Intent.ACTION_BOOT_COMPLETED || 
                    action == Intent.ACTION_TIME_CHANGED || 
                    action == Intent.ACTION_TIMEZONE_CHANGED) {
                    
                    Log.d(TAG, "System event triggered rescheduling of all active reminders.")
                    val database = AppDatabase.getDatabase(context)
                    val activeEntries = database.entryDao().getAllEntries()
                    ReminderScheduler.rescheduleAllReminders(context, activeEntries)
                    
                } else {
                    // Standard scheduled alarm trigger
                    val entryId = intent.getIntExtra("entry_id", -1)
                    if (entryId != -1) {
                        val database = AppDatabase.getDatabase(context)
                        val entry = database.entryDao().getAllEntries().find { it.id == entryId }
                        if (entry != null && !entry.isCompleted) {
                            Log.d(TAG, "Triggered reminder for entry: ${entry.title}")
                            
                            NotificationHelper.showNotification(
                                context = context,
                                id = entry.id,
                                title = entry.title,
                                description = entry.description,
                                priority = entry.priority,
                                soundEnabled = entry.soundEnabled,
                                vibrateEnabled = entry.vibrateEnabled
                            )

                            if (entry.recurrence != "NONE") {
                                ReminderScheduler.scheduleNextOccurrence(context, entry)
                            }
                        } else {
                            Log.d(TAG, "Reminder skipped. Entry is null, deleted, or completed.")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in ReminderReceiver pipeline", e)
            } finally {
                goAsyncResult.finish()
            }
        }
    }
}
