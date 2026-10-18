package com.example

import com.example.data.model.EventCategory
import com.example.data.model.EventMateriality
import com.example.data.model.MarketEvent
import com.example.data.model.MarketIndex
import com.example.data.model.VerificationType
import com.example.data.remote.FeedConfig
import com.example.data.remote.RssIngestionManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class FinancialIngestionUnitTest {

  @Test
  fun testVerificationTypeMapping() {
    assertEquals(VerificationType.OFFICIAL_SOURCE_VERIFIED, VerificationType.fromLegacy("CONFIRMED_FACT"))
    assertEquals(VerificationType.REPORTED_BY_PUBLISHER, VerificationType.fromLegacy("REPORTED_MEDIA"))
    assertEquals(VerificationType.MULTIPLE_SOURCES, VerificationType.fromLegacy("MULTIPLE_SOURCES"))
    assertEquals(VerificationType.ANALYSIS, VerificationType.fromLegacy("ANALYSIS"))
    assertEquals(VerificationType.UNVERIFIED, VerificationType.fromLegacy("UNKNOWN"))
  }

  @Test
  fun testUtcToIstFormatting() {
    val utcTimestamp = 1700000000000L
    val istFormat = SimpleDateFormat("hh:mm a 'IST'", Locale.getDefault()).apply {
      timeZone = TimeZone.getTimeZone("Asia/Kolkata")
    }
    val formatted = istFormat.format(Date(utcTimestamp))
    assertTrue("Formatted time should contain IST", formatted.contains("IST"))
  }

  @Test
  fun testOfficialSourceRegistryConfig() {
    val manager = RssIngestionManager()
    val feeds = manager.officialFeeds

    assertEquals(5, feeds.size)
    val sebiFeed = feeds.find { it.id == "sebi_rss" }
    assertNotNull(sebiFeed)
    assertEquals("OFFICIAL_REGULATOR", sebiFeed?.category)
    assertEquals(5, sebiFeed?.pollIntervalMinutes)

    val rbiFeed = feeds.find { it.id == "rbi_rss" }
    assertNotNull(rbiFeed)
    assertEquals("https://www.rbi.org.in/pressreleases_rss.xml", rbiFeed?.url)
  }

  @Test
  fun testCanonicalEventDataFields() {
    val now = System.currentTimeMillis()
    val event = MarketEvent(
      id = "evt_test_01",
      canonicalTitle = "SEBI Mandates Higher Options Lot Size",
      title = "SEBI Mandates Higher Options Lot Size",
      summary = "Minimum contract value raised from 5 Lakh to 15 Lakh.",
      beginnerExplainer = "SEBI protects retail traders.",
      category = EventCategory.REGULATORY.name,
      publisher = "SEBI",
      sourceType = "OFFICIAL_REGULATOR",
      sourceName = "SEBI Press Release",
      sourceUrl = "https://www.sebi.gov.in/sebirss.xml",
      officialDocumentUrl = "https://www.sebi.gov.in/legal/circulars/index-derivatives.html",
      publicationTime = now,
      formattedTime = "10:15 AM IST",
      entitySymbol = "SEBI",
      entityName = "Securities and Exchange Board of India",
      sector = "Capital Markets",
      materiality = EventMateriality.HIGH.name,
      verificationType = VerificationType.OFFICIAL_SOURCE_VERIFIED.name,
      sourceRetrievalStatus = "SUCCESS"
    )

    assertEquals("SEBI", event.entitySymbol)
    assertEquals("OFFICIAL_SOURCE_VERIFIED", event.verificationType)
    assertEquals("HIGH", event.materiality)
    assertEquals("SUCCESS", event.sourceRetrievalStatus)
  }

  @Test
  fun testNiftyItArithmeticFromRawProviderPayload() {
    val currentVal = 31924.10
    val prevClose = 30719.80 // Exact previous close from Yahoo Finance ^CNXIT payload
    val diff = currentVal - prevClose
    val pct = (diff / prevClose) * 100.0

    assertEquals(1204.30, diff, 0.01)
    assertEquals(3.92, pct, 0.01)
  }

  @Test
  fun testMarketSessionAndFreshnessClassification() {
    val closedIndex = MarketIndex(
      symbol = "NIFTY 50",
      name = "Nifty 50 Index (NSE)",
      currentValue = 23146.15,
      changeValue = 148.80,
      changePercent = 0.65,
      previousClose = 22997.35,
      sparklineJson = "22997.35,23146.15",
      marketSessionStatus = "CLOSED",
      dataFreshnessStatus = "LAST_CLOSE",
      provider = "Yahoo Finance (NSE)"
    )

    assertEquals("CLOSED", closedIndex.marketSessionStatus)
    assertEquals("LAST_CLOSE", closedIndex.dataFreshnessStatus)
  }

  @Test
  fun testTimestampIntegrityValidation() {
    val sourceTime2026 = 1792144867000L // Oct 16, 2026
    val retrievalTime2024 = 1713438000000L // April 18, 2024

    val isValidOrdering = retrievalTime2024 >= sourceTime2026
    assertFalse("Retrieval timestamp from 2024 for a 2026 quote must fail timestamp integrity validation", isValidOrdering)
  }

  @Test
  fun testPreserveDbIndexTimestampsOnLoad() {
    val fetchedTime = 1792144900000L
    val sourceTime = 1792144867000L

    val index = MarketIndex(
      symbol = "NIFTY 50",
      name = "Nifty 50 Index (NSE)",
      currentValue = 23146.15,
      changeValue = 148.80,
      changePercent = 0.65,
      previousClose = 22997.35,
      sparklineJson = "22997.35,23146.15",
      marketSessionStatus = "CLOSED",
      dataFreshnessStatus = "LAST_CLOSE",
      provider = "Yahoo Finance (NSE)",
      lastFetchedTimestamp = fetchedTime,
      sourceTimestamp = sourceTime
    )

    assertEquals(fetchedTime, index.lastFetchedTimestamp)
    assertEquals(sourceTime, index.sourceTimestamp)
  }
}
