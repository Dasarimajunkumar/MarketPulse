package com.example.data.remote

import android.util.Xml
import com.example.data.model.EventCategory
import com.example.data.model.EventMateriality
import com.example.data.model.MarketEvent
import com.example.data.model.SourceHealth
import com.example.data.model.VerificationType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

data class FeedConfig(
  val id: String,
  val name: String,
  val category: String, // "OFFICIAL_REGULATOR", "STOCK_EXCHANGE", "FINANCIAL_PUBLISHER"
  val url: String,
  val pollIntervalMinutes: Int
)

class RssIngestionManager {

  private val client = OkHttpClient.Builder()
    .connectTimeout(10, TimeUnit.SECONDS)
    .readTimeout(15, TimeUnit.SECONDS)
    .build()

  val officialFeeds = listOf(
    FeedConfig(
      id = "sebi_rss",
      name = "SEBI Official Circulars",
      category = "OFFICIAL_REGULATOR",
      url = "https://www.sebi.gov.in/sebirss.xml",
      pollIntervalMinutes = 5
    ),
    FeedConfig(
      id = "rbi_rss",
      name = "RBI Monetary Policy & Circulars",
      category = "OFFICIAL_REGULATOR",
      url = "https://www.rbi.org.in/pressreleases_rss.xml",
      pollIntervalMinutes = 5
    ),
    FeedConfig(
      id = "mc_rss",
      name = "Moneycontrol Financial News",
      category = "FINANCIAL_PUBLISHER",
      url = "https://www.moneycontrol.com/rss/MCtopnews.xml",
      pollIntervalMinutes = 10
    ),
    FeedConfig(
      id = "et_rss",
      name = "The Economic Times Markets",
      category = "FINANCIAL_PUBLISHER",
      url = "https://economictimes.indiatimes.com/markets/rssfeeds/1977021501.cms",
      pollIntervalMinutes = 10
    ),
    FeedConfig(
      id = "mint_rss",
      name = "Mint Financial News",
      category = "FINANCIAL_PUBLISHER",
      url = "https://www.livemint.com/rss/news",
      pollIntervalMinutes = 10
    )
  )

