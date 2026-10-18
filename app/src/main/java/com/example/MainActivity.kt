package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.EventDetailDialog
import com.example.ui.components.MarketHeader
import com.example.ui.components.PaperOrderSheet
import com.example.ui.components.UserProfileDialog
import com.example.ui.screens.AiChatScreen
import com.example.ui.screens.DailyPulseScreen
import com.example.ui.screens.FilingRadarScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MacroEconomicScreen
import com.example.ui.screens.PaperTradingScreen
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CyanTeal
import com.example.ui.theme.MarketPulseTheme
import com.example.ui.theme.SlateNavySurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MarketPulseViewModel
import com.example.ui.viewmodel.NewsSubTab
import com.example.ui.viewmodel.PrimaryViewMode
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val viewModel: MarketPulseViewModel = viewModel()
      val uiState by viewModel.uiState.collectAsStateWithLifecycle()
      MarketPulseTheme(themeMode = uiState.themeMode) {
        MarketPulseApp(viewModel = viewModel)
      }
    }
  }
}

@Composable
fun MarketPulseApp(viewModel: MarketPulseViewModel = viewModel()) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
  val authUiState by viewModel.authUiState.collectAsStateWithLifecycle()
  val indices by viewModel.indices.collectAsStateWithLifecycle()
  val quotes by viewModel.quotes.collectAsStateWithLifecycle()
  val paperAccount by viewModel.paperAccount.collectAsStateWithLifecycle()
  val paperPositions by viewModel.paperPositions.collectAsStateWithLifecycle()
  val paperOrders by viewModel.paperOrders.collectAsStateWithLifecycle()
  val cashLedger by viewModel.cashLedger.collectAsStateWithLifecycle()
  val journalEntries by viewModel.journalEntries.collectAsStateWithLifecycle()
  val disclosures by viewModel.disclosures.collectAsStateWithLifecycle()
  val circulars by viewModel.circulars.collectAsStateWithLifecycle()
  val filteredEvents by viewModel.filteredEvents.collectAsStateWithLifecycle()
  val allEvents by viewModel.allEvents.collectAsStateWithLifecycle()
  val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
  val isChatLoading by viewModel.isChatLoading.collectAsStateWithLifecycle()

  // If not authenticated or login explicitly opened, show LoginScreen
  if (currentUser == null || uiState.forceShowLogin) {
    LoginScreen(
      authStep = authUiState.step,
      emailInput = authUiState.emailInput,
      otpInput = authUiState.otpInput,
      generatedOtp = authUiState.generatedOtp,
      isLoading = authUiState.isLoading,
      errorMessage = authUiState.errorMessage,
      onEmailChanged = { viewModel.setEmailInput(it) },
      onOtpChanged = { viewModel.setOtpInput(it) },
      onRequestOtp = { viewModel.sendOtp(it) },
      onVerifyOtp = { viewModel.verifyOtp(it) },
      onBackToEmail = { viewModel.backToEmailInput() },
      onGuestLogin = { viewModel.loginAsGuest() },
      onToggleTheme = { viewModel.toggleThemeMode() },
      canDismiss = currentUser != null && uiState.forceShowLogin,
      onDismiss = { viewModel.dismissLoginScreen() }
    )
    return
  }

  // Preserved scroll states across dual views and tabs
  val dailyPulseListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
  val filingRadarListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
  val paperTradingListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }

  // Handle system back navigation to return to News Pulse from Paper Trading or sub-tabs
  BackHandler(enabled = uiState.primaryMode != PrimaryViewMode.NEWS_CIRCULARS || uiState.newsSubTab != NewsSubTab.PULSE) {
    if (uiState.primaryMode == PrimaryViewMode.PAPER_TRADING) {
      viewModel.setPrimaryMode(PrimaryViewMode.NEWS_CIRCULARS)
    } else {
      viewModel.setNewsSubTab(NewsSubTab.PULSE)
    }
  }

  // Toast Auto-Dismiss
  LaunchedEffect(uiState.toastMessage) {
    if (uiState.toastMessage != null) {
      delay(3000)
      viewModel.clearToast()
    }
  }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    containerColor = MaterialTheme.colorScheme.background,
    contentWindowInsets = WindowInsets(0, 0, 0, 0),
    topBar = {
      MarketHeader(
        indices = indices,
        primaryMode = uiState.primaryMode,
        onSelectPrimaryMode = { viewModel.setPrimaryMode(it) },
        isProMode = uiState.isProMode,
        onToggleMode = { viewModel.toggleProMode() },
        onRefresh = { viewModel.refreshData() },
        themeMode = uiState.themeMode,
        onToggleTheme = { viewModel.toggleThemeMode() },
        currentUser = currentUser,
        onProfileClick = {
          if (currentUser != null) {
            viewModel.toggleProfileDialog(true)
          } else {
            viewModel.showLoginScreen()
          }
        },
        modifier = Modifier.testTag("market_header")
      )
    }
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      Box(modifier = Modifier.fillMaxSize()) {
        val isNewsMode = uiState.primaryMode == PrimaryViewMode.NEWS_CIRCULARS
        val isPaperMode = uiState.primaryMode == PrimaryViewMode.PAPER_TRADING

        // 1. NEWS & CIRCULARS VIEW
        Box(
          modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
              alpha = if (isNewsMode) 1f else 0f
              translationX = if (isNewsMode) 0f else -10000f
            }
        ) {
          Column(modifier = Modifier.fillMaxSize()) {
            NewsSubTabBar(
              activeSubTab = uiState.newsSubTab,
              onSelectSubTab = { viewModel.setNewsSubTab(it) }
            )

            Spacer(modifier = Modifier.height(4.dp))

            when (uiState.newsSubTab) {
              NewsSubTab.PULSE -> {
                DailyPulseScreen(
                  events = filteredEvents,
                  allEvents = allEvents,
                  isProMode = uiState.isProMode,
                  selectedCategory = uiState.selectedCategory,
                  searchQuery = uiState.searchQuery,
                  onCategorySelected = { viewModel.setCategoryFilter(it) },
                  onSearchQueryChanged = { viewModel.setSearchQuery(it) },
                  onEventClick = { viewModel.openEventDetail(it) },
                  onExplainClick = { viewModel.openEventDetail(it) },
                  onTradeClick = { symbol ->
                    val quote = quotes.firstOrNull { it.symbol == symbol }
                    if (quote != null) viewModel.openTradeSheet(quote)
                  },
                  onToggleBookmark = { viewModel.toggleBookmark(it) },
                  listState = dailyPulseListState
                )
              }

              NewsSubTab.FILINGS_RADAR -> {
                FilingRadarScreen(
                  events = filteredEvents,
                  disclosures = disclosures,
                  circulars = circulars,
                  isProMode = uiState.isProMode,
                  onEventClick = { viewModel.openEventDetail(it) },
                  onExplainClick = { viewModel.openEventDetail(it) },
                  onTradeClick = { symbol ->
                    val quote = quotes.firstOrNull { it.symbol == symbol }
                    if (quote != null) viewModel.openTradeSheet(quote)
                  },
                  onToggleBookmark = { viewModel.toggleBookmark(it) },
                  listState = filingRadarListState
                )
              }

              NewsSubTab.AI_CHAT -> {
                AiChatScreen(
                  chatMessages = chatMessages,
                  onSendMessage = { viewModel.sendChatMessage(it) },
                  isLoading = isChatLoading,
                  lastUpdatedText = "Just now"
                )
              }

              NewsSubTab.MACRO_CALENDAR -> {
                MacroEconomicScreen(
                  indicators = viewModel.economicIndicators,
                  isProMode = uiState.isProMode
                )
              }
            }
          }
        }

        // 2. PAPER TRADING VIEW
        Box(
          modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
              alpha = if (isPaperMode) 1f else 0f
              translationX = if (isPaperMode) 0f else 10000f
            }
        ) {
          PaperTradingScreen(
            account = paperAccount,
            positions = paperPositions,
            orders = paperOrders,
            ledgerEntries = cashLedger,
            journalEntries = journalEntries,
            quotes = quotes,
            onTradeStock = { stock, side -> viewModel.openTradeSheet(stock, side) },
            onResetPortfolio = { viewModel.resetPaperPortfolio() },
            onAddJournalNote = { symbol, title, notes -> viewModel.addJournalNote(symbol, title, notes) },
            listState = paperTradingListState
          )
        }
      }

      // HUD Toast Notification Banner
      AnimatedVisibility(
        visible = uiState.toastMessage != null,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .padding(bottom = 24.dp)
      ) {
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(SlateNavySurface)
            .border(1.dp, CyanTeal, RoundedCornerShape(24.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("app_toast_message")
        ) {
          Text(
            text = uiState.toastMessage ?: "",
            style = MaterialTheme.typography.bodySmall.copy(
              color = TextPrimary,
              fontWeight = FontWeight.Bold
            )
          )
        }
      }
    }

    // Modal Dialog: Explain this filing
    if (uiState.selectedEvent != null) {
      EventDetailDialog(
        event = uiState.selectedEvent!!,
        onDismiss = { viewModel.openEventDetail(null) },
        onOpenTrade = { symbol ->
          val quote = quotes.firstOrNull { it.symbol == symbol }
          if (quote != null) {
            viewModel.setPrimaryMode(PrimaryViewMode.PAPER_TRADING)
            viewModel.openTradeSheet(quote)
          }
        },
        onAddNote = { symbol, title ->
          viewModel.addJournalNote(symbol, title, "Filing review for $title")
        }
      )
    }

    // Modal Sheet: Paper Trade Execution Ticket
    if (uiState.tradeTargetStock != null) {
      PaperOrderSheet(
        stock = uiState.tradeTargetStock!!,
        account = paperAccount,
        initialSide = uiState.tradeActionSide,
        onDismiss = { viewModel.dismissTradeSheet() },
        onConfirmOrder = { symbol, side, type, qty, limitPrice ->
          viewModel.executePaperOrder(symbol, side, type, qty, limitPrice)
        }
      )
    }

    // Modal Dialog: User Profile & Security Sheet
    if (uiState.showProfileDialog && currentUser != null) {
      UserProfileDialog(
        user = currentUser!!,
        themeMode = uiState.themeMode,
        onSelectThemeMode = { viewModel.setThemeMode(it) },
        onDismiss = { viewModel.toggleProfileDialog(false) },
        onLogout = { viewModel.logout() }
      )
    }
  }
}

