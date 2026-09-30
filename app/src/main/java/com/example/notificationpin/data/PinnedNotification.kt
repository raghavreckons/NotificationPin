package com.example.notificationpin.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pinned_notifications")
data class PinnedNotification(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val notificationKey: String,
    val packageName: String,
    val appName: String,
    val title: String,
    val content: String,
    val postTime: Long,
    val pinnedAt: Long = System.currentTimeMillis(),
    val customNote: String? = null
)
