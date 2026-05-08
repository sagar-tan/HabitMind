package com.habitmind.data.database

import androidx.room.TypeConverter
import com.habitmind.data.database.entity.JournalEntryType
import com.habitmind.data.database.entity.*
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

    // New Journal Enums
    @TypeConverter fun fromJournalMode(v: JournalMode) = v.name
    @TypeConverter fun toJournalMode(v: String) = JournalMode.valueOf(v)

    @TypeConverter fun fromSleepQuality(v: SleepQuality) = v.name
    @TypeConverter fun toSleepQuality(v: String) = SleepQuality.valueOf(v)

    @TypeConverter fun fromSocialBattery(v: SocialBattery) = v.name
    @TypeConverter fun toSocialBattery(v: String) = SocialBattery.valueOf(v)

    @TypeConverter fun fromDayPeriod(v: DayPeriod) = v.name
    @TypeConverter fun toDayPeriod(v: String) = DayPeriod.valueOf(v)

    @TypeConverter fun fromEnergyLevel(v: EnergyLevel) = v.name
    @TypeConverter fun toEnergyLevel(v: String) = EnergyLevel.valueOf(v)

    @TypeConverter fun fromMoodState(v: MoodState) = v.name
    @TypeConverter fun toMoodState(v: String) = MoodState.valueOf(v)

    @TypeConverter fun fromStressLevel(v: StressLevel) = v.name
    @TypeConverter fun toStressLevel(v: String) = StressLevel.valueOf(v)

    @TypeConverter fun fromLifeDomain(v: LifeDomain) = v.name
    @TypeConverter fun toLifeDomain(v: String) = LifeDomain.valueOf(v)

    @TypeConverter fun fromIdentityAlignment(v: IdentityAlignment) = v.name
    @TypeConverter fun toIdentityAlignment(v: String) = IdentityAlignment.valueOf(v)

    @TypeConverter fun fromDiagnosticCategory(v: DiagnosticCategory) = v.name
    @TypeConverter fun toDiagnosticCategory(v: String) = DiagnosticCategory.valueOf(v)
}
