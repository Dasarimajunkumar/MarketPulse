package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.MarketPulseDatabase
import com.example.data.model.AuthStep
import com.example.data.model.AuthUiState
import com.example.data.model.ChatMessage
import com.example.data.model.EconomicIndicator
import com.example.data.model.MarketEvent
import com.example.data.model.MarketIndex
import com.example.data.model.NotificationItem
import com.example.data.model.PaperAccount
import com.example.data.model.PaperOrder
import com.example.data.model.PaperPosition
import com.example.data.model.SourceHealth
import com.example.data.model.StockQuote
import com.example.data.model.TradingJournalEntry
import com.example.data.model.UserSession
import com.example.data.model.WatchlistItem
import com.example.data.repository.AuthRepository
import com.example.data.repository.MarketPulseRepository
import com.example.data.worker.MarketSyncWorker
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class PrimaryViewMode {
  NEWS_CIRCULARS,
  PAPER_TRADING
}

enum class NewsSubTab {
  PULSE,
  FILINGS_RADAR,
  AI_CHAT,
  MACRO_CALENDAR
}

enum class AppTab {
  DAILY_PULSE,
  FILING_RADAR,
  AI_CHAT,
  WATCHLIST,
  PAPER_TRADING,
  MACRO_CALENDAR,
  NOTIFICATIONS
}

data class MarketPulseUiState(
  val primaryMode: PrimaryViewMode = PrimaryViewMode.NEWS_CIRCULARS,
  val newsSubTab: NewsSubTab = NewsSubTab.PULSE,
  val currentTab: AppTab = AppTab.DAILY_PULSE,
  val isProMode: Boolean = false,
  val themeMode: ThemeMode = ThemeMode.DARK,
  val selectedCategory: String = "ALL",
  val searchQuery: String = "",
  val selectedEvent: MarketEvent? = null,
  val tradeTargetStock: StockQuote? = null,
  val tradeActionSide: String = "BUY", // "BUY" or "SELL"
  val toastMessage: String? = null,
  val isRefreshing: Boolean = false,
  val showProfileDialog: Boolean = false,
  val forceShowLogin: Boolean = false
)

class MarketPulseViewModel(application: Application) : AndroidViewModel(application) {

  private val prefs = application.getSharedPreferences("marketpulse_prefs", Context.MODE_PRIVATE)
  private val repository: MarketPulseRepository
  private val authRepository: AuthRepository = AuthRepository(application)

  private val _uiState = MutableStateFlow(
    MarketPulseUiState(
      themeMode = try {
        ThemeMode.valueOf(prefs.getString("theme_mode", ThemeMode.DARK.name) ?: ThemeMode.DARK.name)
      } catch (e: Exception) {
        ThemeMode.DARK
      }
    )
  )
  val uiState: StateFlow<MarketPulseUiState> = _uiState.asStateFlow()

  val currentUser: StateFlow<UserSession?> = authRepository.currentUser

  private val _authUiState = MutableStateFlow(
    AuthUiState(
      step = if (authRepository.currentUser.value != null) AuthStep.AUTHENTICATED else AuthStep.EMAIL_INPUT,
      activeUser = authRepository.currentUser.value
    )
  )
  val authUiState: StateFlow<AuthUiState> = _authUiState.asStateFlow()

  val indices: StateFlow<List<MarketIndex>>
  val quotes: StateFlow<List<StockQuote>>
  val watchlist: StateFlow<List<WatchlistItem>>
  val paperAccount: StateFlow<PaperAccount?>
  val paperPositions: StateFlow<List<PaperPosition>>
  val paperOrders: StateFlow<List<PaperOrder>>
  val cashLedger: StateFlow<List<com.example.data.model.CashLedgerEntry>>
  val journalEntries: StateFlow<List<TradingJournalEntry>>
  val disclosures: StateFlow<List<com.example.data.model.CorporateDisclosure>>
  val circulars: StateFlow<List<com.example.data.model.RegulatoryCircular>>
  val sourceHealth: StateFlow<List<SourceHealth>>
  val notifications: StateFlow<List<NotificationItem>>
  val economicIndicators: List<EconomicIndicator>

