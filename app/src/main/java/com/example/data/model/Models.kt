package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Strict verification status for truth tagging:
 * - OFFICIAL_SOURCE_VERIFIED: Official exchange filing, SEBI/RBI regulatory notification.
 * - REPORTED_BY_PUBLISHER: Financial press reporting (Reuters, Mint, ET, Moneycontrol).
 * - MULTIPLE_SOURCES: Cross-verified by 2+ independent publisher feeds.
 * - ANALYSIS: Model-generated or analytical context.
 * - UNVERIFIED: Single non-official disclosure or rumor.
 */
enum class VerificationType {
  OFFICIAL_SOURCE_VERIFIED,
  REPORTED_BY_PUBLISHER,
  MULTIPLE_SOURCES,
  ANALYSIS,
  UNVERIFIED;

  companion object {
    fun fromLegacy(type: String?): VerificationType {
      return when (type?.uppercase()) {
        "CONFIRMED_FACT", "OFFICIAL_SOURCE_VERIFIED" -> OFFICIAL_SOURCE_VERIFIED
        "REPORTED_MEDIA", "REPORTED_BY_PUBLISHER" -> REPORTED_BY_PUBLISHER
        "MULTIPLE_SOURCES" -> MULTIPLE_SOURCES
        "ANALYSIS" -> ANALYSIS
        else -> UNVERIFIED
      }
    }
  }
}

enum class EventMateriality {
  HIGH,
  MEDIUM,
  LOW
}

enum class EventCategory {
  DISCLOSURE,
  REGULATORY,
  EARNINGS,
  CONTRACT_WIN,
  FUNDRAISING,
  CORPORATE_ACTION,
  BOARD_MANAGEMENT,
  CREDIT_RATINGS,
  INSOLVENCY_LITIGATION,
  MACRO_ECONOMIC,
  GLOBAL,
  SECTOR_DEVELOPMENT
}

@Entity(tableName = "market_events")
data class MarketEvent(
  @PrimaryKey val id: String,
  val canonicalTitle: String,
  val title: String,
  val summary: String,
  val beginnerExplainer: String,
  val category: String, // EventCategory string name
  val publisher: String, // SEBI, RBI, NSE, BSE, Moneycontrol, Reuters, ET, Mint
  val sourceType: String, // OFFICIAL_REGULATOR, STOCK_EXCHANGE, GOVT_MINISTRY, FINANCIAL_PUBLISHER
  val sourceName: String,
  val sourceUrl: String,
  val officialDocumentUrl: String? = null,
  val publicationTime: Long = System.currentTimeMillis(), // UTC millis
  val eventTime: Long = System.currentTimeMillis(),
  val detectedTime: Long = System.currentTimeMillis(),
  val ingestedTime: Long = System.currentTimeMillis(),
  val formattedTime: String, // e.g. "10:15 AM IST"
  val entitySymbol: String, // e.g. RELIANCE, TCS, RBI, SEBI
  val entityName: String,
  val sector: String, // IT, Banking, Energy, Automotive, Macro, etc.
  val companyIds: String = "", // Comma-separated symbols
  val sectorIds: String = "",
  val timestamp: Long = System.currentTimeMillis(),
  val materiality: String = "MEDIUM", // EventMateriality string name
  val verificationType: String = "OFFICIAL_SOURCE_VERIFIED", // VerificationType string name
  val sourceRetrievalStatus: String = "SUCCESS", // SUCCESS, STALE, CACHED, FAILED
  val duplicateGroupId: String? = null,
  val relatedEventIds: String? = null,
  val correctionOrSupersessionReference: String? = null,
  val disclosedValue: String? = null,
  val isBookmarked: Boolean = false,
  val filingId: String? = null
)

/**
 * Dedicated normalized table for Listed Company Corporate Disclosures (NSE & BSE)
 */
