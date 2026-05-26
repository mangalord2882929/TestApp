package com.example.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryDao {
    @Query("SELECT * FROM calendar_entries ORDER BY date ASC, time ASC")
    fun getAllEntriesFlow(): Flow<List<CalendarEntry>>

    @Query("SELECT * FROM calendar_entries")
    suspend fun getAllEntries(): List<CalendarEntry>

    @Query("SELECT * FROM calendar_entries WHERE date = :date ORDER BY time ASC")
    fun getEntriesForDate(date: String): Flow<List<CalendarEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: CalendarEntry): Long

    @Delete
    suspend fun deleteEntry(entry: CalendarEntry)

    @Query("DELETE FROM calendar_entries WHERE id = :id")
    suspend fun deleteEntryById(id: Int)

    @Query("DELETE FROM calendar_entries")
    suspend fun clearAllEntries()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllEntries(entries: List<CalendarEntry>)
}
