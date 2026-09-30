package com.example.notificationpin.data

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.example.notificationpin.widget.NotificationWidget
import kotlinx.coroutines.flow.Flow

class NotificationRepository(
    private val dao: PinnedNotificationDao,
    private val context: Context
) {
    val pinnedNotificationsFlow: Flow<List<PinnedNotification>> = dao.getAllPinnedFlow()

    suspend fun getAllPinned(): List<PinnedNotification> = dao.getAllPinned()

    suspend fun pinNotification(notification: PinnedNotification): Long {
        val id = dao.insert(notification)
        refreshWidget()
        return id
    }

    suspend fun unpinNotification(id: Long) {
        dao.deleteById(id)
        refreshWidget()
    }

    suspend fun unpinByNotificationKey(key: String) {
        dao.deleteByNotificationKey(key)
        refreshWidget()
    }

    suspend fun clearAll() {
        dao.deleteAll()
        refreshWidget()
    }

    suspend fun isPinned(key: String): Boolean {
        return dao.isPinned(key)
    }

    private suspend fun refreshWidget() {
        try {
            NotificationWidget().updateAll(context)
        } catch (_: Exception) {
            // Widget might not be placed on home screen yet
        }
    }
}
