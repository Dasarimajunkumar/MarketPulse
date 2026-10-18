package com.example.ui.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.model.MarketEvent

class NotificationHelper(private val context: Context) {

  companion object {
    const val CHANNEL_ID_REGULATORY = "marketpulse_regulatory_channel"
    const val CHANNEL_NAME_REGULATORY = "SEBI & RBI Regulatory Alerts"

    const val CHANNEL_ID_DISCLOSURES = "marketpulse_disclosures_channel"
    const val CHANNEL_NAME_DISCLOSURES = "Corporate Filings & Earnings"
  }

  init {
    createNotificationChannels()
  }

  private fun createNotificationChannels() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

      val regulatoryChannel = NotificationChannel(
        CHANNEL_ID_REGULATORY,
        CHANNEL_NAME_REGULATORY,
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = "Instant notifications for official SEBI and RBI regulatory notifications."
      }

      val disclosuresChannel = NotificationChannel(
        CHANNEL_ID_DISCLOSURES,
        CHANNEL_NAME_DISCLOSURES,
        NotificationManager.IMPORTANCE_DEFAULT
      ).apply {
        description = "High materiality corporate disclosures, earnings, and order wins."
      }

      notificationManager.createNotificationChannel(regulatoryChannel)
      notificationManager.createNotificationChannel(disclosuresChannel)
    }
  }

  fun sendEventNotification(event: MarketEvent) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val intent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
      putExtra("event_id", event.id)
    }

    val pendingIntent = PendingIntent.getActivity(
      context,
      event.id.hashCode(),
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val channelId = if (event.category == "REGULATORY" || event.publisher.contains("SEBI") || event.publisher.contains("RBI")) {
      CHANNEL_ID_REGULATORY
    } else {
      CHANNEL_ID_DISCLOSURES
    }

    val notification = NotificationCompat.Builder(context, channelId)
      .setSmallIcon(android.R.drawable.ic_dialog_info)
      .setContentTitle("MarketPulse Alert: ${event.entitySymbol}")
      .setContentText(event.title)
      .setStyle(NotificationCompat.BigTextStyle().bigText("${event.title}\n\n${event.summary}"))
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setContentIntent(pendingIntent)
      .setAutoCancel(true)
      .setSubText(event.publisher)
      .build()

    notificationManager.notify(event.id.hashCode(), notification)
  }
}
