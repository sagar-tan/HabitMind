package com.habitmind.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.habitmind.data.database.entity.DailyTracker
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface DailyTrackerDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(tracker: DailyTracker): Long
    
    @Update
    suspend fun update(tracker: DailyTracker)
    
    @Delete
    suspend fun delete(tracker: DailyTracker)
    
    @Query("SELECT * FROM daily_trackers WHERE date = :date LIMIT 1")
    fun getByDate(date: LocalDate): Flow<DailyTracker?>
    
    @Query("SELECT * FROM daily_trackers WHERE date = :date LIMIT 1")
    suspend fun getByDateSync(date: LocalDate): DailyTracker?
    
    @Query("SELECT * FROM daily_trackers ORDER BY date DESC")
    fun getAll(): Flow<List<DailyTracker>>
    
    @Query("SELECT * FROM daily_trackers WHERE date BETWEEN :start AND :end ORDER BY date DESC")
    fun getInRange(start: LocalDate, end: LocalDate): Flow<List<DailyTracker>>
    
    @Query("SELECT * FROM daily_trackers ORDER BY date DESC LIMIT :limit")
    fun getRecent(limit: Int = 14): Flow<List<DailyTracker>>
    
    @Query("SELECT AVG(disciplineScore) FROM daily_trackers WHERE date BETWEEN :start AND :end")
    suspend fun getAverageScore(start: LocalDate, end: LocalDate): Float?
}