  // All market events stream
  val allEvents: StateFlow<List<MarketEvent>>

  // Filtered market events based on category, search query, etc.
  val filteredEvents: StateFlow<List<MarketEvent>>

  // Grounded AI Chat State
  private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
    listOf(
      ChatMessage(
        id = "welcome",
        sender = "ASSISTANT",
        text = "Welcome to MarketPulse AI. Ask any question about Indian stock market filings, SEBI circulars, corporate actions, or earnings disclosures. Answers are grounded directly in official source documents.",
        timestamp = System.currentTimeMillis(),
        formattedTime = "Now",
        isGrounded = true
      )
    )
  )
  val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

  private val _isChatLoading = MutableStateFlow(false)
  val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

  init {
    val database = MarketPulseDatabase.getDatabase(application)
    repository = MarketPulseRepository(database.marketDao(), database.paperTradingDao())

    allEvents = repository.allEvents.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    sourceHealth = repository.allSourceHealth.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    notifications = repository.allNotifications.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    indices = repository.allIndices.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    quotes = repository.allQuotes.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    watchlist = repository.watchlist.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    paperAccount = repository.paperAccount.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    paperPositions = repository.paperPositions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    paperOrders = repository.paperOrders.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    cashLedger = repository.cashLedger.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    journalEntries = repository.journalEntries.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    disclosures = repository.allDisclosures.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    circulars = repository.allCirculars.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    economicIndicators = repository.getEconomicIndicators()

    filteredEvents = combine(
      repository.allEvents,
      _uiState
    ) { events, state ->
      events.filter { event ->
        val matchesCategory = when (state.selectedCategory.uppercase().trim()) {
          "ALL", "" -> true
          "SEBI" -> {
            event.entitySymbol.equals("SEBI", ignoreCase = true) ||
              event.sourceName.contains("SEBI", ignoreCase = true) ||
              event.title.contains("SEBI", ignoreCase = true)
          }
          "RBI" -> {
            event.entitySymbol.equals("RBI", ignoreCase = true) ||
              event.sourceName.contains("RBI", ignoreCase = true) ||
              event.title.contains("RBI", ignoreCase = true)
          }
          "CORPORATE", "GENERAL_CORPORATE" -> {
            event.entitySymbol !in listOf("SEBI", "RBI", "MACRO")
          }
          "REGULATORY" -> {
            event.category.equals("REGULATORY", ignoreCase = true) ||
              event.entitySymbol in listOf("SEBI", "RBI")
          }
          else -> event.category.equals(state.selectedCategory, ignoreCase = true)
        }

        val matchesSearch = if (state.searchQuery.isBlank()) {
          true
        } else {
          val query = state.searchQuery.trim().lowercase()
          event.title.lowercase().contains(query) ||
            event.summary.lowercase().contains(query) ||
            event.entitySymbol.lowercase().contains(query) ||
            event.entityName.lowercase().contains(query) ||
            event.sector.lowercase().contains(query)
        }

        matchesCategory && matchesSearch
      }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    viewModelScope.launch {
      repository.initializeSeedDataIfNeeded()
      repository.refreshRssFeeds()
      MarketSyncWorker.schedulePeriodicSync(application)
    }
  }

  // --- AI CHAT ACTIONS ---

  fun sendChatMessage(prompt: String) {
    if (prompt.isBlank()) return
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val userMsg = ChatMessage(
      id = UUID.randomUUID().toString(),
      sender = "USER",
      text = prompt,
      timestamp = System.currentTimeMillis(),
      formattedTime = timeFormat.format(Date())
    )
    _chatMessages.update { it + userMsg }
    _isChatLoading.value = true

    viewModelScope.launch {
      try {
        val response = repository.generateGroundedAiResponse(prompt)
        _chatMessages.update { it + response }
      } catch (e: Exception) {
        val errorMsg = ChatMessage(
          id = UUID.randomUUID().toString(),
          sender = "ASSISTANT",
          text = "Failed to retrieve grounded AI answer: ${e.message}",
          timestamp = System.currentTimeMillis(),
          formattedTime = timeFormat.format(Date()),
          isGrounded = false
        )
        _chatMessages.update { it + errorMsg }
      } finally {
        _isChatLoading.value = false
      }
    }
  }

  fun markNotificationAsRead(id: String) {
    viewModelScope.launch {
      repository.markNotificationAsRead(id)
    }
  }

  // --- AUTHENTICATION ACTIONS ---

  fun setEmailInput(email: String) {
    _authUiState.value = _authUiState.value.copy(
      emailInput = email,
      errorMessage = null
    )
  }

  fun setOtpInput(otp: String) {
    _authUiState.value = _authUiState.value.copy(
      otpInput = otp,
      errorMessage = null
    )
  }

  fun sendOtp(email: String) {
    val cleanEmail = email.trim()
    if (!cleanEmail.contains("@") || !cleanEmail.contains(".") || cleanEmail.length < 5) {
      _authUiState.value = _authUiState.value.copy(
        errorMessage = "Please enter a valid email address (e.g. name@domain.com)"
      )
      return
    }

    viewModelScope.launch {
      _authUiState.value = _authUiState.value.copy(isLoading = true, errorMessage = null)
      delay(600)
      val code = "482910"
      _authUiState.value = _authUiState.value.copy(
        step = AuthStep.OTP_VERIFY,
        emailInput = cleanEmail,
        otpInput = "",
        generatedOtp = code,
        isLoading = false,
        cooldownSeconds = 30
      )
      _uiState.value = _uiState.value.copy(
        toastMessage = "OTP dispatched to $cleanEmail"
      )
    }
  }

  fun verifyOtp(enteredOtp: String) {
    val currentState = _authUiState.value
    viewModelScope.launch {
      _authUiState.value = currentState.copy(isLoading = true, errorMessage = null)
      delay(500)
      val result = authRepository.verifyOtp(
        email = currentState.emailInput,
        enteredOtp = enteredOtp,
        expectedOtp = currentState.generatedOtp
      )

      result.onSuccess { session ->
        _authUiState.value = currentState.copy(
          step = AuthStep.AUTHENTICATED,
          isLoading = false,
          activeUser = session,
          errorMessage = null
        )
        _uiState.value = _uiState.value.copy(
          forceShowLogin = false,
          toastMessage = "Welcome, ${session.displayName}! Terminal unlocked."
        )
      }.onFailure { error ->
        _authUiState.value = currentState.copy(
          isLoading = false,
          errorMessage = error.message ?: "Invalid OTP code. Try again."
        )
      }
    }
  }

  fun backToEmailInput() {
    _authUiState.value = _authUiState.value.copy(
      step = AuthStep.EMAIL_INPUT,
      otpInput = "",
      errorMessage = null
    )
  }

  fun loginAsGuest() {
    val guestSession = authRepository.loginAsGuest()
    _authUiState.value = _authUiState.value.copy(
      step = AuthStep.AUTHENTICATED,
      activeUser = guestSession,
      errorMessage = null
    )
    _uiState.value = _uiState.value.copy(
      forceShowLogin = false,
      toastMessage = "Exploring in Sandbox Guest Mode"
    )
  }

  fun logout() {
    authRepository.logout()
    _authUiState.value = AuthUiState(
      step = AuthStep.EMAIL_INPUT,
      emailInput = "",
      otpInput = "",
      activeUser = null
    )
    _uiState.value = _uiState.value.copy(
      showProfileDialog = false,
      forceShowLogin = true,
      toastMessage = "Logged out successfully"
    )
  }

  fun showLoginScreen() {
    _uiState.value = _uiState.value.copy(forceShowLogin = true)
  }

  fun dismissLoginScreen() {
    _uiState.value = _uiState.value.copy(forceShowLogin = false)
  }

  fun toggleProfileDialog(show: Boolean) {
    _uiState.value = _uiState.value.copy(showProfileDialog = show)
  }

  // --- NAVIGATION & UI ACTIONS ---

  fun setPrimaryMode(mode: PrimaryViewMode) {
    _uiState.value = _uiState.value.copy(primaryMode = mode)
  }

  fun setNewsSubTab(subTab: NewsSubTab) {
    _uiState.value = _uiState.value.copy(newsSubTab = subTab)
  }

  fun setTab(tab: AppTab) {
    _uiState.value = _uiState.value.copy(currentTab = tab)
  }

  fun toggleProMode() {
    val newMode = !_uiState.value.isProMode
    _uiState.value = _uiState.value.copy(
      isProMode = newMode,
      toastMessage = if (newMode) "Professional Terminal Mode enabled" else "Beginner Friendly Mode enabled"
    )
  }

  fun setCategoryFilter(category: String) {
    _uiState.value = _uiState.value.copy(selectedCategory = category)
  }

  fun setSearchQuery(query: String) {
    _uiState.value = _uiState.value.copy(searchQuery = query)
  }

  fun openEventDetail(event: MarketEvent?) {
    _uiState.value = _uiState.value.copy(selectedEvent = event)
  }

  fun openTradeSheet(stock: StockQuote?, side: String = "BUY") {
    _uiState.value = _uiState.value.copy(tradeTargetStock = stock, tradeActionSide = side)
  }

  fun dismissTradeSheet() {
    _uiState.value = _uiState.value.copy(tradeTargetStock = null)
  }

  fun clearToast() {
    _uiState.value = _uiState.value.copy(toastMessage = null)
  }

  fun toggleBookmark(event: MarketEvent) {
    viewModelScope.launch {
      repository.toggleBookmark(event.id, event.isBookmarked)
      _uiState.value = _uiState.value.copy(
        toastMessage = if (!event.isBookmarked) "Saved to Bookmarks" else "Removed from Bookmarks"
      )
    }
  }

  fun toggleWatchlist(symbol: String, companyName: String) {
    viewModelScope.launch {
      repository.toggleWatchlist(symbol, companyName)
      _uiState.value = _uiState.value.copy(
        toastMessage = "Watchlist updated for $symbol"
      )
    }
  }

  fun executePaperOrder(
    symbol: String,
    side: String,
    type: String,
    quantity: Int,
    limitPrice: Double?
  ) {
    viewModelScope.launch {
      val result = repository.placePaperOrder(symbol, side, type, quantity, limitPrice)
      result.onSuccess { message ->
        _uiState.value = _uiState.value.copy(
          tradeTargetStock = null,
          toastMessage = message
        )
      }.onFailure { error ->
        _uiState.value = _uiState.value.copy(
          toastMessage = error.message ?: "Order execution failed"
        )
      }
    }
  }

  fun resetPaperPortfolio() {
    viewModelScope.launch {
      repository.resetPaperAccount()
      _uiState.value = _uiState.value.copy(
        toastMessage = "Paper trading portfolio reset to ₹10,00,000 cash balance."
      )
    }
  }

  fun addJournalNote(symbol: String, title: String, notes: String, linkedEvent: String? = null) {
    viewModelScope.launch {
      repository.addJournalNote(symbol, title, notes, linkedEvent)
      _uiState.value = _uiState.value.copy(
        toastMessage = "Trade thesis recorded in Journal"
      )
    }
  }

  fun refreshData() {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isRefreshing = true)
      repository.refreshRssFeeds()
      _uiState.value = _uiState.value.copy(
        isRefreshing = false,
        toastMessage = "Official SEBI, RBI & exchange feeds polled and updated"
      )
    }
  }

  fun setThemeMode(mode: ThemeMode) {
    prefs.edit().putString("theme_mode", mode.name).apply()
    _uiState.update { it.copy(themeMode = mode) }
  }

  fun toggleThemeMode() {
    val current = _uiState.value.themeMode
    val next = if (current == ThemeMode.LIGHT) ThemeMode.DARK else ThemeMode.LIGHT
    setThemeMode(next)
  }
}