@Entity(tableName = "corporate_disclosures")
data class CorporateDisclosure(
  @PrimaryKey val id: String,
  val companySymbol: String,
  val companyName: String,
  val exchange: String, // "NSE", "BSE"
  val filingCategory: String, // "CONTRACT_WIN", "EARNINGS", "DEMERGER_SPLIT", "DIVIDEND", "CAPEX"
  val title: String,
  val summary: String,
  val disclosedValue: String?,
  val filingDate: Long,
  val formattedDate: String,
  val documentUrl: String,
  val filingReferenceId: String,
  val beginnerExplanation: String,
  val materiality: String // "HIGH", "MEDIUM", "LOW"
)

/**
 * Dedicated normalized table for Financial Regulators (SEBI, RBI, Ministry of Finance)
 */
@Entity(tableName = "regulatory_circulars")
data class RegulatoryCircular(
  @PrimaryKey val id: String,
  val regulator: String, // "SEBI", "RBI", "MoF"
  val circularNumber: String,
  val title: String,
  val department: String,
  val summary: String,
  val actionRequired: String,
  val publicationDate: Long,
  val formattedDate: String,
  val effectiveDate: String,
  val documentUrl: String,
  val materiality: String, // "HIGH", "MEDIUM", "LOW"
  val plainLanguageTakeaway: String
)

/**
 * Source Health tracking entity for real-time ingestion monitoring
 */
@Entity(tableName = "source_health")
data class SourceHealth(
  @PrimaryKey val sourceId: String, // e.g. "sebi_rss", "rbi_rss", "nse_announcements", "moneycontrol_rss"
  val sourceName: String,
  val publisherName: String = sourceName,
  val sourceType: String, // "OFFICIAL", "EXCHANGE", "PUBLISHER"
  val feedUrl: String,
  val officialTermsUrl: String = "",
  val integrationMethod: String = "OFFICIAL_RSS", // "OFFICIAL_API", "OFFICIAL_RSS", "LINK_ONLY", "DISABLED"
  val rightsStatus: String = "PERMISSION_CONFIRMED", // "PERMISSION_CONFIRMED", "LICENSED_FEED", "LINK_ONLY", "PAUSED_PENDING_PERMISSION", "RIGHTS_UNKNOWN"
  val permittedContentFields: String = "title,summary,link",
  val attributionRequirements: String = "",
  val cachingPermissions: Boolean = true,
  val redistributionPermissions: Boolean = true,
  val lastRightsReviewedAt: Long = System.currentTimeMillis(),
  val lastCheckTime: Long = System.currentTimeMillis(),
  val lastPublicationTime: Long = System.currentTimeMillis(),
  val lastIngestionTime: Long = System.currentTimeMillis(),
  val lastSuccessfulRefresh: Long = System.currentTimeMillis(),
  val status: String = "ONLINE", // "ONLINE", "RIGHTS_RESTRICTED", "DEGRADED", "OFFLINE", "STALE"
  val errorMessage: String? = null,
  val pollIntervalMinutes: Int = 10
)

/**
 * User Push Notifications History entity
 */
@Entity(tableName = "user_notifications")
data class NotificationItem(
  @PrimaryKey val id: String,
  val eventId: String? = null,
  val title: String,
  val body: String,
  val category: String, // "REGULATORY", "EARNINGS", "DISCLOSURE", "WATCHLIST"
  val timestamp: Long = System.currentTimeMillis(),
  val formattedTime: String,
  val isRead: Boolean = false,
  val deepLinkUrl: String? = null
)

@Entity(tableName = "market_indices")
data class MarketIndex(
  @PrimaryKey val symbol: String, // NIFTY 50, SENSEX, BANK NIFTY, NIFTY IT
  val name: String,
  val currentValue: Double,
  val changeValue: Double,
  val changePercent: Double,
  val sparklineJson: String, // Serialized comma-separated float values for canvas
  val previousClose: Double = currentValue - changeValue,
  val marketSessionStatus: String = "CLOSED", // "OPEN", "CLOSED", "PRE_OPEN", "UNKNOWN"
  val dataFreshnessStatus: String = "LAST_CLOSE", // "LIVE", "DELAYED", "LAST_CLOSE", "STALE", "DEMO", "UNAVAILABLE"
  val tradingStatus: String = dataFreshnessStatus,
  val provider: String = "Yahoo Finance (NSE/BSE)",
  val lastFetchedTimestamp: Long = System.currentTimeMillis(),
  val sourceTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "stock_quotes")
