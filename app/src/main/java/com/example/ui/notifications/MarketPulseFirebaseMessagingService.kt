package com.example.ui.notifications

import android.util.Log
import com.example.data.local.MarketPulseDatabase
import com.example.data.model.MarketEvent
import com.example.data.model.NotificationItem
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class MarketPulseFirebaseMessagingService : FirebaseMessagingService() {

  companion object {
    private const val TAG = "MarketPulseFCM"
  }

  @Suppress("OVERRIDE_DEPRECATION", "DEPRECATION")
  override fun onNewToken(token: String) {
    super.onNewToken(token)
    Log.d(TAG, "New FCM registration token received: $token")
    registerTokenWithBackend(token)
  }

  override fun onMessageReceived(remoteMessage: RemoteMessage) {
    super.onMessageReceived(remoteMessage)
    Log.d(TAG, "FCM Push Message received from: ${remoteMessage.from}")

    val notificationHelper = NotificationHelper(applicationContext)
    val timeFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())

    val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "MarketPulse Intelligence Alert"
    val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: "New official filing ingested."
    val category = remoteMessage.data["category"] ?: "REGULATORY"
    val eventId = remoteMessage.data["eventId"] ?: "evt_push_${UUID.randomUUID().toString().take(8)}"
    val sourceUrl = remoteMessage.data["sourceUrl"] ?: "https://www.sebi.gov.in"

    val mockEvent = MarketEvent(
      id = eventId,
      canonicalTitle = title,
      title = title,
      summary = body,
      beginnerExplainer = body,
      category = category,
      publisher = "FCM Push Dispatched",
      sourceType = "OFFICIAL_REGULATOR",
      sourceName = "Official Regulator",
      sourceUrl = sourceUrl,
      entitySymbol = "ALERT",
      entityName = "MarketPulse Notification",
      sector = "Push Alert",
      formattedTime = timeFormat.format(Date())
    )

    // Trigger local status bar notification
    notificationHelper.sendEventNotification(mockEvent)

    // Persist notification item in Room database
    CoroutineScope(Dispatchers.IO).launch {
      try {
        val database = MarketPulseDatabase.getDatabase(applicationContext)
        val notifItem = NotificationItem(
          id = "notif_$eventId",
          eventId = eventId,
          title = title,
          body = body,
          category = category,
          timestamp = System.currentTimeMillis(),
          formattedTime = timeFormat.format(Date()),
          isRead = false,
          deepLinkUrl = sourceUrl
        )
        database.marketDao().insertNotifications(listOf(notifItem))
      } catch (e: Exception) {
        Log.e(TAG, "Failed to persist FCM notification to Room", e)
      }
    }
  }

  private fun registerTokenWithBackend(token: String) {
    CoroutineScope(Dispatchers.IO).launch {
      try {
        // Send token to backend endpoint
        Log.i(TAG, "FCM Token registered locally: $token")
      } catch (e: Exception) {
        Log.e(TAG, "Failed to register FCM token with backend", e)
      }
    }
  }
}
