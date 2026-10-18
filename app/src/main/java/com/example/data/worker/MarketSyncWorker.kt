package com.example.data.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.data.local.MarketPulseDatabase
import com.example.data.model.NotificationItem
import com.example.data.remote.RssIngestionManager
import com.example.ui.notifications.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class MarketSyncWorker(
  context: Context,
  params: WorkerParameters
) : CoroutineWorker(context, params) {

  companion object {
    const val WORK_NAME = "MarketPulseSyncWorker"

    fun schedulePeriodicSync(context: Context) {
      val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

      val syncRequest = PeriodicWorkRequestBuilder<MarketSyncWorker>(15, TimeUnit.MINUTES)
        .setConstraints(constraints)
        .build()

      WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        WORK_NAME,
        ExistingPeriodicWorkPolicy.KEEP,
        syncRequest
      )
    }
  }

  override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
    val database = MarketPulseDatabase.getDatabase(applicationContext)
    val dao = database.marketDao()
    val rssManager = RssIngestionManager()
    val notificationHelper = NotificationHelper(applicationContext)
    val timeFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())

    var newEventsCount = 0

    for (feed in rssManager.officialFeeds) {
      val result = rssManager.fetchFeed(feed)
      result.onSuccess { (events, health) ->
        dao.insertSourceHealth(listOf(health))
        if (events.isNotEmpty()) {
          dao.insertEvents(events)

          // Post notification for HIGH materiality events
          for (event in events) {
            if (event.materiality == "HIGH") {
              notificationHelper.sendEventNotification(event)

              // Record notification history item
              val notif = NotificationItem(
                id = "notif_${event.id}",
                eventId = event.id,
                title = "Verified Filing: ${event.entitySymbol}",
                body = event.title,
                category = event.category,
                timestamp = System.currentTimeMillis(),
                formattedTime = timeFormat.format(Date()),
                isRead = false,
                deepLinkUrl = event.sourceUrl
              )
              dao.insertNotifications(listOf(notif))
            }
          }
          newEventsCount += events.size
        }
      }.onFailure { err ->
        dao.updateSourceStatus(
          sourceId = feed.id,
          checkTime = System.currentTimeMillis(),
          refreshTime = System.currentTimeMillis(),
          status = "DEGRADED",
          error = err.message
        )
      }
    }

    Result.success()
  }
}
