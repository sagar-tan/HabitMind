package com.habitmind.data.repository

import com.habitmind.data.database.dao.DailyTrackerDao
import com.habitmind.data.database.entity.DailyTracker
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Repository for daily tracker (Notion-style discipline updates)
 */
class DailyTrackerRepository(private val dao: DailyTrackerDao) {
    
    val allTrackers: Flow<List<DailyTracker>> = dao.getAll()
    
    fun getByDate(date: LocalDate): Flow<DailyTracker?> = dao.getByDate(date)
    
    suspend fun getByDateSync(date: LocalDate): DailyTracker? = dao.getByDateSync(date)
    
    fun getRecent(limit: Int = 14): Flow<List<DailyTracker>> = dao.getRecent(limit)
    
    fun getInRange(start: LocalDate, end: LocalDate): Flow<List<DailyTracker>> = 
        dao.getInRange(start, end)
    
    /**
     * Upsert: creates or updates a tracker for the given date
     * Auto-computes discipline score before saving
     */
    suspend fun save(tracker: DailyTracker): Long {
        return dao.upsert(tracker.withUpdatedScore())
    }
    
    suspend fun delete(tracker: DailyTracker) = dao.delete(tracker)
    
    suspend fun getAverageScore(start: LocalDate, end: LocalDate): Float {
        return dao.getAverageScore(start, end) ?: 0f
    }
}
