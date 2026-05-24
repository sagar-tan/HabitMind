package com.habitmind.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * Entity to track deletions that need to be synced to the cloud.
 */
@Entity(tableName = "sync_deletions")
data class SyncDeletion(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tableName: String,
    val remoteId: String,
    val deletedAt: LocalDateTime = LocalDateTime.now()
)
