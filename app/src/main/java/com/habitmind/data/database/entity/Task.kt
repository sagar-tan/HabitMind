package com.habitmind.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Task entity
 * Represents a daily task with progress tracking
 */
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val date: LocalDate = LocalDate.now(),
    val progress: Int = 0, // 0-100 percentage
    val estimatedMinutes: Int = 30,
    val isCarriedForward: Boolean = false,
    val originalDate: LocalDate? = null, // If carried forward, original date
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val completedAt: LocalDateTime? = null,
    val priority: Int = 0 // 0=normal, 1=high, 2=urgent
)
