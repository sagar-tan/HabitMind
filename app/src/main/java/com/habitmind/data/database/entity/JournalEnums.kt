package com.habitmind.data.database.entity

/**
 * Modes for the daily journal entry
 */
enum class JournalMode {
    FULL,
    MINIMUM
}

/**
 * Qualitative sleep assessment
 */
enum class SleepQuality {
    POOR,
    FAIR,
    GOOD,
    EXCELLENT
}

/**
 * Social energy status
 */
enum class SocialBattery {
    DRAINED,
    LOW,
    MODERATE,
    FULL
}

/**
 * Time periods within a day
 */
enum class DayPeriod {
    MORNING,
    AFTERNOON,
    EVENING
}

/**
 * Energy levels for state tracking
 */
enum class EnergyLevel {
    VERY_LOW,
    LOW,
    NEUTRAL,
    HIGH,
    VERY_HIGH
}

/**
 * Mood states for state tracking
 */
enum class MoodState {
    TERRIBLE,
    MEH,
    GOOD,
    GREAT,
    AMAZING
}

/**
 * Stress levels for state tracking
 */
enum class StressLevel {
    RELAXED,
    LOW,
    MODERATE,
    HIGH,
    OVERWHELMED
}

/**
 * Core life domains for structured check-ins
 */
enum class LifeDomain {
    PHYSICAL,
    FOCUS,
    MINDFULNESS,
    LEARNING,
    WORK,
    SOCIAL,
    IDENTITY
}

/**
 * Degree of alignment with ideal identity
 */
enum class IdentityAlignment {
    NONE,
    PARTIAL,
    MOSTLY,
    FULLY
}

/**
 * Categories for behavior diagnostics
 */
enum class DiagnosticCategory {
    COGNITIVE,
    EMOTIONAL,
    BEHAVIORAL,
    ENERGY
}
