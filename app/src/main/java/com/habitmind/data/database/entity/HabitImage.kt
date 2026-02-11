package com.habitmind.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Image slot types for habit progress photos
 */
enum class ImageSlot {
    FRONT, LEFT, RIGHT, BACK, FACE
}

/**
 * Habit image entity
 * Stores daily progress photos for habits (5 slots per day)
 */
@Entity(tableName = "habit_images")
data class HabitImage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitId: Long,
    val date: LocalDate = LocalDate.now(),
    val slot: ImageSlot,
    val imagePath: String,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
