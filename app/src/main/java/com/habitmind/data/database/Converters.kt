package com.habitmind.data.database

import androidx.room.TypeConverter
import com.habitmind.data.database.entity.JournalEntryType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Room type converters for custom types
 */
class Converters {
    
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    
    // LocalDate converters
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.format(dateFormatter)
    }
    
    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it, dateFormatter) }
    }
    
    // LocalDateTime converters
    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): String? {
        return dateTime?.format(dateTimeFormatter)
    }
    
    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? {
        return value?.let { 
            // Handle both ISO format (2024-02-09T20:30:45) and SQLite format (2024-02-09 20:30:45)
            val normalized = it.replace(" ", "T")
            try {
                LocalDateTime.parse(normalized, dateTimeFormatter)
            } catch (e: Exception) {
                null
            }
        }
    }
    
    // JournalEntryType converters
    @TypeConverter
    fun fromJournalEntryType(type: JournalEntryType): String {
        return type.name
    }
    
    @TypeConverter
    fun toJournalEntryType(value: String): JournalEntryType {
        return JournalEntryType.valueOf(value)
    }
}