  suspend fun fetchFeed(feed: FeedConfig): Result<Pair<List<MarketEvent>, SourceHealth>> = withContext(Dispatchers.IO) {
    val checkTime = System.currentTimeMillis()
    try {
      val request = Request.Builder()
        .url(feed.url)
        .header("User-Agent", "MarketPulse-Android/1.0 (Financial News Intelligence)")
        .build()

      client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
          val errorHealth = SourceHealth(
            sourceId = feed.id,
            sourceName = feed.name,
            sourceType = feed.category,
            feedUrl = feed.url,
            lastCheckTime = checkTime,
            status = "OFFLINE",
            errorMessage = "HTTP ${response.code}: ${response.message}",
            pollIntervalMinutes = feed.pollIntervalMinutes
          )
          return@withContext Result.failure(Exception("HTTP ${response.code}"))
        }

        val xmlBody = response.body?.string() ?: ""
        val events = parseRssXml(xmlBody, feed)

        val health = SourceHealth(
          sourceId = feed.id,
          sourceName = feed.name,
          sourceType = feed.category,
          feedUrl = feed.url,
          lastCheckTime = checkTime,
          lastPublicationTime = events.maxOfOrNull { it.publicationTime } ?: checkTime,
          lastIngestionTime = checkTime,
          lastSuccessfulRefresh = checkTime,
          status = "ONLINE",
          errorMessage = null,
          pollIntervalMinutes = feed.pollIntervalMinutes
        )

        Result.success(Pair(events, health))
      }
    } catch (e: Exception) {
      val health = SourceHealth(
        sourceId = feed.id,
        sourceName = feed.name,
        sourceType = feed.category,
        feedUrl = feed.url,
        lastCheckTime = checkTime,
        status = "DEGRADED",
        errorMessage = e.message,
        pollIntervalMinutes = feed.pollIntervalMinutes
      )
      Result.failure(e)
    }
  }

  private fun parseRssXml(xml: String, feed: FeedConfig): List<MarketEvent> {
    val events = mutableListOf<MarketEvent>()
    if (xml.isBlank()) return events

    try {
      val parser = Xml.newPullParser()
      parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
      parser.setInput(StringReader(xml))

      var eventType = parser.eventType
      var currentTag = ""
      var title = ""
      var link = ""
      var description = ""
      var pubDateStr = ""
      var insideItem = false

      val istFormat = SimpleDateFormat("hh:mm a 'IST'", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("Asia/Kolkata")
      }

      while (eventType != XmlPullParser.END_DOCUMENT) {
        when (eventType) {
          XmlPullParser.START_TAG -> {
            currentTag = parser.name
            if (currentTag.equals("item", ignoreCase = true) || currentTag.equals("entry", ignoreCase = true)) {
              insideItem = true
              title = ""
              link = ""
              description = ""
              pubDateStr = ""
            }
          }
          XmlPullParser.TEXT -> {
            if (insideItem) {
              val text = parser.text?.trim() ?: ""
              when {
                currentTag.equals("title", ignoreCase = true) -> title += text
                currentTag.equals("link", ignoreCase = true) -> link += text
                currentTag.equals("description", ignoreCase = true) || currentTag.equals("summary", ignoreCase = true) -> description += text
                currentTag.equals("pubDate", ignoreCase = true) || currentTag.equals("published", ignoreCase = true) -> pubDateStr += text
              }
            }
          }
          XmlPullParser.END_TAG -> {
            if (parser.name.equals("item", ignoreCase = true) || parser.name.equals("entry", ignoreCase = true)) {
              insideItem = false
              if (title.isNotBlank()) {
                val cleanTitle = title.trim()
                val cleanSummary = description.replace(Regex("<[^>]*>"), "").trim().take(300)
                val pubTime = parsePubDate(pubDateStr)
                val entityInfo = resolveEntity(cleanTitle + " " + cleanSummary)

                val verificationType = if (feed.category == "OFFICIAL_REGULATOR") {
                  VerificationType.OFFICIAL_SOURCE_VERIFIED.name
                } else {
                  VerificationType.REPORTED_BY_PUBLISHER.name
                }

                val eventCategory = if (feed.category == "OFFICIAL_REGULATOR") {
                  EventCategory.REGULATORY.name
                } else {
                  EventCategory.DISCLOSURE.name
                }

                val canonicalTitle = cleanTitle.replace(Regex("[^a-zA-Z0-9 ]"), "").lowercase()
                val eventId = "evt_${feed.id}_${canonicalTitle.hashCode().toString().replace("-", "n")}"

                events.add(
                  MarketEvent(
                    id = eventId,
                    canonicalTitle = cleanTitle,
                    title = cleanTitle,
                    summary = cleanSummary.ifBlank { cleanTitle },
                    beginnerExplainer = "WHAT HAPPENED:\n$cleanTitle\n\nSOURCE:\n${feed.name}\n\nVERIFICATION:\n$verificationType",
                    category = eventCategory,
                    publisher = feed.name,
                    sourceType = feed.category,
                    sourceName = feed.name,
                    sourceUrl = link.ifBlank { feed.url },
                    officialDocumentUrl = if (feed.category == "OFFICIAL_REGULATOR") link else null,
                    publicationTime = pubTime,
                    eventTime = pubTime,
                    detectedTime = System.currentTimeMillis(),
                    ingestedTime = System.currentTimeMillis(),
                    formattedTime = istFormat.format(Date(pubTime)),
                    entitySymbol = entityInfo.symbol,
                    entityName = entityInfo.name,
                    sector = entityInfo.sector,
                    companyIds = entityInfo.symbol,
                    sectorIds = entityInfo.sector.uppercase().replace(" ", "_"),
                    timestamp = pubTime,
                    materiality = if (feed.category == "OFFICIAL_REGULATOR") EventMateriality.HIGH.name else EventMateriality.MEDIUM.name,
                    verificationType = verificationType,
                    sourceRetrievalStatus = "SUCCESS"
                  )
                )
              }
            }
          }
        }
        eventType = parser.next()
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
    return events.take(15)
  }

  private fun parsePubDate(dateStr: String): Long {
    if (dateStr.isBlank()) return System.currentTimeMillis()
    val formats = listOf(
      "EEE, dd MMM yyyy HH:mm:ss z",
      "EEE, dd MMM yyyy HH:mm:ss Z",
      "yyyy-MM-dd'T'HH:mm:ss'Z'",
      "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
      "yyyy-MM-dd HH:mm:ss"
    )
    for (fmt in formats) {
      try {
        val sdf = SimpleDateFormat(fmt, Locale.US)
        val parsed = sdf.parse(dateStr)
        if (parsed != null) return parsed.time
      } catch (_: Exception) {}
    }
    return System.currentTimeMillis()
  }

  private data class EntityInfo(val symbol: String, val name: String, val sector: String)

  private fun resolveEntity(text: String): EntityInfo {
    val upper = text.uppercase()
    return when {
      upper.contains("RELIANCE") -> EntityInfo("RELIANCE", "Reliance Industries Ltd.", "Energy & Telecom")
      upper.contains("TCS") || upper.contains("TATA CONSULTANCY") -> EntityInfo("TCS", "Tata Consultancy Services", "Information Technology")
      upper.contains("HDFC BANK") || upper.contains("HDFCBANK") -> EntityInfo("HDFCBANK", "HDFC Bank Ltd.", "Banking & Financials")
      upper.contains("INFOSYS") || upper.contains("INFY") -> EntityInfo("INFY", "Infosys Ltd.", "Information Technology")
      upper.contains("LARSEN") || upper.contains("L&T") -> EntityInfo("LT", "Larsen & Toubro Ltd.", "Infrastructure & EPC")
      upper.contains("TATA MOTORS") -> EntityInfo("TATAMOTORS", "Tata Motors Ltd.", "Automotive")
      upper.contains("SEBI") -> EntityInfo("SEBI", "Securities and Exchange Board of India", "Capital Markets")
      upper.contains("RBI") || upper.contains("RESERVE BANK") -> EntityInfo("RBI", "Reserve Bank of India", "Banking & Monetary")
      else -> EntityInfo("MACRO", "Indian Markets", "Macro Economy")
    }
  }
}
