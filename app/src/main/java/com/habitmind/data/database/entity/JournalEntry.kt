package com.habitmind.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * Journal entry types
 */
enum class JournalEntryType {
    TEXT,
    VOICE,
    IMAGE
}

/**
 * Journal entry entity
 * Supports text, voice notes, and image entries
 */
@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: JournalEntryType = JournalEntryType.TEXT,
    val content: String = "", // Text content or caption
    val mediaPath: String? = null, // Path to voice/image file
    val mediaDurationSeconds: Int? = null, // For voice notes
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val tags: String = "", // Comma-separated tags
    val mood: Int? = null // 1-5 mood rating, optional
)

/**
 * Daily log / quick note entity
 * Minimal friction, one-tap notes
 */
@Entity(tableName = "daily_logs")
data class DailyLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val content: String,
    val timestamp: LocalDateTime = LocalDateTime.now()
)
