package com.example.notificationpin.ui

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notificationpin.data.PinnedNotification
import com.example.notificationpin.service.ActiveNotificationItem
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: NotificationViewModel) {
    val context = LocalContext.current
    val isConnected by viewModel.isServiceConnected.collectAsState()
    val activeList by viewModel.activeNotifications.collectAsState()
    val pinnedList by viewModel.pinnedNotifications.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val pinnedKeys = remember(pinnedList) { pinnedList.map { it.notificationKey }.toSet() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Notification Pin",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isConnected) "● Listener Active" else "○ Listener Inactive",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isConnected) Color(0xFF4CAF50) else Color(0xFFFF5722)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshActiveNotifications() }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Permission Banner if listener is not enabled
            if (!isConnected) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Notification Access Required",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "To allow manual pinning of status bar notifications, please grant Notification Listener access in System Settings.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                                context.startActivity(intent)
                            }
                        ) {
                            Text("Open Notification Access Settings")
                        }
                    }
                }
            }

            // Tabs
            TabRow(selectedTabIndex = selectedTabIndex) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Active (${activeList.size})") },
                    icon = { Icon(Icons.Default.Notifications, contentDescription = null) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Pinned (${pinnedList.size})") },
                    icon = { Icon(Icons.Default.Star, contentDescription = null) }
                )
            }

            when (selectedTabIndex) {
                0 -> ActiveNotificationsTab(
                    activeList = activeList,
                    pinnedKeys = pinnedKeys,
                    onPin = { item -> viewModel.pinNotification(item) },
                    onUnpin = { key -> viewModel.unpinByNotificationKey(key) }
                )
                1 -> PinnedNotificationsTab(
                    pinnedList = pinnedList,
                    onUnpin = { id -> viewModel.unpinNotification(id) },
                    onClearAll = { viewModel.clearAllPinned() }
                )
            }
        }
    }
}

@Composable
fun ActiveNotificationsTab(
    activeList: List<ActiveNotificationItem>,
    pinnedKeys: Set<String>,
    onPin: (ActiveNotificationItem) -> Unit,
    onUnpin: (String) -> Unit
) {
    if (activeList.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No active notifications found in status bar.\nWhen apps post notifications, they will appear here.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(activeList, key = { it.key }) { item ->
                val isPinned = pinnedKeys.contains(item.key)
                ActiveNotificationCard(
                    item = item,
                    isPinned = isPinned,
                    onPinClick = {
                        if (isPinned) onUnpin(item.key) else onPin(item)
                    }
                )
            }
        }
    }
}

@Composable
fun ActiveNotificationCard(
    item: ActiveNotificationItem,
    isPinned: Boolean,
    onPinClick: () -> Unit
) {
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(item.postTime))

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = item.appName,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (item.title.isNotEmpty()) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
            }

            if (item.text.isNotEmpty()) {
                Text(
                    text = item.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onPinClick,
                    colors = if (isPinned) {
                        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                    } else {
                        ButtonDefaults.buttonColors()
                    }
                ) {
                    Text(if (isPinned) "✓ Pinned to Widget" else "📌 Pin to Widget")
                }
            }
        }
    }
}

@Composable
fun PinnedNotificationsTab(
    pinnedList: List<PinnedNotification>,
    onUnpin: (Long) -> Unit,
    onClearAll: () -> Unit
) {
    val context = LocalContext.current

    if (pinnedList.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No pinned notifications.\nPin any active notification to display it on your Home Screen widget.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${pinnedList.size} item(s) on Home Screen widget",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = onClearAll) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear All")
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(pinnedList, key = { it.id }) { item ->
                    PinnedNotificationCard(
                        item = item,
                        onUnpin = { onUnpin(item.id) },
                        onOpenApp = {
                            val launchIntent = context.packageManager.getLaunchIntentForPackage(item.packageName)
                            if (launchIntent != null) {
                                context.startActivity(launchIntent)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PinnedNotificationCard(
    item: PinnedNotification,
    onUnpin: () -> Unit,
    onOpenApp: () -> Unit
) {
    val timeFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(item.postTime))

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = item.appName,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (item.title.isNotEmpty()) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
            }

            if (item.content.isNotEmpty()) {
                Text(
                    text = item.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = onOpenApp) {
                    Text("Open App")
                }

                FilledTonalButton(
                    onClick = onUnpin,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Text("Unpin ✕")
                }
            }
        }
    }
}
