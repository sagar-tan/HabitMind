package com.habitmind.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.habitmind.data.database.entity.ActionFix
import com.habitmind.data.database.entity.BehaviorDiagnostic
import com.habitmind.data.database.entity.DailyJournal
import com.habitmind.data.database.entity.DomainCheckin
import com.habitmind.data.database.entity.DomainFlag
import com.habitmind.data.database.entity.JournalEvent
import com.habitmind.data.database.entity.JournalStateSlice
import com.habitmind.data.database.entity.LifeDomain
import com.habitmind.data.database.entity.TomorrowPriority

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface DailyJournalDao {

    // --- DailyJournal ---
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournal(journal: DailyJournal): Long

    @Update
    suspend fun updateJournal(journal: DailyJournal): Int

    @Query("SELECT * FROM daily_journals WHERE date = :date")
    suspend fun getJournalByDate(date: LocalDate): DailyJournal?

    @Query("SELECT * FROM daily_journals WHERE date = :date")
    fun getJournalFlowByDate(date: LocalDate): Flow<DailyJournal?>

    @Query("SELECT * FROM daily_journals ORDER BY date DESC")
    fun getAllJournalsFlow(): Flow<List<DailyJournal>>

    // --- State Slices ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStateSlice(slice: JournalStateSlice): Long

    @Query("SELECT * FROM journal_state_slices WHERE journalId = :journalId")
    fun getStateSlicesForJournal(journalId: Long): Flow<List<JournalStateSlice>>

    // --- Journal Events ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: JournalEvent): Long

    @Query("SELECT * FROM journal_events WHERE journalId = :journalId ORDER BY position ASC")
    fun getEventsForJournal(journalId: Long): Flow<List<JournalEvent>>

    @Delete
    suspend fun deleteEvent(event: JournalEvent): Int

    // --- Behavior Diagnostics ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiagnostic(diagnostic: BehaviorDiagnostic): Long

    @Query("SELECT * FROM behavior_diagnostics WHERE journalId = :journalId")
    fun getDiagnosticsForJournal(journalId: Long): Flow<List<BehaviorDiagnostic>>

    // --- Action Fixes ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActionFix(fix: ActionFix): Long

    @Query("SELECT * FROM action_fixes WHERE journalId = :journalId ORDER BY position ASC")
    fun getActionFixesForJournal(journalId: Long): Flow<List<ActionFix>>

    @Delete
    suspend fun deleteActionFix(fix: ActionFix): Int

    // --- Tomorrow Priorities ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPriority(priority: TomorrowPriority): Long

    @Query("SELECT * FROM tomorrow_priorities WHERE journalId = :journalId ORDER BY slot ASC")
    fun getPrioritiesForJournal(journalId: Long): Flow<List<TomorrowPriority>>

    // --- Domain Check-ins ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDomainCheckin(checkin: DomainCheckin): Long

    @Query("SELECT * FROM domain_checkins WHERE date = :date AND domain = :domain")
    suspend fun getDomainCheckin(date: LocalDate, domain: LifeDomain): DomainCheckin?

    @Query("SELECT * FROM domain_checkins WHERE date = :date")
    fun getDomainCheckinsForDate(date: LocalDate): Flow<List<DomainCheckin>>

    // --- Domain Flags ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDomainFlag(flag: DomainFlag): Long

    @Query("SELECT * FROM domain_flags WHERE checkinId = :checkinId")
    fun getFlagsForCheckin(checkinId: Long): Flow<List<DomainFlag>>
}