data class StockQuote(
  @PrimaryKey val symbol: String, // RELIANCE, TCS, HDFCBANK, INFY, ICICIBANK, TATAMOTORS, SBIN, BHARTIARTL
  val companyName: String,
  val sector: String,
  val currentPrice: Double,
  val changePercent: Double,
  val dayHigh: Double,
  val dayLow: Double,
  val peRatio: Double,
  val marketCap: String,
  val sparklineJson: String,
  val previousClose: Double = currentPrice / (1 + (changePercent / 100.0)),
  val marketSessionStatus: String = "CLOSED",
  val dataFreshnessStatus: String = "LAST_CLOSE",
  val tradingStatus: String = dataFreshnessStatus,
  val provider: String = "NSE_OFFICIAL",
  val lastFetchedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "paper_account")
data class PaperAccount(
  @PrimaryKey val id: Int = 1,
  val cashBalance: Double = 1000000.0, // ₹10 Lakhs starting capital
  val investedValue: Double = 0.0,
  val realizedPnl: Double = 0.0,
  val initialCapital: Double = 1000000.0
)

@Entity(tableName = "paper_orders")
data class PaperOrder(
  @PrimaryKey val orderId: String,
  val symbol: String,
  val companyName: String,
  val side: String, // "BUY", "SELL"
  val orderType: String, // "MARKET", "LIMIT"
  val quantity: Int,
  val price: Double,
  val status: String, // "FILLED", "PENDING", "CANCELLED"
  val timestamp: Long,
  val formattedDate: String
)

@Entity(tableName = "paper_positions")
data class PaperPosition(
  @PrimaryKey val symbol: String,
  val companyName: String,
  val quantity: Int,
  val avgPrice: Double,
  val currentPrice: Double
)

/**
 * Dedicated double-entry cash ledger audit trail
 */
@Entity(tableName = "cash_ledger_entries")
data class CashLedgerEntry(
  @PrimaryKey val entryId: String,
  val type: String, // "CREDIT", "DEBIT"
  val category: String, // "INITIAL_DEPOSIT", "STOCK_BUY_DEBIT", "STOCK_SELL_CREDIT", "BROKERAGE_DEBIT", "PORTFOLIO_RESET"
  val description: String,
  val amount: Double,
  val runningBalance: Double,
  val linkedOrderId: String? = null,
  val timestamp: Long,
  val formattedTime: String
)

data class LedgerCategoryAggregate(
  val category: String,
  val totalAmount: Double,
  val entryCount: Int
)

data class LedgerBalanceSummary(
  val totalCredits: Double,
  val totalDebits: Double,
  val netAggregatedCash: Double,
  val totalBrokeragePaid: Double,
  val latestRunningBalance: Double,
  val totalTransactions: Int
)

@Entity(tableName = "watchlist_items")
data class WatchlistItem(
  @PrimaryKey val symbol: String,
  val companyName: String,
  val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "trading_journal")
data class TradingJournalEntry(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val symbol: String,
  val title: String,
  val notes: String,
  val linkedEventTitle: String? = null,
  val timestamp: Long = System.currentTimeMillis()
)

data class EconomicIndicator(
  val id: String,
  val title: String,
  val authority: String, // RBI, MoSPI, Ministry of Commerce
  val currentValue: String,
  val previousValue: String,
  val impact: String, // "Hawkish", "Bullish", "Neutral"
  val nextReleaseDate: String,
  val description: String
)

data class ChatMessage(
  val id: String,
  val sender: String, // "USER" or "ASSISTANT"
  val text: String,
  val timestamp: Long = System.currentTimeMillis(),
  val formattedTime: String = "",
  val citedSources: List<String> = emptyList(),
  val citedUrls: List<String> = emptyList(),
  val verificationType: String? = null,
  val isGrounded: Boolean = true,
  val isLoading: Boolean = false
)
