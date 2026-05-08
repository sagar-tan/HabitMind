package com.habitmind.data.repository

import com.habitmind.data.database.dao.DailyJournalDao
import com.habitmind.data.database.entity.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Repository for Daily Journal operations
 */
class DailyJournalRepository(private val dailyJournalDao: DailyJournalDao) {

    // --- DailyJournal ---

    fun getJournalFlowByDate(date: LocalDate): Flow<DailyJournal?> {
        return dailyJournalDao.getJournalFlowByDate(date)
    }

    suspend fun getJournalByDate(date: LocalDate): DailyJournal? {
        return dailyJournalDao.getJournalByDate(date)
    }

    fun getAllJournalsFlow(): Flow<List<DailyJournal>> {
        return dailyJournalDao.getAllJournalsFlow()
    }

    suspend fun saveJournal(journal: DailyJournal): Long {
        return if (journal.id == 0L) {
            dailyJournalDao.insertJournal(journal)
        } else {
            dailyJournalDao.updateJournal(journal)
            journal.id
        }
    }

    // --- State Slices ---

    fun getStateSlicesForJournal(journalId: Long): Flow<List<JournalStateSlice>> {
        return dailyJournalDao.getStateSlicesForJournal(journalId)
    }

    suspend fun saveStateSlice(slice: JournalStateSlice) {
        dailyJournalDao.insertStateSlice(slice)
    }

    // --- Journal Events ---

    fun getEventsForJournal(journalId: Long): Flow<List<JournalEvent>> {
        return dailyJournalDao.getEventsForJournal(journalId)
    }

    suspend fun saveEvent(event: JournalEvent) {
        dailyJournalDao.insertEvent(event)
    }

    suspend fun deleteEvent(event: JournalEvent) {
        dailyJournalDao.deleteEvent(event)
    }

    // --- Behavior Diagnostics ---

    fun getDiagnosticsForJournal(journalId: Long): Flow<List<BehaviorDiagnostic>> {
        return dailyJournalDao.getDiagnosticsForJournal(journalId)
    }

    suspend fun saveDiagnostic(diagnostic: BehaviorDiagnostic) {
        dailyJournalDao.insertDiagnostic(diagnostic)
    }

    // --- Action Fixes ---

    fun getActionFixesForJournal(journalId: Long): Flow<List<ActionFix>> {
        return dailyJournalDao.getActionFixesForJournal(journalId)
    }

    suspend fun saveActionFix(fix: ActionFix) {
        dailyJournalDao.insertActionFix(fix)
    }

    suspend fun deleteActionFix(fix: ActionFix) {
        dailyJournalDao.deleteActionFix(fix)
    }

    // --- Tomorrow Priorities ---

    fun getPrioritiesForJournal(journalId: Long): Flow<List<TomorrowPriority>> {
        return dailyJournalDao.getPrioritiesForJournal(journalId)
    }

    suspend fun savePriority(priority: TomorrowPriority) {
        dailyJournalDao.insertPriority(priority)
    }

    // --- Domain Check-ins ---

    suspend fun getDomainCheckin(date: LocalDate, domain: LifeDomain): DomainCheckin? {
        return dailyJournalDao.getDomainCheckin(date, domain)
    }

    fun getDomainCheckinsForDate(date: LocalDate): Flow<List<DomainCheckin>> {
        return dailyJournalDao.getDomainCheckinsForDate(date)
    }

    suspend fun saveDomainCheckin(checkin: DomainCheckin): Long {
        return dailyJournalDao.insertDomainCheckin(checkin)
    }

    // --- Domain Flags ---

    fun getFlagsForCheckin(checkinId: Long): Flow<List<DomainFlag>> {
        return dailyJournalDao.getFlagsForCheckin(checkinId)
    }

    suspend fun saveDomainFlag(flag: DomainFlag) {
        dailyJournalDao.insertDomainFlag(flag)
    }
}