@Composable
fun NewsSubTabBar(
  activeSubTab: NewsSubTab,
  onSelectSubTab: (NewsSubTab) -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 14.dp, vertical = 4.dp),
    horizontalArrangement = Arrangement.spacedBy(6.dp)
  ) {
    listOf(
      Triple(NewsSubTab.PULSE, "Daily Pulse", Icons.Default.Bolt),
      Triple(NewsSubTab.FILINGS_RADAR, "Disclosures", Icons.Default.Policy),
      Triple(NewsSubTab.AI_CHAT, "AI Chat", Icons.Default.AutoAwesome),
      Triple(NewsSubTab.MACRO_CALENDAR, "Macro", Icons.Default.AccountBalance)
    ).forEach { (subTab, label, icon) ->
      val isSelected = activeSubTab == subTab
      Box(
        modifier = Modifier
          .weight(1f)
          .clip(RoundedCornerShape(8.dp))
          .background(if (isSelected) CyanTeal.copy(alpha = 0.2f) else CardSurface)
          .border(
            1.dp,
            if (isSelected) CyanTeal else CardBorder,
            RoundedCornerShape(8.dp)
          )
          .clickable { onSelectSubTab(subTab) }
          .testTag("news_subtab_${subTab.name.lowercase()}")
          .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) CyanTeal else TextMuted,
            modifier = Modifier.size(13.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              color = if (isSelected) CyanTeal else TextSecondary,
              fontSize = 10.sp
            ),
            maxLines = 1
          )
        }
      }
    }
  }
}
