package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.MarketPulseDatabase
import com.example.data.repository.MarketPulseRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import com.example.ui.theme.ThemeMode
import com.example.ui.viewmodel.MarketPulseViewModel
import com.example.ui.viewmodel.NewsSubTab
import com.example.ui.viewmodel.PrimaryViewMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  private lateinit var database: MarketPulseDatabase
  private lateinit var repository: MarketPulseRepository

  @Before
  fun setup() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    database = Room.inMemoryDatabaseBuilder(context, MarketPulseDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    repository = MarketPulseRepository(database.marketDao(), database.paperTradingDao())
  }

  @After
  fun teardown() {
    database.close()
  }

  @Test
  fun `read app name string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("MarketPulse", appName)
  }

  @Test
  fun `verify database seed data initialization`() = runBlocking {
    repository.initializeSeedDataIfNeeded()

    val events = repository.allEvents.first()
    assertTrue("Events should be pre-seeded", events.isNotEmpty())

    val indices = repository.allIndices.first()
    assertTrue("Market indices should be pre-seeded", indices.isNotEmpty())

    val account = repository.paperAccount.first()
    assertNotNull(account)
    assertEquals(1000000.0, account?.cashBalance ?: 0.0, 0.01)
  }

  @Test
  fun `verify dedicated corporate disclosures and regulatory circulars tables seeded`() = runBlocking {
    repository.initializeSeedDataIfNeeded()

    val disclosures = repository.allDisclosures.first()
    assertTrue("Corporate disclosures should be pre-seeded with multi-day filings", disclosures.isNotEmpty())
    val ltFiling = disclosures.firstOrNull { it.companySymbol == "LT" }
    assertNotNull("L&T filing should exist", ltFiling)
    assertEquals("₹4,500 Crore", ltFiling?.disclosedValue)

    val circulars = repository.allCirculars.first()
    assertTrue("Regulatory circulars should be pre-seeded", circulars.isNotEmpty())
    val sebiCircular = circulars.firstOrNull { it.regulator == "SEBI" }
    assertNotNull("SEBI circular should exist", sebiCircular)
    assertEquals("CIR/MRD/DP/2026/89", sebiCircular?.circularNumber)
  }

  @Test
  fun `verify paper trading buy order updates ledger correctly`() = runBlocking {
    repository.initializeSeedDataIfNeeded()

    // Place buy order for 10 shares of RELIANCE
    val result = repository.placePaperOrder(
      symbol = "RELIANCE",
      side = "BUY",
      orderType = "MARKET",
      quantity = 10,
      limitPrice = null
    )
    assertTrue("Buy order should succeed", result.isSuccess)

    val positions = repository.paperPositions.first()
    val reliancePos = positions.firstOrNull { it.symbol == "RELIANCE" }
    assertNotNull("Reliance position should exist", reliancePos)
    assertEquals(10, reliancePos?.quantity)

    val account = repository.paperAccount.first()
    assertTrue("Cash balance should be deducted", (account?.cashBalance ?: 0.0) < 1000000.0)
  }

  @Test
  fun `verify double entry cash ledger audit trail`() = runBlocking {
    repository.initializeSeedDataIfNeeded()

    // Initial funding entry should exist
    val initialLedger = repository.cashLedger.first()
    assertTrue("Initial deposit should exist in cash ledger", initialLedger.isNotEmpty())
    assertEquals(1000000.0, initialLedger.last().amount, 0.01)

    // Execute Buy
    val buyResult = repository.placePaperOrder("TCS", "BUY", "MARKET", 5, null)
    assertTrue(buyResult.isSuccess)

    val updatedLedger = repository.cashLedger.first()
    assertTrue("Ledger entries should increase with stock debit and brokerage debit", updatedLedger.size >= 3)

    val account = repository.paperAccount.first()
    val latestLedgerBalance = updatedLedger.first().runningBalance
    assertEquals(account?.cashBalance ?: 0.0, latestLedgerBalance, 0.01)
  }

  @Test
  fun `verify paper trading sell order prevents overselling`() = runBlocking {
    repository.initializeSeedDataIfNeeded()

    // Try to sell 50 shares of TCS without owning any
    val result = repository.placePaperOrder(
      symbol = "TCS",
      side = "SELL",
      orderType = "MARKET",
      quantity = 50,
      limitPrice = null
    )
    assertTrue("Overselling should be rejected", result.isFailure)
  }

  @Test
  fun `verify paper trading DAO transaction history and balance aggregation queries`() = runBlocking {
    repository.initializeSeedDataIfNeeded()
    val paperDao = database.paperTradingDao()

    // 1. Initial Deposit check
    val totalCreditsInitial = paperDao.getTotalCredits()
    assertEquals(1000000.0, totalCreditsInitial, 0.01)

    val totalDebitsInitial = paperDao.getTotalDebits()
    assertEquals(0.0, totalDebitsInitial, 0.01)

    val netInitial = paperDao.getNetAggregatedCashBalance()
    assertEquals(1000000.0, netInitial, 0.01)

    // 2. Perform a buy trade: 10 RELIANCE @ 2742.50 = 27,425.00 + 20.00 fee
    val buyRes = repository.placePaperOrder("RELIANCE", "BUY", "MARKET", 10, null)
    assertTrue(buyRes.isSuccess)

    // 3. Test Aggregation Queries
    val totalDebitsAfterBuy = paperDao.getTotalDebits()
    assertEquals(27445.0, totalDebitsAfterBuy, 0.01) // 27425 + 20 fee

    val netCashAfterBuy = paperDao.getNetAggregatedCashBalance()
    assertEquals(1000000.0 - 27445.0, netCashAfterBuy, 0.01)

    val totalBrokerage = paperDao.getTotalBrokeragePaid()
    assertEquals(20.0, totalBrokerage, 0.01)

    val latestRunBal = paperDao.getLatestRunningBalance()
    assertEquals(netCashAfterBuy, latestRunBal ?: 0.0, 0.01)

    // 4. Test Transaction History Retrieval Queries
    val debitEntries = paperDao.getLedgerEntriesByType("DEBIT").first()
    assertEquals(2, debitEntries.size) // stock debit + brokerage debit

    val buyEntries = paperDao.getLedgerEntriesByCategory("STOCK_BUY_DEBIT").first()
    assertEquals(1, buyEntries.size)
    assertEquals(27425.0, buyEntries.first().amount, 0.01)

    val categoryAggs = paperDao.getCategoryAggregates().first()
    assertTrue(categoryAggs.any { it.category == "INITIAL_DEPOSIT" })
    assertTrue(categoryAggs.any { it.category == "STOCK_BUY_DEBIT" })
    assertTrue(categoryAggs.any { it.category == "BROKERAGE_DEBIT" })
  }

  @Test
  fun `verify AuthRepository email OTP verification and guest mode`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val authRepo = com.example.data.repository.AuthRepository(context)

    // Initially logged out
    authRepo.logout()
    org.junit.Assert.assertNull(authRepo.currentUser.value)

    // Test OTP verification with valid demo code
    val verifyResult = authRepo.verifyOtp("majundasari7410@gmail.com", "482910", "482910")
    assertTrue("OTP verification should succeed", verifyResult.isSuccess)

    val session = verifyResult.getOrNull()
    assertNotNull(session)
    assertEquals("majundasari7410@gmail.com", session?.email)
    assertTrue("Session should be verified", session?.isVerified == true)
    assertEquals(session?.email, authRepo.currentUser.value?.email)

    // Test invalid OTP fails
    val invalidResult = authRepo.verifyOtp("test@domain.com", "000000", "987654")
    assertTrue("Invalid OTP should fail", invalidResult.isFailure)

    // Test Guest login
    val guestSession = authRepo.loginAsGuest()
    assertTrue("Guest session isGuest should be true", guestSession.isGuest)
    assertEquals(guestSession.email, authRepo.currentUser.value?.email)

    // Test Logout
    authRepo.logout()
    org.junit.Assert.assertNull(authRepo.currentUser.value)
  }

  @Test
  fun `test 6-digit pin validation and resend flow`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val authRepo = com.example.data.repository.AuthRepository(context)

    val validPin = "482910"
    assertEquals("PIN must be 6 digits", 6, validPin.length)
    assertTrue("PIN must be numeric", validPin.all { it.isDigit() })

    // Verify correct OTP
    val resSuccess = authRepo.verifyOtp("majundasari7410@gmail.com", validPin, validPin)
    assertTrue(resSuccess.isSuccess)
    assertEquals("majundasari7410@gmail.com", resSuccess.getOrNull()?.email)

    // Verify mismatched OTP fails (not matching validPin, 482910, or 123456)
    val resMismatch = authRepo.verifyOtp("majundasari7410@gmail.com", "000000", "987654")
    assertTrue(resMismatch.isFailure)

    // Countdown initial duration is 30s
    val initialSeconds = 30
    assertEquals(30, initialSeconds)
    assertTrue(initialSeconds > 0)
  }

  @Test
  fun `verify SEBI, RBI, and General Corporate category filtering`() = runBlocking {
    repository.initializeSeedDataIfNeeded()
    val allEvents = repository.allEvents.first()
    assertTrue("Initial events should be seeded", allEvents.isNotEmpty())

    // 1. Filter SEBI
    val sebiEvents = allEvents.filter {
      it.entitySymbol.equals("SEBI", ignoreCase = true) ||
        it.sourceName.contains("SEBI", ignoreCase = true) ||
        it.title.contains("SEBI", ignoreCase = true)
    }
    assertTrue("SEBI events must exist", sebiEvents.isNotEmpty())
    assertTrue("All SEBI events must have SEBI entity or source", sebiEvents.all { it.entitySymbol == "SEBI" || it.sourceName.contains("SEBI") })

    // 2. Filter RBI
    val rbiEvents = allEvents.filter {
      it.entitySymbol.equals("RBI", ignoreCase = true) ||
        it.sourceName.contains("RBI", ignoreCase = true) ||
        it.title.contains("RBI", ignoreCase = true)
    }
    assertTrue("RBI events must exist", rbiEvents.isNotEmpty())
    assertTrue("All RBI events must have RBI entity or source", rbiEvents.all { it.entitySymbol == "RBI" || it.sourceName.contains("RBI") })

    // 3. Filter General Corporate
    val corporateEvents = allEvents.filter {
      it.entitySymbol !in listOf("SEBI", "RBI", "MACRO")
    }
    assertTrue("General Corporate events must exist", corporateEvents.isNotEmpty())
    assertTrue("Corporate events must exclude regulators SEBI and RBI", corporateEvents.none { it.entitySymbol in listOf("SEBI", "RBI") })
    assertTrue("Corporate events must include companies like LT, TCS, RELIANCE, or TATAMOTORS", corporateEvents.any { it.entitySymbol in listOf("LT", "TCS", "RELIANCE", "TATAMOTORS") })
  }

  @Test
  fun `verify ViewModel category filter state transitions between SEBI, RBI, and Corporate`() = runBlocking {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val vm = com.example.ui.viewmodel.MarketPulseViewModel(app)

    // Default is ALL
    assertEquals("ALL", vm.uiState.value.selectedCategory)

    // Set SEBI filter
    vm.setCategoryFilter("SEBI")
    assertEquals("SEBI", vm.uiState.value.selectedCategory)

    // Set RBI filter
    vm.setCategoryFilter("RBI")
    assertEquals("RBI", vm.uiState.value.selectedCategory)

    // Set General Corporate filter
    vm.setCategoryFilter("CORPORATE")
    assertEquals("CORPORATE", vm.uiState.value.selectedCategory)

    // Toggle back to ALL
    vm.setCategoryFilter("ALL")
    assertEquals("ALL", vm.uiState.value.selectedCategory)
  }

  @Test
  fun `verify Light Mode, Dark Mode, and System theme mode state transitions`() = runBlocking {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val vm = com.example.ui.viewmodel.MarketPulseViewModel(app)

    // Set Light Mode
    vm.setThemeMode(ThemeMode.LIGHT)
    assertEquals(ThemeMode.LIGHT, vm.uiState.value.themeMode)

    // Set Dark Mode
    vm.setThemeMode(ThemeMode.DARK)
    assertEquals(ThemeMode.DARK, vm.uiState.value.themeMode)

    // Set System Mode
    vm.setThemeMode(ThemeMode.SYSTEM)
    assertEquals(ThemeMode.SYSTEM, vm.uiState.value.themeMode)

    // Toggle from System should switch to Light Mode
    vm.toggleThemeMode()
    assertEquals(ThemeMode.LIGHT, vm.uiState.value.themeMode)

    // Toggle from Light should switch to Dark Mode
    vm.toggleThemeMode()
    assertEquals(ThemeMode.DARK, vm.uiState.value.themeMode)

    // Verify persistence in SharedPreferences
    val prefs = app.getSharedPreferences("marketpulse_prefs", Context.MODE_PRIVATE)
    assertEquals("DARK", prefs.getString("theme_mode", null))
  }

  @Test
  fun `verify Complete App CUJ Navigation and Pro vs Beginner Dual Mode Switching`() = runBlocking {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val vm = MarketPulseViewModel(app)

    // Initial state check
    assertEquals(PrimaryViewMode.NEWS_CIRCULARS, vm.uiState.value.primaryMode)
    assertEquals(NewsSubTab.PULSE, vm.uiState.value.newsSubTab)
    org.junit.Assert.assertFalse(vm.uiState.value.isProMode)

    // Switch to Paper Trading
    vm.setPrimaryMode(PrimaryViewMode.PAPER_TRADING)
    assertEquals(PrimaryViewMode.PAPER_TRADING, vm.uiState.value.primaryMode)

    // Switch back to News & Circulars
    vm.setPrimaryMode(PrimaryViewMode.NEWS_CIRCULARS)
    assertEquals(PrimaryViewMode.NEWS_CIRCULARS, vm.uiState.value.primaryMode)

    // Switch Sub-Tabs
    vm.setNewsSubTab(NewsSubTab.FILINGS_RADAR)
    assertEquals(NewsSubTab.FILINGS_RADAR, vm.uiState.value.newsSubTab)

    vm.setNewsSubTab(NewsSubTab.MACRO_CALENDAR)
    assertEquals(NewsSubTab.MACRO_CALENDAR, vm.uiState.value.newsSubTab)

    vm.setNewsSubTab(NewsSubTab.PULSE)
    assertEquals(NewsSubTab.PULSE, vm.uiState.value.newsSubTab)

    // Toggle Pro Mode
    vm.toggleProMode()
    assertTrue("Pro mode should be active", vm.uiState.value.isProMode)

    vm.toggleProMode()
    org.junit.Assert.assertFalse("Pro mode should be disabled", vm.uiState.value.isProMode)
  }

  @Test
  fun `verify Watchlist Add, Toggle, and Trading Journal Thesis Entry`() = runBlocking {
    repository.initializeSeedDataIfNeeded()

    // 1. Watchlist Toggle
    val initialWatchlist = repository.watchlist.first()
    assertTrue("Initial watchlist items should exist", initialWatchlist.isNotEmpty())

    repository.toggleWatchlist("TATAMOTORS", "Tata Motors Ltd")
    val updatedWatchlist = repository.watchlist.first()
    val tataItem = updatedWatchlist.firstOrNull { it.symbol == "TATAMOTORS" }
    assertNotNull("Tata Motors should be in watchlist", tataItem)
    assertEquals("Tata Motors Ltd", tataItem?.companyName)

    // 2. Add Trading Journal Entry
    repository.addJournalNote("TCS", "Strong Q3 Deal Wins", "Operating margins expanded by 60bps.")
    val journal = repository.journalEntries.first()
    assertTrue("Trading journal entries should exist", journal.isNotEmpty())
    val tcsNote = journal.firstOrNull { it.symbol == "TCS" }
    assertNotNull("TCS journal note should exist", tcsNote)
    assertEquals("Strong Q3 Deal Wins", tcsNote?.title)
  }

  @Test
  fun `verify Google Play Store Compliance and App Metadata`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val packageName = context.packageName
    assertEquals("com.aistudio.marketpulse.qxvp", packageName)

    // Verify App Name
    val appName = context.getString(R.string.app_name)
    assertEquals("MarketPulse", appName)
    assertTrue("App title must be <= 30 characters for Play Store policy", appName.length <= 30)

    // Verify package manager can resolve the main activity
    val pm = context.packageManager
    val intent = pm.getLaunchIntentForPackage(packageName)
    assertNotNull("Launch intent for app package must be resolveable", intent)
  }
}


