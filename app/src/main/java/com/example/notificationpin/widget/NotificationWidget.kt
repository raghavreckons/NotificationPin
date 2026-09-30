package com.example.notificationpin.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.ButtonDefaults
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.notificationpin.MainActivity
import com.example.notificationpin.data.AppDatabase
import com.example.notificationpin.data.PinnedNotification
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val db = AppDatabase.getInstance(context)
        val pinnedList = db.pinnedNotificationDao().getAllPinned()

        provideContent {
            GlanceTheme {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(Color(0xFF1E1E2E))
                        .cornerRadius(16.dp)
                        .padding(12.dp)
                ) {
                    // Header Row
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📌 Pinned (${pinnedList.size})",
                            style = TextStyle(
                                color = ColorProvider(Color.White),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = GlanceModifier.defaultWeight()
                        )

                        Text(
                            text = "Open App",
                            style = TextStyle(
                                color = ColorProvider(Color(0xFF89B4FA)),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = GlanceModifier
                                .clickable(actionStartActivity<MainActivity>())
                                .padding(4.dp)
                        )
                    }

                    Spacer(modifier = GlanceModifier.height(8.dp))

                    if (pinnedList.isEmpty()) {
                        Box(
                            modifier = GlanceModifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No pinned notifications.\nOpen app to pin items.",
                                style = TextStyle(
                                    color = ColorProvider(Color(0xFFA6ADC8)),
                                    fontSize = 14.sp
                                )
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = GlanceModifier.fillMaxSize()
                        ) {
                            items(pinnedList) { item ->
                                PinnedItemView(item)
                                Spacer(modifier = GlanceModifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun PinnedItemView(item: PinnedNotification) {
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(item.postTime))

    Column(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(Color(0xFF313244))
            .cornerRadius(12.dp)
            .padding(10.dp)
    ) {
        // App name and time header
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.appName,
                style = TextStyle(
                    color = ColorProvider(Color(0xFFCBA6F7)),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = GlanceModifier.defaultWeight()
            )

            Text(
                text = formattedTime,
                style = TextStyle(
                    color = ColorProvider(Color(0xFFBAC2DE)),
                    fontSize = 11.sp
                )
            )
        }

        Spacer(modifier = GlanceModifier.height(4.dp))

        // Title
        if (item.title.isNotEmpty()) {
            Text(
                text = item.title,
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1
            )
            Spacer(modifier = GlanceModifier.height(2.dp))
        }

        // Content
        if (item.content.isNotEmpty()) {
            Text(
                text = item.content,
                style = TextStyle(
                    color = ColorProvider(Color(0xFFCDD6F4)),
                    fontSize = 12.sp
                ),
                maxLines = 3
            )
        }

        Spacer(modifier = GlanceModifier.height(6.dp))

        // Action Row
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                text = "Unpin ✕",
                onClick = actionRunCallback<UnpinActionCallback>(
                    actionParametersOf(PinnedIdParamKey to item.id)
                ),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = ColorProvider(Color(0xFFF38BA8)),
                    contentColor = ColorProvider(Color(0xFF11111B))
                ),
                modifier = GlanceModifier.height(28.dp)
            )
        }
    }
}
