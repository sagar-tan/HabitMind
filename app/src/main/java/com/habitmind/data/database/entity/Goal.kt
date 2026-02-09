package com.habitmind.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Goal entity
 * Long-term goals with weekly progress tracking
 */
@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val targetDate: LocalDate? = null,
    val progressPercent: Int = 0, // 0-100
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val isCompleted: Boolean = false,
    val completedAt: LocalDateTime? = null
)

/**
 * Goal progress update
 * Weekly updates on goal progress
 */
@Entity(
    tableName = "goal_updates",
    primaryKeys = ["goalId", "weekStart"]
)
data class GoalUpdate(
    val goalId: Long,
    val weekStart: LocalDate, // Monday of the week
    val progressDelta: Int = 0, // Change in progress
    val notes: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now()
)
