package com.habitmind.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Daily Tracker entity — Notion-style discipline update
 * One entry per day, covering checkboxes, sliders, numeric inputs, photos, text
 */
@Entity(
    tableName = "daily_trackers",
    indices = [Index(value = ["date"], unique = true)]
)
data class DailyTracker(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: LocalDate = LocalDate.now(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    
    // === Checkboxes ===
    val meditation: Boolean = false,
    val noJunkFood: Boolean = false,
    val noMusic: Boolean = false,
    val noMbt: Boolean = false,
    val workout: Boolean = false,
    
    // === Workout details ===
    val workoutType: String = "",       // "Walk", "Run", "Gym", "Yoga", etc.
    val workoutDurationMin: Int = 0,
    
    // === 1-10 Sliders ===
    val energy: Int = 0,
    val focus: Int = 0,
    val mood: Int = 0,
    val stress: Int = 0,
    
    // === Numeric inputs ===
    val screenTimeHours: Float = 0f,
    val sleepHours: Float = 0f,
    val socialMediaMin: Int = 0,
    val waterIntakeLiters: Float = 0f,
    val studyWorkHours: Float = 0f,
    
    // === Photos (comma-separated URIs) ===
    val photoPaths: String = "",
    
    // === Text fields ===
    val gratitude: String = "",
    val winOfTheDay: String = "",
    val notesEmotions: String = "",
    
    // === Computed score ===
    val disciplineScore: Int = 0
) {
    /**
     * Auto-compute discipline score (0-10) based on habits and thresholds
     */
    fun computeDisciplineScore(): Int {
        var score = 0
        // Booleans: +1 each for positive habits
        if (meditation) score++
        if (noJunkFood) score++
        if (noMusic) score++
        if (noMbt) score++
        if (workout) score++
        
        // Thresholds
        if (sleepHours >= 7f) score++
        if (screenTimeHours <= 3f && screenTimeHours > 0f) score++
        if (waterIntakeLiters >= 2f) score++
        if (studyWorkHours >= 2f) score++
        if (socialMediaMin <= 30 && socialMediaMin >= 0) score++
        
        return score.coerceIn(0, 10)
    }
    
    fun withUpdatedScore(): DailyTracker {
        return copy(
            disciplineScore = computeDisciplineScore(),
            updatedAt = LocalDateTime.now()
        )
    }
    
    val photoList: List<String>
        get() = if (photoPaths.isBlank()) emptyList() 
                else photoPaths.split(",").filter { it.isNotBlank() }
}
