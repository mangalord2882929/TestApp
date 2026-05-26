package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calendar_entries")
data class CalendarEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val date: String, // format: "YYYY-MM-DD"
    val time: String, // format: "HH:MM"
    val recurrence: String = "NONE", // "NONE", "DAILY", "WEEKLY", "MONTHLY"
    val notifyTimeOffsetMinutes: Int = 0, // e.g., 0, 5, 15, 60
    val soundEnabled: Boolean = true,
    val vibrateEnabled: Boolean = true,
    val priority: String = "DEFAULT", // "HIGH", "DEFAULT", "LOW"
    val isCompleted: Boolean = false
)
