package com.example.data.repository

import com.example.data.local.MarketDao
import com.example.data.local.PaperTradingDao
import com.example.data.model.CashLedgerEntry
import com.example.data.model.CorporateDisclosure
import com.example.data.model.EconomicIndicator
import com.example.data.model.EventCategory
import com.example.data.model.EventMateriality
import com.example.data.model.MarketEvent
import com.example.data.model.MarketIndex
import com.example.data.model.PaperAccount
import com.example.data.model.PaperOrder
import com.example.data.model.PaperPosition
import com.example.data.model.RegulatoryCircular
import com.example.data.model.StockQuote
import com.example.data.model.TradingJournalEntry
import com.example.data.model.VerificationType
import com.example.data.model.WatchlistItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class MarketPulseRepository(
  private val marketDao: MarketDao,
  private val paperDao: PaperTradingDao
) {

  val allEvents: Flow<List<MarketEvent>> = marketDao.getAllEvents()
  val allDisclosures: Flow<List<CorporateDisclosure>> = marketDao.getAllDisclosures()
  val allCirculars: Flow<List<RegulatoryCircular>> = marketDao.getAllCirculars()
  val allSourceHealth: Flow<List<com.example.data.model.SourceHealth>> = marketDao.getAllSourceHealth()
  val allNotifications: Flow<List<com.example.data.model.NotificationItem>> = marketDao.getAllNotifications()
  val allIndices: Flow<List<MarketIndex>> = marketDao.getAllIndices()
  val allQuotes: Flow<List<StockQuote>> = marketDao.getAllStockQuotes()
  val watchlist: Flow<List<WatchlistItem>> = marketDao.getWatchlist()
  val paperAccount: Flow<PaperAccount?> = paperDao.getAccountFlow()
  val paperPositions: Flow<List<PaperPosition>> = paperDao.getAllPositions()
  val paperOrders: Flow<List<PaperOrder>> = paperDao.getAllOrders()
  val cashLedger: Flow<List<CashLedgerEntry>> = paperDao.getAllLedgerEntries()
  val journalEntries: Flow<List<TradingJournalEntry>> = paperDao.getAllJournalEntries()

  private val rssManager = com.example.data.remote.RssIngestionManager()

  suspend fun initializeSeedDataIfNeeded() = withContext(Dispatchers.IO) {
    val existingEvents = marketDao.getAllEvents().firstOrNull()
    if (existingEvents.isNullOrEmpty()) {
      marketDao.insertEvents(getInitialEvents())
    }

    val existingDisclosures = marketDao.getAllDisclosures().firstOrNull()
    if (existingDisclosures.isNullOrEmpty()) {
      marketDao.insertDisclosures(getMultiDayDisclosures())
    }

    val existingCirculars = marketDao.getAllCirculars().firstOrNull()
    if (existingCirculars.isNullOrEmpty()) {
      marketDao.insertCirculars(getMultiDayCirculars())
    }

    val existingIndices = marketDao.getAllIndices().firstOrNull()
    if (existingIndices.isNullOrEmpty()) {
      marketDao.insertIndices(getInitialIndices())
    }

    val existingQuotes = marketDao.getAllStockQuotes().firstOrNull()
    if (existingQuotes.isNullOrEmpty()) {
      marketDao.insertStockQuotes(getInitialQuotes())
    }

    val account = paperDao.getAccount()
    if (account == null) {
      paperDao.insertOrUpdateAccount(
        PaperAccount(
          id = 1,
          cashBalance = 1000000.0,
          investedValue = 0.0,
          realizedPnl = 0.0,
          initialCapital = 1000000.0
        )
      )
      // Initial double-entry ledger entry
      val timeFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
      paperDao.insertLedgerEntry(
        CashLedgerEntry(
          entryId = "LEDG-INIT-01",
          type = "CREDIT",
          category = "INITIAL_DEPOSIT",
          description = "Initial Virtual Account Funding",
          amount = 1000000.0,
          runningBalance = 1000000.0,
          timestamp = System.currentTimeMillis(),
          formattedTime = timeFormat.format(Date())
        )
      )
    }

    val initialWatchlist = marketDao.getWatchlist().firstOrNull()
    if (initialWatchlist.isNullOrEmpty()) {
      marketDao.addToWatchlist(WatchlistItem(symbol = "RELIANCE", companyName = "Reliance Industries Ltd."))
      marketDao.addToWatchlist(WatchlistItem(symbol = "TCS", companyName = "Tata Consultancy Services"))
      marketDao.addToWatchlist(WatchlistItem(symbol = "HDFCBANK", companyName = "HDFC Bank Ltd."))
    }
  }

  suspend fun refreshRssFeeds() = withContext(Dispatchers.IO) {
    refreshIndices()
    for (feed in rssManager.officialFeeds) {
      val result = rssManager.fetchFeed(feed)
      result.onSuccess { (events, health) ->
        marketDao.insertSourceHealth(listOf(health))
        if (events.isNotEmpty()) {
          marketDao.insertEvents(events)
        }
      }.onFailure { err ->
        marketDao.updateSourceStatus(
          sourceId = feed.id,
          checkTime = System.currentTimeMillis(),
          refreshTime = System.currentTimeMillis(),
          status = "DEGRADED",
          error = err.message
        )
      }
    }
  }

  suspend fun refreshIndices(): Result<List<MarketIndex>> = withContext(Dispatchers.IO) {
    try {
      val response = apiService.getMarketIndices()
      if (response.isSuccessful && response.body()?.status == "success") {
        val indicesList = response.body()?.indices ?: emptyList()
        if (indicesList.isNotEmpty()) {
          val validatedIndices = indicesList.map { idx ->
            val prevClose = if (idx.previousClose > 0) idx.previousClose else idx.currentValue - idx.changeValue
            val diff = idx.currentValue - prevClose
            val pct = if (prevClose > 0) (diff / prevClose) * 100.0 else 0.0
            idx.copy(
              previousClose = prevClose,
              changeValue = diff,
              changePercent = pct
            )
          }
          marketDao.insertIndices(validatedIndices)
          return@withContext Result.success(validatedIndices)
        }
      }
    } catch (_: Exception) {}
    Result.failure(Exception("Failed to fetch live indices from provider"))
  }

  suspend fun markNotificationAsRead(id: String) = withContext(Dispatchers.IO) {
    marketDao.markNotificationAsRead(id)
  }

  private val configuredUrl = com.example.BuildConfig.DEFAULT_API_BASE_URL
  private val apiBaseUrl = if (configuredUrl.isNotBlank() && configuredUrl.startsWith("http")) configuredUrl else "http://10.0.2.2:8080/"

  private val retrofit by lazy {
    retrofit2.Retrofit.Builder()
      .baseUrl(apiBaseUrl)
      .addConverterFactory(retrofit2.converter.moshi.MoshiConverterFactory.create())
      .build()
  }

  private val apiService by lazy {
    retrofit.create(com.example.data.remote.MarketPulseApiService::class.java)
  }

  suspend fun generateGroundedAiResponse(prompt: String): com.example.data.model.ChatMessage = withContext(Dispatchers.IO) {
    val timeFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())

    // 1. Try primary server-side RAG backend API endpoint
    try {
      val response = apiService.sendChatMessage(com.example.data.remote.ChatRequest(prompt))
      if (response.isSuccessful && response.body()?.status == "success") {
        val body = response.body()!!
        return@withContext com.example.data.model.ChatMessage(
          id = UUID.randomUUID().toString(),
          sender = "ASSISTANT",
          text = body.text,
          timestamp = System.currentTimeMillis(),
          formattedTime = timeFormat.format(Date()),
          citedSources = body.citedSources,
          citedUrls = body.citedUrls,
          verificationType = body.verificationType ?: "OFFICIAL_SOURCE_VERIFIED",
          isGrounded = body.isGrounded
        )
      }
    } catch (_: Exception) {
      // Backend unavailable; falling back to local Room database RAG
    }

    // 2. Offline fallback: local RAG over stored Room events
    val currentEvents = marketDao.getAllEvents().firstOrNull() ?: emptyList()
    val promptLower = prompt.lowercase()

    val relevant = currentEvents.filter { e ->
      promptLower.contains(e.entitySymbol.lowercase()) ||
        promptLower.contains(e.sector.lowercase()) ||
        e.title.lowercase().split(" ").any { word -> word.length > 3 && promptLower.contains(word) }
    }.take(4)

    val contextEvents = if (relevant.isNotEmpty()) relevant else currentEvents.take(4)

    val textBuilder = StringBuilder()
    textBuilder.append("Based on retrieved MarketPulse canonical evidence (Offline Local Storage):\n\n")

    val citedSources = mutableListOf<String>()
    val citedUrls = mutableListOf<String>()

    contextEvents.forEachIndexed { idx, evt ->
      textBuilder.append("**[${idx + 1}] ${evt.title}**\n")
      textBuilder.append("• Entity: ${evt.entityName} (${evt.entitySymbol})\n")
      textBuilder.append("• Publisher: ${evt.publisher} (${evt.formattedTime})\n")
      textBuilder.append("• Verification: `${evt.verificationType}`\n")
      textBuilder.append("• Summary: ${evt.summary}\n\n")

      citedSources.add("${evt.publisher} (${evt.formattedTime})")
      citedUrls.add(evt.sourceUrl)
    }

    textBuilder.append("---\n*Financial Information Disclaimer: MarketPulse AI provides research insights grounded in official filings. MarketPulse is not a SEBI-registered investment advisor.*")

    com.example.data.model.ChatMessage(
      id = UUID.randomUUID().toString(),
      sender = "ASSISTANT",
      text = textBuilder.toString(),
      timestamp = System.currentTimeMillis(),
      formattedTime = timeFormat.format(Date()),
      citedSources = citedSources,
      citedUrls = citedUrls,
      verificationType = contextEvents.firstOrNull()?.verificationType ?: "OFFICIAL_SOURCE_VERIFIED",
      isGrounded = true
    )
  }

  suspend fun toggleBookmark(eventId: String, currentStatus: Boolean) = withContext(Dispatchers.IO) {
    marketDao.setBookmark(eventId, !currentStatus)
  }

  suspend fun toggleWatchlist(symbol: String, companyName: String) = withContext(Dispatchers.IO) {
    if (marketDao.isInWatchlist(symbol)) {
      marketDao.removeFromWatchlistBySymbol(symbol)
    } else {
      marketDao.addToWatchlist(WatchlistItem(symbol = symbol, companyName = companyName))
    }
  }

  suspend fun placePaperOrder(
    symbol: String,
    side: String, // "BUY" or "SELL"
    orderType: String, // "MARKET" or "LIMIT"
    quantity: Int,
    limitPrice: Double?
  ): Result<String> = withContext(Dispatchers.IO) {
    if (quantity <= 0) return@withContext Result.failure(IllegalArgumentException("Quantity must be greater than zero."))

    val quote = marketDao.getQuoteBySymbol(symbol)
      ?: return@withContext Result.failure(IllegalArgumentException("Symbol $symbol not found in market registry."))

    val executionPrice = if (orderType == "LIMIT" && limitPrice != null) limitPrice else quote.currentPrice
    val grossOrderTotal = executionPrice * quantity
    val brokerageTaxes = 20.0 // Simulated flat brokerage & taxes
    val currentAccount = paperDao.getAccount() ?: PaperAccount()

    val timeFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    val formattedDate = timeFormat.format(Date())
    val now = System.currentTimeMillis()
    val orderId = UUID.randomUUID().toString().take(8).uppercase()

    if (side == "BUY") {
      val totalRequired = grossOrderTotal + brokerageTaxes
      if (currentAccount.cashBalance < totalRequired) {
        val deficit = totalRequired - currentAccount.cashBalance
        return@withContext Result.failure(
          IllegalStateException("Insufficient cash balance! Required: ₹${String.format(Locale.US, "%,.2f", totalRequired)}, Shortfall: ₹${String.format(Locale.US, "%,.2f", deficit)}")
        )
      }

      // Update Position with Weighted Average Price
      val existingPosition = paperDao.getPositionBySymbol(symbol)
      if (existingPosition != null) {
        val totalQty = existingPosition.quantity + quantity
        val blendedCost = ((existingPosition.quantity * existingPosition.avgPrice) + grossOrderTotal) / totalQty
        paperDao.savePosition(
          existingPosition.copy(
            quantity = totalQty,
            avgPrice = blendedCost,
            currentPrice = quote.currentPrice
          )
        )
      } else {
        paperDao.savePosition(
          PaperPosition(
            symbol = symbol,
            companyName = quote.companyName,
            quantity = quantity,
            avgPrice = executionPrice,
            currentPrice = quote.currentPrice
          )
        )
      }

      val newCash = currentAccount.cashBalance - totalRequired
      val newInvested = currentAccount.investedValue + grossOrderTotal
      paperDao.insertOrUpdateAccount(
        currentAccount.copy(
          cashBalance = newCash,
          investedValue = newInvested
        )
      )

      // Save Order
      val order = PaperOrder(
        orderId = orderId,
        symbol = symbol,
        companyName = quote.companyName,
        side = "BUY",
        orderType = orderType,
        quantity = quantity,
        price = executionPrice,
        status = "FILLED",
        timestamp = now,
        formattedDate = formattedDate
      )
      paperDao.insertOrder(order)

      // Double-entry cash ledger audit: 1. Stock debit
      val intermediateCash = currentAccount.cashBalance - grossOrderTotal
      paperDao.insertLedgerEntry(
        CashLedgerEntry(
          entryId = "LEDG-DR-$orderId-1",
          type = "DEBIT",
          category = "STOCK_BUY_DEBIT",
          description = "Bought $quantity shares of $symbol @ ₹${String.format(Locale.US, "%.2f", executionPrice)}",
          amount = grossOrderTotal,
          runningBalance = intermediateCash,
          linkedOrderId = orderId,
          timestamp = now,
          formattedTime = formattedDate
        )
      )

      // Double-entry cash ledger audit: 2. Brokerage debit
      paperDao.insertLedgerEntry(
        CashLedgerEntry(
          entryId = "LEDG-DR-$orderId-2",
          type = "DEBIT",
          category = "BROKERAGE_DEBIT",
          description = "Exchange turnover fees & brokerage for $orderId",
          amount = brokerageTaxes,
          runningBalance = newCash,
          linkedOrderId = orderId,
          timestamp = now + 1,
          formattedTime = formattedDate
        )
      )

      Result.success("Bought $quantity shares of $symbol at ₹${String.format(Locale.US, "%.2f", executionPrice)}")
    } else {
      // SELL ORDER
      val existingPosition = paperDao.getPositionBySymbol(symbol)
      if (existingPosition == null || existingPosition.quantity < quantity) {
        val currentHoldings = existingPosition?.quantity ?: 0
        return@withContext Result.failure(
          IllegalStateException("Cannot sell $quantity shares. You only own $currentHoldings shares of $symbol.")
        )
      }

      val costBasis = existingPosition.avgPrice * quantity
      val grossProceeds = executionPrice * quantity
      val tradePnl = grossProceeds - costBasis

      val remainingQty = existingPosition.quantity - quantity
      if (remainingQty == 0) {
        paperDao.deletePosition(symbol)
      } else {
        paperDao.savePosition(
          existingPosition.copy(
            quantity = remainingQty,
            currentPrice = quote.currentPrice
          )
        )
      }

      val netProceeds = grossProceeds - brokerageTaxes
      val newCash = currentAccount.cashBalance + netProceeds
      val newInvested = (currentAccount.investedValue - costBasis).coerceAtLeast(0.0)
      val newRealizedPnl = currentAccount.realizedPnl + tradePnl - brokerageTaxes

      paperDao.insertOrUpdateAccount(
        currentAccount.copy(
          cashBalance = newCash,
          investedValue = newInvested,
          realizedPnl = newRealizedPnl
        )
      )

      val order = PaperOrder(
        orderId = orderId,
        symbol = symbol,
        companyName = quote.companyName,
        side = "SELL",
        orderType = orderType,
        quantity = quantity,
        price = executionPrice,
        status = "FILLED",
        timestamp = now,
        formattedDate = formattedDate
      )
      paperDao.insertOrder(order)

      // Double-entry cash ledger audit: 1. Stock gross proceeds
      val intermediateCash = currentAccount.cashBalance + grossProceeds
      paperDao.insertLedgerEntry(
        CashLedgerEntry(
          entryId = "LEDG-CR-$orderId-1",
          type = "CREDIT",
          category = "STOCK_SELL_CREDIT",
          description = "Sold $quantity shares of $symbol @ ₹${String.format(Locale.US, "%.2f", executionPrice)}",
          amount = grossProceeds,
          runningBalance = intermediateCash,
          linkedOrderId = orderId,
          timestamp = now,
          formattedTime = formattedDate
        )
      )

      // Double-entry cash ledger audit: 2. Brokerage debit
      paperDao.insertLedgerEntry(
        CashLedgerEntry(
          entryId = "LEDG-DR-$orderId-2",
          type = "DEBIT",
          category = "BROKERAGE_DEBIT",
          description = "Exchange turnover fees & STT for $orderId",
          amount = brokerageTaxes,
          runningBalance = newCash,
          linkedOrderId = orderId,
          timestamp = now + 1,
          formattedTime = formattedDate
        )
      )

      val pnlSign = if (tradePnl >= 0) "+₹" else "-₹"
      Result.success("Sold $quantity shares of $symbol. Trade P&L: $pnlSign${String.format(Locale.US, "%,.2f", Math.abs(tradePnl))}")
    }
  }

  suspend fun resetPaperAccount() = withContext(Dispatchers.IO) {
    paperDao.clearAllPositions()
    paperDao.clearAllOrders()
    paperDao.clearLedger()

    paperDao.insertOrUpdateAccount(
      PaperAccount(
        id = 1,
        cashBalance = 1000000.0,
        investedValue = 0.0,
        realizedPnl = 0.0,
        initialCapital = 1000000.0
      )
    )

    val timeFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    paperDao.insertLedgerEntry(
      CashLedgerEntry(
        entryId = "LEDG-RESET-${System.currentTimeMillis()}",
        type = "CREDIT",
        category = "PORTFOLIO_RESET",
        description = "Paper Portfolio Reset to Initial Virtual Capital",
        amount = 1000000.0,
        runningBalance = 1000000.0,
        timestamp = System.currentTimeMillis(),
        formattedTime = timeFormat.format(Date())
      )
    )
  }

  suspend fun addJournalNote(symbol: String, title: String, notes: String, linkedEvent: String? = null) = withContext(Dispatchers.IO) {
    paperDao.insertJournalEntry(
      TradingJournalEntry(
        symbol = symbol,
        title = title,
        notes = notes,
        linkedEventTitle = linkedEvent
      )
    )
  }

  fun getEconomicIndicators(): List<EconomicIndicator> {
    return listOf(
      EconomicIndicator(
        id = "ind_rbi_repo",
        title = "RBI Policy Repo Rate",
        authority = "Reserve Bank of India (MPC)",
        currentValue = "6.50%",
        previousValue = "6.50%",
        impact = "Neutral",
        nextReleaseDate = "Next MPC Meeting (Dec 2026)",
        description = "Monetary Policy Committee maintained repo rate at 6.50% with focus on withdrawal of accommodation to align inflation to 4% target."
      ),
      EconomicIndicator(
        id = "ind_cpi_inflation",
        title = "Consumer Price Index (CPI)",
        authority = "MoSPI (Govt of India)",
        currentValue = "5.49%",
        previousValue = "3.65%",
        impact = "Hawkish",
        nextReleaseDate = "12th of every month",
        description = "Headline retail inflation driven primarily by erratic monsoon food prices (vegetables, pulses), within RBI's 2-6% tolerance band."
      ),
      EconomicIndicator(
        id = "ind_gdp_growth",
        title = "Gross Domestic Product (GDP)",
        authority = "National Statistical Office",
        currentValue = "6.7% YoY",
        previousValue = "7.8% YoY",
        impact = "Bullish",
        nextReleaseDate = "Quarterly Release",
        description = "India remains the fastest growing major economy, propelled by government capital expenditure, manufacturing, and financial services."
      ),
      EconomicIndicator(
        id = "ind_iip_industrial",
        title = "Index of Industrial Production (IIP)",
        authority = "Ministry of Statistics",
        currentValue = "4.8% YoY",
        previousValue = "4.2% YoY",
        impact = "Bullish",
        nextReleaseDate = "Monthly Release",
        description = "Manufacturing sector output and capital goods infrastructure showed resilient expansion across mining, electricity, and capital goods."
      ),
      EconomicIndicator(
        id = "ind_forex_reserves",
        title = "Foreign Exchange Reserves",
        authority = "Reserve Bank of India",
        currentValue = "$704.8 Billion",
        previousValue = "$692.3 Billion",
        impact = "Bullish",
        nextReleaseDate = "Weekly on Fridays",
        description = "All-time record reserves providing solid buffer against currency volatility, import coverage exceeding 11 months of goods."
      )
    )
  }

  // Multi-day pre-seeded corporate filings across T, T-1, and T-2
  private fun getMultiDayDisclosures(): List<CorporateDisclosure> {
    val now = System.currentTimeMillis()
    val day = 24 * 60 * 60 * 1000L
    return listOf(
      CorporateDisclosure(
        id = "disc_lt_01",
        companySymbol = "LT",
        companyName = "Larsen & Toubro Ltd.",
        exchange = "NSE",
        filingCategory = "CONTRACT_WIN",
        title = "Mega Hydrocarbon EPC Contract Award in Middle East",
        summary = "Order valued in excess of ₹4,500 Crore awarded to L&T Hydrocarbon Onshore vertical for gas processing compressor station.",
        disclosedValue = "₹4,500 Crore",
        filingDate = now - (45 * 60 * 1000),
        formattedDate = "Today, 09:15 AM IST",
        documentUrl = "https://www.nseindia.com/filings/LT-EPC-2026.pdf",
        filingReferenceId = "NSE-CORP-LT-2026-1049",
        beginnerExplanation = "L&T won a large ₹4,500 Cr construction contract that gives clear multi-year revenue growth.",
        materiality = "HIGH"
      ),
      CorporateDisclosure(
        id = "disc_tcs_02",
        companySymbol = "TCS",
        companyName = "Tata Consultancy Services Ltd.",
        exchange = "BSE",
        filingCategory = "EARNINGS",
        title = "Audited Financial Results for Q2 FY27 and Interim Dividend",
        summary = "Net profit up 5.8% YoY at ₹11,909 Crore; board approves second interim dividend of ₹10 per equity share of face value ₹1.",
        disclosedValue = "₹11,909 Cr Profit / ₹10 Div",
        filingDate = now - (150 * 60 * 1000),
        formattedDate = "Today, 07:30 AM IST",
        documentUrl = "https://www.bseindia.com/filings/532540-Q2-2026.pdf",
        filingReferenceId = "BSE-CA-532540-20261017",
        beginnerExplanation = "TCS posted strong quarterly earnings and is rewarding investors with ₹10 per share cash dividend.",
        materiality = "HIGH"
      ),
      CorporateDisclosure(
        id = "disc_ril_03",
        companySymbol = "RELIANCE",
        companyName = "Reliance Industries Ltd.",
        exchange = "NSE",
        filingCategory = "CAPEX",
        title = "Commissioning of 5 GW Integrated Solar Photovoltaic Module Unit",
        summary = "Commercial synchronization of first phase of Jamnagar Green Energy Giga Complex completed with automated cell assembly.",
        disclosedValue = "5 GW Solar Capacity",
        filingDate = now - (day + (2 * 60 * 60 * 1000)),
        formattedDate = "Yesterday, 04:30 PM IST",
        documentUrl = "https://www.nseindia.com/filings/RELIANCE-GIGA-2026.pdf",
        filingReferenceId = "NSE-CORP-RELIANCE-2026-902",
        beginnerExplanation = "Reliance has begun commercial manufacturing at its massive solar giga-factory in Gujarat.",
        materiality = "HIGH"
      ),
      CorporateDisclosure(
        id = "disc_tamo_04",
        companySymbol = "TATAMOTORS",
        companyName = "Tata Motors Ltd.",
        exchange = "BSE",
        filingCategory = "DEMERGER_SPLIT",
        title = "National Company Law Tribunal Clears Demerger of CV and PV Businesses",
        summary = "Final sanctioned demerger order received. Equity shareholders to receive 1 share in commercial vehicle entity for each share held.",
        disclosedValue = "1:1 Share Demerger",
        filingDate = now - (day + (5 * 60 * 60 * 1000)),
        formattedDate = "Yesterday, 01:15 PM IST",
        documentUrl = "https://www.bseindia.com/filings/TATAMOTORS-NCLT-2026.pdf",
        filingReferenceId = "NCLT-MUMBAI-TAMO-2026",
        beginnerExplanation = "Tata Motors is splitting into 2 separately listed firms: passenger cars/EVs and commercial trucks.",
        materiality = "HIGH"
      ),
      CorporateDisclosure(
        id = "disc_infy_05",
        companySymbol = "INFY",
        companyName = "Infosys Ltd.",
        exchange = "NSE",
        filingCategory = "CONTRACT_WIN",
        title = "Expansion of Strategic Digital Transformation Engagement with Leading European Bank",
        summary = "Five-year agreement estimated at $1.2 Billion covering cloud migration, core banking modernization, and generative AI copilot rollout.",
        disclosedValue = "$1.2 Billion TCV",
        filingDate = now - (2 * day + (3 * 60 * 60 * 1000)),
        formattedDate = "2 Days Ago",
        documentUrl = "https://www.nseindia.com/filings/INFY-EUROPE-2026.pdf",
        filingReferenceId = "NSE-CORP-INFY-2026-811",
        beginnerExplanation = "Infosys won a long-term $1.2B digital banking modernization contract in Europe.",
        materiality = "HIGH"
      ),
      CorporateDisclosure(
        id = "disc_hdfc_06",
        companySymbol = "HDFCBANK",
        companyName = "HDFC Bank Ltd.",
        exchange = "BSE",
        filingCategory = "CAPEX",
        title = "Board Approves Issuance of Unsecured Tier-II Capital Bonds",
        summary = "Private placement of Basel III compliant Tier-II bonds aggregating up to ₹15,000 Crore to bolster capital adequacy ratio.",
        disclosedValue = "₹15,000 Crore Bonds",
        filingDate = now - (2 * day + (7 * 60 * 60 * 1000)),
        formattedDate = "2 Days Ago",
        documentUrl = "https://www.bseindia.com/filings/500180-BONDS-2026.pdf",
        filingReferenceId = "BSE-500180-2026-670",
        beginnerExplanation = "HDFC Bank is raising ₹15,000 Cr in subordinated bonds to support credit growth.",
        materiality = "MEDIUM"
      )
    )
  }

  // Multi-day pre-seeded regulatory circulars (SEBI & RBI)
  private fun getMultiDayCirculars(): List<RegulatoryCircular> {
    val now = System.currentTimeMillis()
    val day = 24 * 60 * 60 * 1000L
    return listOf(
      RegulatoryCircular(
        id = "circ_sebi_01",
        regulator = "SEBI",
        circularNumber = "CIR/MRD/DP/2026/89",
        title = "Measures to Strengthen Equity Index Derivatives Framework for Investor Protection",
        department = "Market Regulation Department (MRD)",
        summary = "Revision of minimum derivative contract size from ₹5 lakh to ₹15 lakh; rationalization of weekly index derivative expiries to only one benchmark per exchange.",
        actionRequired = "Trading members and clearing corporations to configure margin thresholds by Nov 20, 2026.",
        publicationDate = now - (15 * 60 * 1000),
        formattedDate = "Today, 09:45 AM IST",
        effectiveDate = "Nov 20, 2026",
        documentUrl = "https://www.sebi.gov.in/legal/circulars/index-derivatives-2026.html",
        materiality = "HIGH",
        plainLanguageTakeaway = "Trading index options will require higher capital; discourages hyper-speculative retail trading."
      ),
      RegulatoryCircular(
        id = "circ_rbi_02",
        regulator = "RBI",
        circularNumber = "RBI/2026-27/58",
        title = "Variable Rate Repo (VRR) Operations for Systemic Liquidity Infusion",
        department = "Financial Markets Operations Department",
        summary = "7-day Variable Rate Repo auction of ₹50,000 Crore conducted at 6.51% cut-off to smooth temporary frictional tax outflows.",
        actionRequired = "Scheduled commercial banks to submit bids via E-Kuber portal.",
        publicationDate = now - (90 * 60 * 1000),
        formattedDate = "Today, 08:30 AM IST",
        effectiveDate = "Immediate",
        documentUrl = "https://www.rbi.org.in/scripts/BS_PressReleaseDisplay.aspx?id=1412",
        materiality = "MEDIUM",
        plainLanguageTakeaway = "RBI provided short-term cash to banks to prevent borrowing interest rates from spiking."
      ),
      RegulatoryCircular(
        id = "circ_sebi_03",
        regulator = "SEBI",
        circularNumber = "SEBI/HO/IMD/2026/45",
        title = "Standardized Disclosures for Mutual Fund Total Expense Ratio (TER) Breakdown",
        department = "Investment Management Department",
        summary = "Asset Management Companies (AMCs) mandated to provide daily unbundled TER breakdowns distinguishing brokerage, GST, and distributor commission.",
        actionRequired = "All AMCs and AMFI to update fact-sheets and API feeds.",
        publicationDate = now - (day + (3 * 60 * 60 * 1000)),
        formattedDate = "Yesterday, 03:00 PM IST",
        effectiveDate = "Dec 01, 2026",
        documentUrl = "https://www.sebi.gov.in/circulars/ter-transparency-2026.html",
        materiality = "MEDIUM",
        plainLanguageTakeaway = "Mutual funds must disclose exact commission fees daily so investors know what they pay."
      ),
      RegulatoryCircular(
        id = "circ_rbi_04",
        regulator = "RBI",
        circularNumber = "RBI/DOR/2026-27/22",
        title = "Revised Regulatory Guidelines for Micro-Grid and Clean Energy Priority Lending",
        department = "Department of Regulation",
        summary = "Incentive weights of 125% assigned to commercial bank advances toward off-grid solar and battery storage systems under priority sector lending.",
        actionRequired = "Banks to incorporate revised risk-weights in quarterly regulatory returns.",
        publicationDate = now - (2 * day + (4 * 60 * 60 * 1000)),
        formattedDate = "2 Days Ago",
        effectiveDate = "Next Financial Quarter",
        documentUrl = "https://www.rbi.org.in/circulars/clean-energy-psl.html",
        materiality = "MEDIUM",
        plainLanguageTakeaway = "Banks will find it more attractive to lend to green solar projects."
      )
    )
  }

  // Pre-seed canonical Indian market events
  private fun getInitialEvents(): List<MarketEvent> {
    val now = System.currentTimeMillis()
    return listOf(
      MarketEvent(
        id = "evt_sebi_derivatives_01",
        canonicalTitle = "SEBI Mandates Higher Contract Size & Weekly Options Restrictions",
        title = "SEBI Mandates Higher Contract Size & Weekly Options Restrictions",
        summary = "SEBI has issued a circular revising the equity derivatives framework. Minimum contract value raised from ₹5 lakh to ₹15 lakh, and each exchange is limited to only one weekly index expiry contract to curb retail speculative trading.",
        beginnerExplainer = "WHAT HAPPENED:\nSEBI has made trading in options more expensive to protect individual retail traders.\n\nWHY IT MATTERS:\nTrading volume in NIFTY and SENSEX weekly options will reduce. Brokerages and stock exchanges may see a mild dip in transaction revenues, but systemic risk is significantly lower.\n\nWHO IS AFFECTED:\nRetail option traders, discount brokers (Zerodha, Groww, AngelOne), and exchanges (BSE, NSE).",
        category = EventCategory.REGULATORY.name,
        publisher = "SEBI",
        sourceType = "OFFICIAL_REGULATOR",
        sourceName = "SEBI",
        sourceUrl = "https://www.sebi.gov.in/legal/circulars/index-derivatives.html",
        entitySymbol = "SEBI",
        entityName = "Securities and Exchange Board of India",
        sector = "Capital Markets",
        timestamp = now - (15 * 60 * 1000),
        formattedTime = "09:45 AM IST",
        materiality = EventMateriality.HIGH.name,
        verificationType = VerificationType.OFFICIAL_SOURCE_VERIFIED.name,
        disclosedValue = "₹15 Lakh Minimum Lot",
        filingId = "CIR/MRD/DP/2026/89"
      ),
      MarketEvent(
        id = "evt_lt_order_02",
        canonicalTitle = "Larsen & Toubro Secures Mega ₹4,500 Crore Hydrocarbon EPC Order in Middle East",
        title = "Larsen & Toubro Secures Mega ₹4,500 Crore Hydrocarbon EPC Order in Middle East",
        summary = "L&T's Hydrocarbon business vertical has won a prestigious offshore package from an international energy major. Scope includes engineering, procurement, construction, and installation of gas compression facilities.",
        beginnerExplainer = "WHAT HAPPENED:\nL&T won a massive construction and engineering contract in the Middle East worth over ₹4,500 Crore.\n\nWHY IT MATTERS:\nAdds strongly to L&T's multi-year order book and ensures high revenue visibility for the next 3 to 4 fiscal years.\n\nWHAT TO VERIFY:\nCheck the company's margin guidance and execution timeline in the investor presentation.",
        category = EventCategory.CONTRACT_WIN.name,
        publisher = "NSE Corporate Filing",
        sourceType = "STOCK_EXCHANGE",
        sourceName = "NSE Corporate Filing",
        sourceUrl = "https://www.nseindia.com/companies-listing/corporate-filings",
        entitySymbol = "LT",
        entityName = "Larsen & Toubro Ltd.",
        sector = "Infrastructure & EPC",
        timestamp = now - (45 * 60 * 1000),
        formattedTime = "09:15 AM IST",
        materiality = EventMateriality.HIGH.name,
        verificationType = VerificationType.OFFICIAL_SOURCE_VERIFIED.name,
        disclosedValue = "₹4,500 Crore",
        filingId = "NSE-CORP-LT-2026-1049"
      ),
      MarketEvent(
        id = "evt_rbi_liquidity_03",
        canonicalTitle = "RBI Conducts 7-Day VRR Auction to Inject ₹50,000 Crore into Banking System",
        title = "RBI Conducts 7-Day VRR Auction to Inject ₹50,000 Crore into Banking System",
        summary = "To address festive season advance tax outflows and ensure smooth credit transmission, the Reserve Bank conducted a variable rate repo (VRR) auction of ₹50,000 crore at 6.51% cut-off yield.",
        beginnerExplainer = "WHAT HAPPENED:\nThe Reserve Bank of India supplied ₹50,000 Crore of temporary funds to commercial banks.\n\nWHY IT MATTERS:\nPrevents interbank money market interest rates from spiking, keeping borrowing costs stable for companies and home loan borrowers.\n\nKEY TAKEAWAY:\nReflects proactive liquidity management by RBI without changing baseline interest rates.",
        category = EventCategory.REGULATORY.name,
        publisher = "RBI Press Release",
        sourceType = "OFFICIAL_REGULATOR",
        sourceName = "RBI Press Release",
        sourceUrl = "https://www.rbi.org.in/scripts/BS_PressReleaseDisplay.aspx",
        entitySymbol = "RBI",
        entityName = "Reserve Bank of India",
        sector = "Banking & Monetary",
        timestamp = now - (90 * 60 * 1000),
        formattedTime = "08:30 AM IST",
        materiality = EventMateriality.MEDIUM.name,
        verificationType = VerificationType.OFFICIAL_SOURCE_VERIFIED.name,
        disclosedValue = "₹50,000 Crore Liquidity",
        filingId = "RBI-PR-2026-1412"
      ),
      MarketEvent(
        id = "evt_tcs_q2_earnings_04",
        canonicalTitle = "TCS Reports 5.8% YoY Net Profit Growth; Declares ₹10 Interim Dividend",
        title = "TCS Reports 5.8% YoY Net Profit Growth; Declares ₹10 Interim Dividend",
        summary = "Tata Consultancy Services posted Q2 consolidated revenue of ₹64,259 crore (up 7.6% YoY) and net profit of ₹11,909 crore. Total contract value (TCV) stood at $8.6 billion with robust BFSI and AI transformation demand.",
        beginnerExplainer = "WHAT HAPPENED:\nIndia's largest software exporter announced strong quarterly financial results and announced an extra dividend reward to shareholders.\n\nWHY IT MATTERS:\nTCS is the bellwether for the Indian IT sector; solid deal wins indicate global corporate technology spending is recovering.\n\nWHAT TO VERIFY:\nCheck the ex-dividend date to know if you qualify for the ₹10 payout.",
        category = EventCategory.EARNINGS.name,
        publisher = "BSE Corporate Announcements",
        sourceType = "STOCK_EXCHANGE",
        sourceName = "BSE Corporate Announcements",
        sourceUrl = "https://www.bseindia.com/corporates/ann.html",
        entitySymbol = "TCS",
        entityName = "Tata Consultancy Services Ltd.",
        sector = "Information Technology",
        timestamp = now - (150 * 60 * 1000),
        formattedTime = "07:30 AM IST",
        materiality = EventMateriality.HIGH.name,
        verificationType = VerificationType.OFFICIAL_SOURCE_VERIFIED.name,
        disclosedValue = "₹11,909 Cr Profit / ₹10 Div",
        filingId = "BSE-CA-532540-20261017"
      ),
      MarketEvent(
        id = "evt_reliance_green_05",
        canonicalTitle = "Reliance Industries Commissions Phase 1 of Solar Giga-Factory in Jamnagar",
        title = "Reliance Industries Commissions Phase 1 of Solar Giga-Factory in Jamnagar",
        summary = "Reliance New Energy announced commercial synchronization of the first 5 GW cell and module manufacturing unit at Dhirubhai Ambani Green Energy Giga Complex. Production of indigenously certified TOPCon solar cells has commenced.",
        beginnerExplainer = "WHAT HAPPENED:\nReliance has begun producing advanced solar power panels at its brand-new mega factory in Gujarat.\n\nWHY IT MATTERS:\nDirect step toward green energy independence and captive power supply for telecom and refining businesses, cutting long-term carbon footprint.\n\nWHO IS AFFECTED:\nDomestic solar supply chains, capital equipment makers, and green hydrogen initiatives.",
        category = EventCategory.DISCLOSURE.name,
        publisher = "NSE Corporate Filing",
        sourceType = "STOCK_EXCHANGE",
        sourceName = "NSE Corporate Filing",
        sourceUrl = "https://www.nseindia.com/companies-listing/corporate-filings",
        entitySymbol = "RELIANCE",
        entityName = "Reliance Industries Ltd.",
        sector = "Energy & Renewables",
        timestamp = now - (220 * 60 * 1000),
        formattedTime = "Yesterday Close",
        materiality = EventMateriality.HIGH.name,
        verificationType = VerificationType.OFFICIAL_SOURCE_VERIFIED.name,
        disclosedValue = "5 GW Solar Capacity",
        filingId = "NSE-CORP-RELIANCE-2026-902"
      ),
      MarketEvent(
        id = "evt_tata_motors_split_06",
        canonicalTitle = "Tata Motors Demerger Scheme Receives NCLT & Shareholder Approval",
        title = "Tata Motors Demerger Scheme Receives NCLT & Shareholder Approval",
        summary = "Tata Motors has received final clearance to separate its Commercial Vehicles (CV) and Passenger Vehicles (PV, including EV and JLR) into two distinct listed entities. The demerger ratio is set at 1:1 for equity shareholders.",
        beginnerExplainer = "WHAT HAPPENED:\nTata Motors is officially splitting into two independent listed companies: one for trucks/buses and one for passenger cars/EVs/Jaguar Land Rover.\n\nWHY IT MATTERS:\nShareholders will automatically receive one share in the new commercial vehicle company for every share they currently hold, unlocking separate business valuation.\n\nWHAT SHOULD I VERIFY:\nReview the record date once notified by the board to plan holdings.",
        category = EventCategory.CORPORATE_ACTION.name,
        publisher = "BSE Corporate Announcements",
        sourceType = "STOCK_EXCHANGE",
        sourceName = "BSE Corporate Announcements",
        sourceUrl = "https://www.bseindia.com/corporates/ann.html",
        entitySymbol = "TATAMOTORS",
        entityName = "Tata Motors Ltd.",
        sector = "Automotive",
        timestamp = now - (300 * 60 * 1000),
        formattedTime = "Yesterday Close",
        materiality = EventMateriality.HIGH.name,
        verificationType = VerificationType.OFFICIAL_SOURCE_VERIFIED.name,
        disclosedValue = "1:1 Share Demerger",
        filingId = "NCLT-MUMBAI-TAMO-2026"
      ),
      MarketEvent(
        id = "evt_crude_oil_global_07",
        canonicalTitle = "Brent Crude Drops 3.2% to $74/bbl on OPEC+ Supply Guidance",
        title = "Brent Crude Drops 3.2% to $74/bbl on OPEC+ Supply Guidance",
        summary = "International crude oil futures declined as OPEC+ signaled planned phase-out of voluntary production cuts alongside subdued demand forecasts from East Asia. Indian OMC refiners and aviation stocks traded higher.",
        beginnerExplainer = "WHAT HAPPENED:\nGlobal crude oil prices fell noticeably due to higher expected supply.\n\nWHY IT MATTERS FOR INDIA:\nIndia imports over 85% of its crude oil. Lower oil prices lower India's import bill, strengthen the Indian Rupee, and improve profit margins for airlines (IndiGo) and paint makers (Asian Paints).\n\nNOTE:\nFinancial media context observation, not a direct corporate disclosure.",
        category = EventCategory.GLOBAL.name,
        publisher = "Financial Media Wire",
        sourceType = "FINANCIAL_PUBLISHER",
        sourceName = "Financial Media Wire",
        sourceUrl = "https://reuters.com/markets/commodities",
        entitySymbol = "MACRO",
        entityName = "Global Energy Markets",
        sector = "Commodities",
        timestamp = now - (360 * 60 * 1000),
        formattedTime = "Overnight Global",
        materiality = EventMateriality.MEDIUM.name,
        verificationType = VerificationType.REPORTED_BY_PUBLISHER.name,
        disclosedValue = "$74.20 / barrel"
      ),
      MarketEvent(
        id = "evt_sebi_mutual_funds_08",
        canonicalTitle = "SEBI Approves 'Mutual Fund Lite' Framework for Passive Index Schemes",
        title = "SEBI Approves 'Mutual Fund Lite' Framework for Passive Index Schemes",
        summary = "SEBI Board has introduced a relaxed regulatory framework ('MF Lite') for entities launching only passive mutual fund schemes (ETFs and Index Funds), lowering net worth requirements and compliance overhead to foster low-cost investing.",
        beginnerExplainer = "WHAT HAPPENED:\nSEBI reduced paperwork and capital requirements for fund houses wanting to launch index funds and ETFs.\n\nWHY IT MATTERS:\nEncourages lower expense ratios for retail investors and invites more specialized fintech asset managers to offer index products.\n\nWHO IS AFFECTED:\nRetail mutual fund investors, passive AMC fund managers, and fintech wealth platforms.",
        category = EventCategory.REGULATORY.name,
        publisher = "SEBI Press Release",
        sourceType = "OFFICIAL_REGULATOR",
        sourceName = "SEBI Press Release",
        sourceUrl = "https://www.sebi.gov.in/media/press-releases/mf-lite-framework.html",
        entitySymbol = "SEBI",
        entityName = "Securities and Exchange Board of India",
        sector = "Capital Markets",
        timestamp = now - (420 * 60 * 1000),
        formattedTime = "Yesterday 04:30 PM",
        materiality = EventMateriality.HIGH.name,
        verificationType = VerificationType.OFFICIAL_SOURCE_VERIFIED.name,
        disclosedValue = "MF Lite Regulatory Relief",
        filingId = "SEBI-PR-2026-031"
      ),
      MarketEvent(
        id = "evt_rbi_mpc_repo_09",
        canonicalTitle = "RBI Monetary Policy Committee Keeps Repo Rate Unchanged at 6.50%",
        title = "RBI Monetary Policy Committee Keeps Repo Rate Unchanged at 6.50%",
        summary = "The Reserve Bank of India MPC voted by majority to maintain the benchmark policy repo rate at 6.50% while shifting stance to 'Neutral', signaling flexibility amid stabilizing headline CPI inflation and robust GDP momentum.",
        beginnerExplainer = "WHAT HAPPENED:\nThe RBI decided not to hike or cut standard interest rates, holding the repo rate steady at 6.50%.\n\nWHY IT MATTERS:\nHome loan EMIs, auto loans, and corporate lending rates remain unchanged for now. The shift to 'Neutral' stance hints that rate cuts may occur in upcoming quarters.\n\nWHO IS AFFECTED:\nBorrowers with floating rate loans, fixed deposit savers, and interest-rate-sensitive stocks (Banks, Real Estate, Auto).",
        category = EventCategory.REGULATORY.name,
        publisher = "RBI Governor Statement",
        sourceType = "OFFICIAL_REGULATOR",
        sourceName = "RBI Governor Statement",
        sourceUrl = "https://www.rbi.org.in/scripts/BS_PressReleaseDisplay.aspx",
        entitySymbol = "RBI",
        entityName = "Reserve Bank of India",
        sector = "Banking & Monetary",
        timestamp = now - (480 * 60 * 1000),
        formattedTime = "Yesterday 11:00 AM",
        materiality = EventMateriality.HIGH.name,
        verificationType = VerificationType.OFFICIAL_SOURCE_VERIFIED.name,
        disclosedValue = "6.50% Repo Rate (Neutral)",
        filingId = "RBI-MPC-OCT-2026"
      )
    )
  }

  // Pre-seed benchmark Indian indices (Explicit DEMO_FIXTURE status until verified refresh)
  private fun getInitialIndices(): List<MarketIndex> {
    return listOf(
      MarketIndex(
        symbol = "NIFTY 50",
        name = "Nifty 50 Index (NSE)",
        currentValue = 23146.15,
        changeValue = 148.80,
        changePercent = 0.65,
        previousClose = 22997.35,
        tradingStatus = "DEMO",
        provider = "DEMO_FIXTURE",
        sparklineJson = "22997.35,23146.15"
      ),
      MarketIndex(
        symbol = "SENSEX",
        name = "BSE SENSEX",
        currentValue = 74729.27,
        changeValue = 413.66,
        changePercent = 0.56,
        previousClose = 74315.61,
        tradingStatus = "DEMO",
        provider = "DEMO_FIXTURE",
        sparklineJson = "74315.61,74729.27"
      ),
      MarketIndex(
        symbol = "BANK NIFTY",
        name = "Nifty Bank Index",
        currentValue = 55844.00,
        changeValue = 33.50,
        changePercent = 0.06,
        previousClose = 55810.50,
        tradingStatus = "DEMO",
        provider = "DEMO_FIXTURE",
        sparklineJson = "55810.50,55844.00"
      ),
      MarketIndex(
        symbol = "NIFTY IT",
        name = "Nifty IT Index",
        currentValue = 31924.10,
        changeValue = 1205.40,
        changePercent = 3.92,
        previousClose = 30718.70,
        tradingStatus = "DEMO",
        provider = "DEMO_FIXTURE",
        sparklineJson = "30718.70,31924.10"
      )
    )
  }

  // Pre-seed top liquid Indian equities
  private fun getInitialQuotes(): List<StockQuote> {
    return listOf(
      StockQuote(
        symbol = "RELIANCE",
        companyName = "Reliance Industries Ltd.",
        sector = "Energy & Telecom",
        currentPrice = 2742.50,
        changePercent = 1.25,
        dayHigh = 2758.00,
        dayLow = 2715.20,
        peRatio = 26.4,
        marketCap = "₹18.55 Lakh Cr",
        sparklineJson = "2715,2720,2735,2730,2745,2740,2758,2742"
      ),
      StockQuote(
        symbol = "TCS",
        companyName = "Tata Consultancy Services",
        sector = "Information Technology",
        currentPrice = 4185.00,
        changePercent = 1.80,
        dayHigh = 4210.00,
        dayLow = 4120.00,
        peRatio = 31.2,
        marketCap = "₹15.14 Lakh Cr",
        sparklineJson = "4120,4135,4150,4165,4170,4190,4210,4185"
      ),
      StockQuote(
        symbol = "HDFCBANK",
        companyName = "HDFC Bank Ltd.",
        sector = "Banking",
        currentPrice = 1682.40,
        changePercent = 0.65,
        dayHigh = 1694.00,
        dayLow = 1670.00,
        peRatio = 18.9,
        marketCap = "₹12.78 Lakh Cr",
        sparklineJson = "1670,1674,1679,1681,1685,1689,1694,1682"
      ),
      StockQuote(
        symbol = "INFY",
        companyName = "Infosys Ltd.",
        sector = "Information Technology",
        currentPrice = 1918.75,
        changePercent = 1.55,
        dayHigh = 1932.00,
        dayLow = 1890.00,
        peRatio = 28.6,
        marketCap = "₹7.96 Lakh Cr",
        sparklineJson = "1890,1898,1908,1912,1920,1928,1932,1918"
      ),
      StockQuote(
        symbol = "ICICIBANK",
        companyName = "ICICI Bank Ltd.",
        sector = "Banking",
        currentPrice = 1264.30,
        changePercent = 0.90,
        dayHigh = 1272.00,
        dayLow = 1251.00,
        peRatio = 17.8,
        marketCap = "₹8.89 Lakh Cr",
        sparklineJson = "1251,1255,1260,1258,1263,1268,1272,1264"
      ),
      StockQuote(
        symbol = "LT",
        companyName = "Larsen & Toubro Ltd.",
        sector = "Infrastructure & EPC",
        currentPrice = 3680.00,
        changePercent = 2.15,
        dayHigh = 3710.00,
        dayLow = 3605.00,
        peRatio = 34.1,
        marketCap = "₹5.06 Lakh Cr",
        sparklineJson = "3605,3620,3645,3660,3675,3690,3710,3680"
      ),
      StockQuote(
        symbol = "TATAMOTORS",
        companyName = "Tata Motors Ltd.",
        sector = "Automotive",
        currentPrice = 964.80,
        changePercent = 1.40,
        dayHigh = 975.00,
        dayLow = 952.00,
        peRatio = 11.5,
        marketCap = "₹3.54 Lakh Cr",
        sparklineJson = "952,956,961,960,967,970,975,964"
      ),
      StockQuote(
        symbol = "SBIN",
        companyName = "State Bank of India",
        sector = "Public Sector Banking",
        currentPrice = 822.15,
        changePercent = 0.45,
        dayHigh = 829.00,
        dayLow = 816.00,
        peRatio = 10.8,
        marketCap = "₹7.34 Lakh Cr",
        sparklineJson = "816,818,820,823,825,827,829,822"
      )
    )
  }
}
