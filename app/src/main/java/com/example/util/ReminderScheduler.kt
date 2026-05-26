package com.example.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.database.CalendarEntry
import com.example.receiver.ReminderReceiver
import java.util.Calendar

object ReminderScheduler {
    private const val TAG = "ReminderScheduler"

    fun scheduleReminder(context: Context, entry: CalendarEntry) {
        if (entry.isCompleted) return

        val triggerTimeMs = calculateTriggerTimeMs(entry)
        if (triggerTimeMs == null || triggerTimeMs <= System.currentTimeMillis()) {
            Log.d(TAG, "Not scheduling reminder for Id ${entry.id} as trigger time is in the past.")
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("entry_id", entry.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            entry.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeMs,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeMs,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMs,
                    pendingIntent
                )
            }
            Log.d(TAG, "Scheduled reminder for Id ${entry.id} at $triggerTimeMs")
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException scheduling exact alarm, falling back to setAndAllowWhileIdle", e)
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTimeMs,
                pendingIntent
            )
        }
    }

    fun cancelReminder(context: Context, entry: CalendarEntry) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("entry_id", entry.id)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            entry.id,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d(TAG, "Canceled reminder for Id ${entry.id}")
        }
    }

    fun scheduleNextOccurrence(context: Context, entry: CalendarEntry) {
        // Find next trigger time by shifting current date
        val triggerTimeMs = calculateNextOccurrenceTriggerTimeMs(entry)
        if (triggerTimeMs != null) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, ReminderReceiver::class.java).apply {
                putExtra("entry_id", entry.id)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                entry.id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerTimeMs,
                            pendingIntent
                        )
                    } else {
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerTimeMs,
                            pendingIntent
                        )
                    }
                } else {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeMs,
                        pendingIntent
                    )
                }
                Log.d(TAG, "Scheduled next recurrence for Id ${entry.id} at $triggerTimeMs")
            } catch (e: Exception) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMs,
                    pendingIntent
                )
            }
        }
    }

    fun rescheduleAllReminders(context: Context, entries: List<CalendarEntry>) {
        entries.forEach { entry ->
            cancelReminder(context, entry)
            scheduleReminder(context, entry)
        }
    }

    private fun calculateTriggerTimeMs(entry: CalendarEntry): Long? {
        return try {
            val dateParts = entry.date.split("-")
            val timeParts = entry.time.split(":")
            if (dateParts.size != 3 || timeParts.size != 2) return null

            val calendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, dateParts[0].toInt())
                set(Calendar.MONTH, dateParts[1].toInt() - 1)
                set(Calendar.DAY_OF_MONTH, dateParts[2].toInt())
                set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
                set(Calendar.MINUTE, timeParts[1].toInt())
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            // Subtract notification offset minutes
            if (entry.notifyTimeOffsetMinutes > 0) {
                calendar.add(Calendar.MINUTE, -entry.notifyTimeOffsetMinutes)
            }

            var timeMs = calendar.timeInMillis
            val now = System.currentTimeMillis()

            // If time is in the past but we have recurrence, shift it to future!
            if (timeMs <= now && entry.recurrence != "NONE") {
                while (timeMs <= now) {
                    when (entry.recurrence) {
                        "DAILY" -> calendar.add(Calendar.DAY_OF_YEAR, 1)
                        "WEEKLY" -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
                        "MONTHLY" -> calendar.add(Calendar.MONTH, 1)
                        else -> break
                    }
                    timeMs = calendar.timeInMillis
                }
            }
            timeMs
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating trigger time for Id ${entry.id}", e)
            null
        }
    }

    private fun calculateNextOccurrenceTriggerTimeMs(entry: CalendarEntry): Long? {
        val triggerTimeMs = calculateTriggerTimeMs(entry) ?: return null
        val calendar = Calendar.getInstance().apply {
            timeInMillis = triggerTimeMs
        }
        val now = System.currentTimeMillis()

        // Force moving to the next recurrence in the future
        while (calendar.timeInMillis <= now || calendar.timeInMillis == triggerTimeMs) {
            when (entry.recurrence) {
                "DAILY" -> calendar.add(Calendar.DAY_OF_YEAR, 1)
                "WEEKLY" -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
                "MONTHLY" -> calendar.add(Calendar.MONTH, 1)
                else -> break
            }
        }
        return calendar.timeInMillis
    }
}
