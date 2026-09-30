package com.example.notificationpin.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PinnedNotificationDao {
    @Query("SELECT * FROM pinned_notifications ORDER BY pinnedAt DESC")
    fun getAllPinnedFlow(): Flow<List<PinnedNotification>>

    @Query("SELECT * FROM pinned_notifications ORDER BY pinnedAt DESC")
    suspend fun getAllPinned(): List<PinnedNotification>

    @Query("SELECT * FROM pinned_notifications WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): PinnedNotification?

    @Query("SELECT EXISTS(SELECT 1 FROM pinned_notifications WHERE notificationKey = :key)")
    suspend fun isPinned(key: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: PinnedNotification): Long

    @Query("DELETE FROM pinned_notifications WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM pinned_notifications WHERE notificationKey = :key")
    suspend fun deleteByNotificationKey(key: String)

    @Query("DELETE FROM pinned_notifications")
    suspend fun deleteAll()
}
