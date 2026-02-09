package com.habitmind.data.repository

import com.habitmind.data.database.dao.JournalDao
import com.habitmind.data.database.entity.DailyLog
import com.habitmind.data.database.entity.JournalEntry
import com.habitmind.data.database.entity.JournalEntryType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * Repository for journal and daily log operations
 */
class JournalRepository(private val journalDao: JournalDao) {
    
    val allEntries: Flow<List<JournalEntry>> = journalDao.getAllEntries()
    val allDailyLogs: Flow<List<DailyLog>> = journalDao.getAllDailyLogs()
    
    fun getRecentEntries(limit: Int = 5): Flow<List<JournalEntry>> {
        return journalDao.getRecentEntries(limit)
    }
    
    fun getEntriesByType(type: JournalEntryType): Flow<List<JournalEntry>> {
        return journalDao.getEntriesByType(type)
    }
    
    fun getEntriesInRange(start: LocalDateTime, end: LocalDateTime): Flow<List<JournalEntry>> {
        return journalDao.getEntriesInRange(start, end)
    }
    
    suspend fun getEntryById(id: Long): JournalEntry? = journalDao.getById(id)
    
    suspend fun insertEntry(entry: JournalEntry): Long = journalDao.insert(entry)
    
    suspend fun updateEntry(entry: JournalEntry) = journalDao.update(entry)
    
    suspend fun deleteEntry(entry: JournalEntry) = journalDao.delete(entry)
    
    // Quick note / daily log
    suspend fun addQuickNote(content: String): Long {
        val log = DailyLog(content = content)
        return journalDao.insertDailyLog(log)
    }
    
    fun getRecentDailyLogs(limit: Int = 10): Flow<List<DailyLog>> {
        return journalDao.getRecentDailyLogs(limit)
    }
    
    suspend fun deleteDailyLog(log: DailyLog) = journalDao.deleteDailyLog(log)
}
