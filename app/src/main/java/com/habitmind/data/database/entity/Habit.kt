package com.habitmind.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Habit entity
 * Represents a recurring habit the user wants to track
 */
@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val isArchived: Boolean = false,
    val reminderTime: String? = null, // HH:mm format
    val color: String = "#E8E8EC" // Accent color hex
)

/**
 * Habit completion record
 * Tracks whether a habit was completed on a specific date
 */
@Entity(
    tableName = "habit_completions",
    primaryKeys = ["habitId", "date"]
)
data class HabitCompletion(
    val habitId: Long,
    val date: LocalDate,
    val isCompleted: Boolean = true,
    val completedAt: LocalDateTime? = null
)
