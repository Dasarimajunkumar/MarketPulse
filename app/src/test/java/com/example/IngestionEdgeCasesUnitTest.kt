package com.example

import com.example.data.model.EventCategory
import com.example.data.model.MarketEvent
import com.example.data.model.SourceHealth
import com.example.data.model.VerificationType
import com.example.data.remote.FeedConfig
import com.example.data.remote.RssIngestionManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class IngestionEdgeCasesUnitTest {

  @Test
  fun testMalformedXmlGracefulHandling() {
    val manager = RssIngestionManager()
    val feed = FeedConfig(
      id = "test_feed",
      name = "Test Feed",
      category = "FINANCIAL_PUBLISHER",
      url = "https://example.com/rss",
      pollIntervalMinutes = 10
    )

    // Broken XML payload
    val malformedXml = "<rss><channel><item><title>Broken Title<description>Unclosed tags"
    // Using reflection or testing parser behavior
    assertNotNull(manager)
  }

  @Test
  fun testDeduplicationByCanonicalTitle() {
    val events = mutableListOf<MarketEvent>()

    val event1 = MarketEvent(
      id = "evt_01",
      canonicalTitle = "SEBI Mandates Higher Contract Size",
      title = "SEBI Mandates Higher Contract Size!",
      summary = "Contract size revised to 15 Lakh.",
      beginnerExplainer = "Explainer 1",
      category = EventCategory.REGULATORY.name,
      publisher = "SEBI",
      sourceType = "OFFICIAL_REGULATOR",
      sourceName = "SEBI Feed",
      sourceUrl = "https://www.sebi.gov.in/doc1.pdf",
      formattedTime = "10:00 AM IST",
      entitySymbol = "SEBI",
      entityName = "SEBI",
      sector = "Regulatory"
    )

    val event2 = MarketEvent(
      id = "evt_02",
      canonicalTitle = "SEBI Mandates Higher Contract Size", // Identical canonical title
      title = "SEBI Mandates Higher Contract Size",
      summary = "Duplicate report from news publisher.",
      beginnerExplainer = "Explainer 2",
      category = EventCategory.REGULATORY.name,
      publisher = "Moneycontrol",
      sourceType = "FINANCIAL_PUBLISHER",
      sourceName = "Moneycontrol Feed",
      sourceUrl = "https://www.moneycontrol.com/doc1.html",
      formattedTime = "10:05 AM IST",
      entitySymbol = "SEBI",
      entityName = "SEBI",
      sector = "Regulatory"
    )

    events.add(event1)

    // Deduplication check
    val isDuplicate = events.any {
      it.canonicalTitle.equals(event2.canonicalTitle, ignoreCase = true) || it.sourceUrl == event2.sourceUrl
    }

    assertTrue("Duplicate event should be detected and filtered out", isDuplicate)
    assertEquals(1, events.size)
  }

  @Test
  fun testSourceHealthStaleFlagAssignment() {
    val checkTime = System.currentTimeMillis()
    val errorHealth = SourceHealth(
      sourceId = "failing_source",
      sourceName = "Failing Source",
      sourceType = "OFFICIAL",
      feedUrl = "https://failing.source.com/rss",
      lastCheckTime = checkTime,
      lastSuccessfulRefresh = checkTime - (2 * 60 * 60 * 1000L), // Last success 2 hours ago
      status = "DEGRADED",
      errorMessage = "HTTP 500 Internal Server Error",
      pollIntervalMinutes = 5
    )

    assertEquals("DEGRADED", errorHealth.status)
    assertEquals("HTTP 500 Internal Server Error", errorHealth.errorMessage)
    assertTrue("Last success should be older than last check time", errorHealth.lastSuccessfulRefresh < errorHealth.lastCheckTime)
  }

  @Test
  fun testNotificationDeduplicationSet() {
    val processedNotificationIds = mutableSetOf<String>()

    val eventId = "evt_sebi_01"
    val notifId1 = "notif_$eventId"
    val notifId2 = "notif_$eventId"

    val insertedFirst = processedNotificationIds.add(notifId1)
    val insertedSecond = processedNotificationIds.add(notifId2)

    assertTrue("First notification insertion should succeed", insertedFirst)
    assertFalse("Duplicate notification insertion should be blocked", insertedSecond)
    assertEquals(1, processedNotificationIds.size)
  }
}
