package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PaperAccount
import com.example.data.model.PaperOrder
import com.example.data.model.PaperPosition
import com.example.data.model.StockQuote
import com.example.data.model.TradingJournalEntry
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CyanTeal
import com.example.ui.theme.GainEmerald
import com.example.ui.theme.LossCrimson
import com.example.ui.theme.SlateNavySurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun PaperTradingScreen(
  account: PaperAccount?,
  positions: List<PaperPosition>,
  orders: List<PaperOrder>,
  ledgerEntries: List<com.example.data.model.CashLedgerEntry>,
  journalEntries: List<TradingJournalEntry>,
  quotes: List<StockQuote>,
  onTradeStock: (StockQuote, side: String) -> Unit,
  onResetPortfolio: () -> Unit,
  onAddJournalNote: (symbol: String, title: String, notes: String) -> Unit,
  modifier: Modifier = Modifier,
  listState: LazyListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
) {
  var selectedTab by remember { mutableStateOf("POSITIONS") } // "POSITIONS", "ORDERS", "LEDGER", "JOURNAL"
  var showResetConfirmation by remember { mutableStateOf(false) }
  var showAddJournalDialog by remember { mutableStateOf(false) }

  // Portfolio calculations
  val cashBalance = account?.cashBalance ?: 1000000.0
  val initialCapital = account?.initialCapital ?: 1000000.0
  val realizedPnl = account?.realizedPnl ?: 0.0

  val quoteMap = quotes.associateBy { it.symbol }

  var currentPositionsMarketValue = 0.0
  var totalUnrealizedPnl = 0.0

  positions.forEach { pos ->
    val livePrice = quoteMap[pos.symbol]?.currentPrice ?: pos.currentPrice
    val marketVal = pos.quantity * livePrice
    val costVal = pos.quantity * pos.avgPrice
    currentPositionsMarketValue += marketVal
    totalUnrealizedPnl += (marketVal - costVal)
  }

  val totalEquity = cashBalance + currentPositionsMarketValue
  val totalReturnAmt = totalEquity - initialCapital
  val totalReturnPercent = (totalReturnAmt / initialCapital) * 100.0

  if (showResetConfirmation) {
    AlertDialog(
      onDismissRequest = { showResetConfirmation = false },
      title = { Text("Reset Paper Portfolio?") },
      text = { Text("This will clear all virtual positions and order history, resetting cash balance to ₹10,00,000.") },
      confirmButton = {
        Button(
          onClick = {
            onResetPortfolio()
            showResetConfirmation = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = LossCrimson)
        ) {
          Text("Reset")
        }
      },
      dismissButton = {
        TextButton(onClick = { showResetConfirmation = false }) {
          Text("Cancel", color = TextSecondary)
        }
      },
      containerColor = CardSurface
    )
  }

  if (showAddJournalDialog) {
    AddJournalDialog(
      quotes = quotes,
      onDismiss = { showAddJournalDialog = false },
      onSave = { symbol, title, notes ->
        onAddJournalNote(symbol, title, notes)
        showAddJournalDialog = false
      }
    )
  }

  LazyColumn(
    state = listState,
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 14.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    // Portfolio Stats Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
          .testTag("paper_portfolio_card"),
        colors = CardDefaults.cardColors(containerColor = CardSurface)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "PORTFOLIO NET EQUITY (VIRTUAL)",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = TextMuted,
                  fontWeight = FontWeight.Bold,
                  fontSize = 10.sp
                )
              )
              Text(
                text = "₹${String.format(Locale.US, "%,.2f", totalEquity)}",
                style = MaterialTheme.typography.headlineSmall.copy(
                  fontWeight = FontWeight.Black,
                  fontFamily = FontFamily.Monospace,
                  color = TextPrimary
                )
              )
            }

            IconButton(
              onClick = { showResetConfirmation = true },
              modifier = Modifier.testTag("reset_portfolio_button")
            ) {
              Icon(imageVector = Icons.Default.RestartAlt, contentDescription = "Reset", tint = TextMuted)
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // 3-stat Grid
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(SlateNavySurface)
              .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text("AVAILABLE CASH", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 9.sp))
              Text(
                "₹${String.format(Locale.US, "%,.0f", cashBalance)}",
                style = MaterialTheme.typography.bodySmall.copy(
                  color = CyanTeal,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace,
                  fontSize = 12.sp
                )
              )
            }

            Column {
              Text("UNREALIZED P&L", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 9.sp))
              val unPnlColor = if (totalUnrealizedPnl >= 0) GainEmerald else LossCrimson
              val sign = if (totalUnrealizedPnl >= 0) "+" else ""
              Text(
                "$sign₹${String.format(Locale.US, "%,.0f", totalUnrealizedPnl)}",
                style = MaterialTheme.typography.bodySmall.copy(
                  color = unPnlColor,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace,
                  fontSize = 12.sp
                )
              )
            }

            Column {
              Text("REALIZED P&L", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 9.sp))
              val realColor = if (realizedPnl >= 0) GainEmerald else LossCrimson
              val sign = if (realizedPnl >= 0) "+" else ""
              Text(
                "$sign₹${String.format(Locale.US, "%,.0f", realizedPnl)}",
                style = MaterialTheme.typography.bodySmall.copy(
                  color = realColor,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace,
                  fontSize = 12.sp
                )
              )
            }
          }
        }
      }
    }

    // Sub-Navigation Tabs
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        listOf(
          "POSITIONS" to "Holdings (${positions.size})",
          "ORDERS" to "Orders (${orders.size})",
          "LEDGER" to "Ledger (${ledgerEntries.size})",
          "JOURNAL" to "Journal (${journalEntries.size})"
        ).forEach { (tabKey, tabLabel) ->
          val isSelected = selectedTab == tabKey
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(8.dp))
              .background(if (isSelected) CyanTeal.copy(alpha = 0.2f) else CardSurface)
              .border(1.dp, if (isSelected) CyanTeal else CardBorder, RoundedCornerShape(8.dp))
              .clickable { selectedTab = tabKey }
              .testTag("paper_tab_$tabKey")
              .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = tabLabel,
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = if (isSelected) CyanTeal else TextSecondary,
                fontSize = 11.sp
              )
            )
          }
        }
      }
    }

    // Tab Content
    when (selectedTab) {
      "POSITIONS" -> {
        if (positions.isEmpty()) {
          item {
            EmptyPaperState(
              title = "No open positions yet",
              subtitle = "Select any Indian equity from the Watchlist or feed to practice paper trading."
            )
          }
        } else {
          items(positions, key = { it.symbol }) { position ->
            val livePrice = quoteMap[position.symbol]?.currentPrice ?: position.currentPrice
            val pnl = (livePrice - position.avgPrice) * position.quantity
            val isProfit = pnl >= 0
            val pnlColor = if (isProfit) GainEmerald else LossCrimson
            val sign = if (isProfit) "+" else ""

            Card(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, CardBorder, RoundedCornerShape(8.dp)),
              colors = CardDefaults.cardColors(containerColor = CardSurface)
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(
                    text = position.symbol,
                    style = MaterialTheme.typography.titleSmall.copy(
                      fontWeight = FontWeight.Bold,
                      fontFamily = FontFamily.Monospace,
                      color = TextPrimary
                    )
                  )
                  Text(
                    text = "${position.quantity} Shares • Avg: ₹${String.format(Locale.US, "%.1f", position.avgPrice)}",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                  )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                  Column(horizontalAlignment = Alignment.End) {
                    Text(
                      text = "₹${String.format(Locale.US, "%,.2f", livePrice * position.quantity)}",
                      style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = TextPrimary
                      )
                    )
                    Text(
                      text = "$sign₹${String.format(Locale.US, "%,.1f", pnl)}",
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = pnlColor,
                        fontFamily = FontFamily.Monospace
                      )
                    )
                  }

                  Spacer(modifier = Modifier.width(8.dp))

                  OutlinedButton(
                    onClick = {
                      val stock = quoteMap[position.symbol]
                      if (stock != null) onTradeStock(stock, "SELL")
                    },
                    modifier = Modifier.height(28.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = LossCrimson),
                    border = BorderStroke(1.dp, LossCrimson.copy(alpha = 0.5f)),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                  ) {
                    Text("Exit", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                  }
                }
              }
            }
          }
        }
      }

      "ORDERS" -> {
        if (orders.isEmpty()) {
          item {
            EmptyPaperState(
              title = "No orders executed yet",
              subtitle = "All simulated market and limit orders will appear here with execution timestamps."
            )
          }
        } else {
          items(orders, key = { it.orderId }) { order ->
            val isBuy = order.side == "BUY"
            val badgeColor = if (isBuy) GainEmerald else LossCrimson

            Card(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, CardBorder, RoundedCornerShape(8.dp)),
              colors = CardDefaults.cardColors(containerColor = CardSurface)
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(4.dp))
                      .background(badgeColor.copy(alpha = 0.15f))
                      .padding(horizontal = 6.dp, vertical = 3.dp)
                  ) {
                    Text(
                      text = order.side,
                      style = MaterialTheme.typography.labelSmall.copy(
                        color = badgeColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp
                      )
                    )
                  }
                  Spacer(modifier = Modifier.width(8.dp))
                  Column {
                    Text(
                      text = order.symbol,
                      style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = TextPrimary
                      )
                    )
                    Text(
                      text = "${order.formattedDate} • ID: ${order.orderId}",
                      style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 9.sp)
                    )
                  }
                }

                Column(horizontalAlignment = Alignment.End) {
                  Text(
                    text = "${order.quantity} @ ₹${String.format(Locale.US, "%,.2f", order.price)}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                      fontWeight = FontWeight.Bold,
                      fontFamily = FontFamily.Monospace,
                      color = TextPrimary
                    )
                  )
                  Text(
                    text = order.status,
                    style = MaterialTheme.typography.labelSmall.copy(
                      color = GainEmerald,
                      fontWeight = FontWeight.Bold,
                      fontSize = 10.sp
                    )
                  )
                }
              }
            }
          }
        }
      }

      "LEDGER" -> {
        if (ledgerEntries.isEmpty()) {
          item {
            EmptyPaperState(
              title = "Cash ledger is empty",
              subtitle = "All double-entry debits, credits, and running balance changes will be audited here."
            )
          }
        } else {
          items(ledgerEntries, key = { it.entryId }) { entry ->
            val isCredit = entry.type == "CREDIT"
            val typeColor = if (isCredit) GainEmerald else LossCrimson
            val sign = if (isCredit) "+₹" else "-₹"

            Card(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, CardBorder, RoundedCornerShape(8.dp)),
              colors = CardDefaults.cardColors(containerColor = CardSurface)
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(4.dp))
                      .background(typeColor.copy(alpha = 0.15f))
                      .padding(horizontal = 6.dp, vertical = 3.dp)
                  ) {
                    Text(
                      text = entry.type,
                      style = MaterialTheme.typography.labelSmall.copy(
                        color = typeColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp
                      )
                    )
                  }
                  Spacer(modifier = Modifier.width(8.dp))
                  Column {
                    Text(
                      text = entry.description,
                      style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                      )
                    )
                    Text(
                      text = "${entry.formattedTime} • ${entry.entryId}",
                      style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 9.sp)
                    )
                  }
                }

                Column(horizontalAlignment = Alignment.End) {
                  Text(
                    text = "$sign${String.format(Locale.US, "%,.2f", entry.amount)}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                      fontWeight = FontWeight.Bold,
                      fontFamily = FontFamily.Monospace,
                      color = typeColor
                    )
                  )
                  Text(
                    text = "Bal: ₹${String.format(Locale.US, "%,.0f", entry.runningBalance)}",
                    style = MaterialTheme.typography.labelSmall.copy(
                      color = TextSecondary,
                      fontFamily = FontFamily.Monospace,
                      fontSize = 9.sp
                    )
                  )
                }
              }
            }
          }
        }
      }

      "JOURNAL" -> {
        item {
          OutlinedButton(
            onClick = { showAddJournalDialog = true },
            modifier = Modifier
              .fillMaxWidth()
              .height(40.dp)
              .testTag("add_journal_button"),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanTeal),
            border = BorderStroke(1.dp, CyanTeal.copy(alpha = 0.5f))
          ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Add Trade Thesis Note", fontWeight = FontWeight.Bold)
          }
        }

        if (journalEntries.isEmpty()) {
          item {
            EmptyPaperState(
              title = "Trading journal is empty",
              subtitle = "Document your strategy, entry thesis, and risk-reward ratio before placing trades."
            )
          }
        } else {
          items(journalEntries, key = { it.id }) { entry ->
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, CardBorder, RoundedCornerShape(8.dp)),
              colors = CardDefaults.cardColors(containerColor = CardSurface)
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(4.dp))
                      .background(CyanTeal.copy(alpha = 0.15f))
                      .padding(horizontal = 6.dp, vertical = 2.dp)
                  ) {
                    Text(
                      text = entry.symbol,
                      style = MaterialTheme.typography.labelSmall.copy(
                        color = CyanTeal,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                      )
                    )
                  }
                  Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                      fontWeight = FontWeight.Bold,
                      color = TextPrimary
                    )
                  )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = entry.notes,
                  style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                  )
                )
              }
            }
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
fun EmptyPaperState(title: String, subtitle: String) {
  Card(
    colors = CardDefaults.cardColors(containerColor = CardSurface),
    modifier = Modifier
      .fillMaxWidth()
      .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
      .padding(vertical = 24.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Icon(imageVector = Icons.Default.Assessment, contentDescription = null, tint = TextMuted, modifier = Modifier.size(32.dp))
      Spacer(modifier = Modifier.height(8.dp))
      Text(title, style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary, fontWeight = FontWeight.Bold))
      Text(subtitle, style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 11.sp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
  }
}

@Composable
fun AddJournalDialog(
  quotes: List<StockQuote>,
  onDismiss: () -> Unit,
  onSave: (symbol: String, title: String, notes: String) -> Unit
) {
  var selectedSymbol by remember { mutableStateOf(quotes.firstOrNull()?.symbol ?: "RELIANCE") }
  var title by remember { mutableStateOf("") }
  var notes by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Record Trade Thesis", fontWeight = FontWeight.Bold) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Thesis / Catalyst (e.g. Q2 Result Beat)") },
          modifier = Modifier.fillMaxWidth(),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CyanTeal,
            unfocusedBorderColor = CardBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          )
        )

        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text("Detailed Rationale & Expected Target") },
          modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CyanTeal,
            unfocusedBorderColor = CardBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          )
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (title.isNotBlank()) onSave(selectedSymbol, title, notes)
        },
        colors = ButtonDefaults.buttonColors(containerColor = CyanTeal, contentColor = SlateNavySurface)
      ) {
        Text("Save Note", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = TextSecondary)
      }
    },
    containerColor = CardSurface
  )
}
