package com.habitmind.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Primary daily journal record
 */
@Entity(
    tableName = "daily_journals",
    indices = [Index(value = ["date"], unique = true)]
)
data class DailyJournal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: LocalDate = LocalDate.now(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val mode: JournalMode = JournalMode.FULL,
    
    // Quantitative Stats
    val sleepHours: Float = 0f,
    val sleepQuality: SleepQuality = SleepQuality.GOOD,
    val workoutCompleted: Boolean = false,
    val workoutType: String = "",
    val workoutDurationMin: Int = 0,
    val deepWorkHours: Float = 0f,
    val screenTimeHours: Float = 0f,
    val steps: Long = 0L,
    val hydrationLiters: Float = 0f,
    
    // Operational State
    val energyLevel: EnergyLevel = EnergyLevel.STABLE,
    val socialBattery: SocialBattery = SocialBattery.MODERATE,
    val focusLevel: FocusLevel = FocusLevel.OKAY,
    
    // Highlights & Reflections
    val bestPart: String = "",
    val worstPart: String = "",
    val mentallyOccupiedBy: String = "",
    val brainDump: String = "",
    
    // Post-mortem
    val whatWentWell: String = "",
    val whatWentBadly: String = "",
    val whyItHappened: String = "",
    val patternNoticed: String = "",
    val whatIAvoided: String = "",
    
    // Growth
    val dailyLesson: String = "",
    val identityAlignment: IdentityAlignment = IdentityAlignment.MOSTLY,
    val isComplete: Boolean = false,
    
    // Sync Metadata
    override val remoteId: String? = null,
    override val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY,
    override val updatedAt: LocalDateTime = LocalDateTime.now()
) : SyncableEntity

/**
 * Morning / Afternoon / Evening snapshots
 */
@Entity(
    tableName = "journal_state_slices",
    primaryKeys = ["journalId", "period"],
    foreignKeys = [
        ForeignKey(
            entity = DailyJournal::class,
            parentColumns = ["id"],
            childColumns = ["journalId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class JournalStateSlice(
    val journalId: Long,
    val period: DayPeriod,
    val energy: EnergyLevel = EnergyLevel.NEUTRAL,
    val mood: MoodState = MoodState.GOOD,
    val stress: StressLevel = StressLevel.LOW,
    
    // Sync Metadata
    override val remoteId: String? = null,
    override val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY,
    override val updatedAt: LocalDateTime = LocalDateTime.now()
) : SyncableEntity

/**
 * Timeline bullets for the day
 */
@Entity(
    tableName = "journal_events",
    foreignKeys = [
        ForeignKey(
            entity = DailyJournal::class,
            parentColumns = ["id"],
            childColumns = ["journalId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("journalId")]
)
data class JournalEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val journalId: Long,
    val position: Int,
    val text: String,
    
    // Sync Metadata
    override val remoteId: String? = null,
    override val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY,
    override val updatedAt: LocalDateTime = LocalDateTime.now()
) : SyncableEntity

/**
 * Category-based behavior diagnostics
 */
@Entity(
    tableName = "behavior_diagnostics",
    foreignKeys = [
        ForeignKey(
            entity = DailyJournal::class,
            parentColumns = ["id"],
            childColumns = ["journalId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("journalId")]
)
data class BehaviorDiagnostic(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val journalId: Long,
    val category: DiagnosticCategory,
    val key: String,
    val isChecked: Boolean = false,
    
    // Sync Metadata
    override val remoteId: String? = null,
    override val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY,
    override val updatedAt: LocalDateTime = LocalDateTime.now()
) : SyncableEntity

/**
 * Problem -> Fix pairs
 */
@Entity(
    tableName = "action_fixes",
    foreignKeys = [
        ForeignKey(
            entity = DailyJournal::class,
            parentColumns = ["id"],
            childColumns = ["journalId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("journalId")]
)
data class ActionFix(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val journalId: Long,
    val problem: String,
    val fix: String,
    val position: Int,
    
    // Sync Metadata
    override val remoteId: String? = null,
    override val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY,
    override val updatedAt: LocalDateTime = LocalDateTime.now()
) : SyncableEntity

/**
 * Top 3 priorities for tomorrow
 */
@Entity(
    tableName = "tomorrow_priorities",
    foreignKeys = [
        ForeignKey(
            entity = DailyJournal::class,
            parentColumns = ["id"],
            childColumns = ["journalId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("journalId")]
)
data class TomorrowPriority(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val journalId: Long,
    val slot: Int, // 1, 2, or 3
    val text: String,
    
    // Sync Metadata
    override val remoteId: String? = null,
    override val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY,
    override val updatedAt: LocalDateTime = LocalDateTime.now()
) : SyncableEntity

/**
 * Reusable domain check-in record
 */
@Entity(
    tableName = "domain_checkins",
    indices = [Index(value = ["date", "domain"], unique = true)]
)
data class DomainCheckin(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: LocalDate = LocalDate.now(),
    val domain: LifeDomain,
    val promptAnswer: String = "",
    val summaryValue: String = "",
    val notes: String = "",
    
    // Sync Metadata
    override val remoteId: String? = null,
    override val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY,
    override val updatedAt: LocalDateTime = LocalDateTime.now()
) : SyncableEntity

/**
 * Tags or flags attached to a domain check-in
 */
@Entity(
    tableName = "domain_flags",
    foreignKeys = [
        ForeignKey(
            entity = DomainCheckin::class,
            parentColumns = ["id"],
            childColumns = ["checkinId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("checkinId")]
)
data class DomainFlag(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val checkinId: Long,
    val key: String,
    val isChecked: Boolean = false,
    
    // Sync Metadata
    override val remoteId: String? = null,
    override val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY,
    override val updatedAt: LocalDateTime = LocalDateTime.now()
) : SyncableEntity
