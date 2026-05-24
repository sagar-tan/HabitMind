package com.habitmind.data.database.entity

import java.time.LocalDateTime

/**
 * Enum representing the synchronization state of a local record with the cloud.
 */
enum class SyncStatus {
    /** Data is purely local and has never been synced (or user is not logged in). */
    LOCAL_ONLY,
    
    /** Data has been modified locally and is waiting to be pushed to the cloud. */
    PENDING,
    
    /** Data is successfully synchronized with the cloud. */
    SYNCED
}

/**
 * Interface for entities that support synchronization.
 */
interface SyncableEntity {
    val remoteId: String?
    val syncStatus: SyncStatus
    val updatedAt: LocalDateTime
}
