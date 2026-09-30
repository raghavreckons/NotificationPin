package com.example.notificationpin.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.notificationpin.data.NotificationRepository
import com.example.notificationpin.data.PinnedNotification
import com.example.notificationpin.service.ActiveNotificationItem
import com.example.notificationpin.service.NotificationMonitorService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val repository: NotificationRepository
) : ViewModel() {

    val isServiceConnected: StateFlow<Boolean> = NotificationMonitorService.isServiceConnected

    val activeNotifications: StateFlow<List<ActiveNotificationItem>> =
        NotificationMonitorService.activeNotificationsFlow

    val pinnedNotifications: StateFlow<List<PinnedNotification>> =
        repository.pinnedNotificationsFlow
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    fun pinNotification(item: ActiveNotificationItem, customNote: String? = null) {
        viewModelScope.launch {
            val entity = PinnedNotification(
                notificationKey = item.key,
                packageName = item.packageName,
                appName = item.appName,
                title = item.title,
                content = item.text,
                postTime = item.postTime,
                customNote = customNote
            )
            repository.pinNotification(entity)
        }
    }

    fun unpinNotification(id: Long) {
        viewModelScope.launch {
            repository.unpinNotification(id)
        }
    }

    fun unpinByNotificationKey(key: String) {
        viewModelScope.launch {
            repository.unpinByNotificationKey(key)
        }
    }

    fun clearAllPinned() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    fun refreshActiveNotifications() {
        NotificationMonitorService.instance?.refreshActiveNotifications()
    }
}

class NotificationViewModelFactory(
    private val repository: NotificationRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationViewModel::class.java)) {
            return NotificationViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
