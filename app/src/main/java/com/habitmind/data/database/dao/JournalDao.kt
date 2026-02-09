package com.habitmind.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.habitmind.data.database.entity.DailyLog
import com.habitmind.data.database.entity.JournalEntry
import com.habitmind.data.database.entity.JournalEntryType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface JournalDao {
    
    // --- Journal Entries ---
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: JournalEntry): Long
    
    @Update
    suspend fun update(entry: JournalEntry)
    
    @Delete
    suspend fun delete(entry: JournalEntry)
    
    @Query("SELECT * FROM journal_entries WHERE id = :id")
    suspend fun getById(id: Long): JournalEntry?
    
    @Query("SELECT * FROM journal_entries ORDER BY timestamp DESC")
    fun getAllEntries(): Flow<List<JournalEntry>>
    
    @Query("SELECT * FROM journal_entries WHERE type = :type ORDER BY timestamp DESC")
    fun getEntriesByType(type: JournalEntryType): Flow<List<JournalEntry>>
    
    @Query("""
        SELECT * FROM journal_entries 
        WHERE timestamp BETWEEN :startTime AND :endTime 
        ORDER BY timestamp DESC
    """)
    fun getEntriesInRange(startTime: LocalDateTime, endTime: LocalDateTime): Flow<List<JournalEntry>>
    
    @Query("SELECT * FROM journal_entries WHERE tags LIKE '%' || :tag || '%' ORDER BY timestamp DESC")
    fun getEntriesByTag(tag: String): Flow<List<JournalEntry>>
    
    // Get recent entries (for home screen)
    @Query("SELECT * FROM journal_entries ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentEntries(limit: Int = 5): Flow<List<JournalEntry>>
    
    // --- Daily Logs (Quick Notes) ---
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyLog(log: DailyLog): Long
    
    @Delete
    suspend fun deleteDailyLog(log: DailyLog)
    
    @Query("SELECT * FROM daily_logs ORDER BY timestamp DESC")
    fun getAllDailyLogs(): Flow<List<DailyLog>>
    
    @Query("""
        SELECT * FROM daily_logs 
        WHERE DATE(timestamp) = DATE(:date)
        ORDER BY timestamp DESC
    """)
    fun getDailyLogsForDate(date: LocalDateTime): Flow<List<DailyLog>>
    
    // Get recent daily logs
    @Query("SELECT * FROM daily_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentDailyLogs(limit: Int = 10): Flow<List<DailyLog>>
}
