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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StockQuote
import com.example.data.model.WatchlistItem
import com.example.ui.components.SparklineChart
import com.example.ui.components.parseSparkline
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CyanTeal
import com.example.ui.theme.GainEmerald
import com.example.ui.theme.LossCrimson
import com.example.ui.theme.RegulatoryAmber
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun WatchlistScreen(
  quotes: List<StockQuote>,
  watchlist: List<WatchlistItem>,
  onToggleWatchlist: (symbol: String, name: String) -> Unit,
  onTradeClick: (StockQuote) -> Unit,
  modifier: Modifier = Modifier
) {
  var searchQuery by remember { mutableStateOf("") }
  var showOnlyWatchlist by remember { mutableStateOf(false) }

  val watchlistSymbols = watchlist.map { it.symbol }.toSet()

  val filteredQuotes = quotes.filter { quote ->
    val matchesSearch = if (searchQuery.isBlank()) {
      true
    } else {
      val q = searchQuery.trim().lowercase()
      quote.symbol.lowercase().contains(q) ||
        quote.companyName.lowercase().contains(q) ||
        quote.sector.lowercase().contains(q)
    }

    val matchesFilter = if (showOnlyWatchlist) {
      watchlistSymbols.contains(quote.symbol)
    } else {
      true
    }

    matchesSearch && matchesFilter
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 14.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    // Header & Toggle Tabs
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(if (!showOnlyWatchlist) CyanTeal.copy(alpha = 0.2f) else CardSurface)
            .border(1.dp, if (!showOnlyWatchlist) CyanTeal else CardBorder, RoundedCornerShape(8.dp))
            .clickable { showOnlyWatchlist = false }
            .testTag("tab_all_equities")
            .padding(vertical = 10.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "ALL EQUITIES (${quotes.size})",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              color = if (!showOnlyWatchlist) CyanTeal else TextSecondary
            )
          )
        }

        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(if (showOnlyWatchlist) CyanTeal.copy(alpha = 0.2f) else CardSurface)
            .border(1.dp, if (showOnlyWatchlist) CyanTeal else CardBorder, RoundedCornerShape(8.dp))
            .clickable { showOnlyWatchlist = true }
            .testTag("tab_my_watchlist")
            .padding(vertical = 10.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "MY WATCHLIST (${watchlist.size})",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              color = if (showOnlyWatchlist) CyanTeal else TextSecondary
            )
          )
        }
      }
    }

    // Search bar
    item {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = { Text("Search by symbol (e.g. RELIANCE, TCS)...", color = TextMuted, fontSize = 12.sp) },
        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = CyanTeal, modifier = Modifier.size(18.dp)) },
        trailingIcon = {
          if (searchQuery.isNotEmpty()) {
            IconButton(onClick = { searchQuery = "" }) {
              Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = TextSecondary, modifier = Modifier.size(16.dp))
            }
          }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("watchlist_search_input"),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = CyanTeal,
          unfocusedBorderColor = CardBorder,
          focusedContainerColor = CardSurface,
          unfocusedContainerColor = CardSurface,
          focusedTextColor = TextPrimary,
          unfocusedTextColor = TextPrimary
        ),
        shape = RoundedCornerShape(10.dp)
      )
    }

    // Items
    items(filteredQuotes, key = { it.symbol }) { stock ->
      val isSaved = watchlistSymbols.contains(stock.symbol)
      val isPositive = stock.changePercent >= 0
      val changeColor = if (isPositive) GainEmerald else LossCrimson
      val sign = if (isPositive) "+" else ""

      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(10.dp))
          .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
          .testTag("stock_card_${stock.symbol}"),
        colors = CardDefaults.cardColors(containerColor = CardSurface)
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          // Top Row: Symbol, Star, Price & Sparkline
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              IconButton(
                onClick = { onToggleWatchlist(stock.symbol, stock.companyName) },
                modifier = Modifier
                  .size(32.dp)
                  .testTag("star_button_${stock.symbol}")
              ) {
                Icon(
                  imageVector = if (isSaved) Icons.Default.Star else Icons.Default.StarBorder,
                  contentDescription = "Watchlist",
                  tint = if (isSaved) RegulatoryAmber else TextMuted,
                  modifier = Modifier.size(20.dp)
                )
              }

              Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = stock.symbol,
                    style = MaterialTheme.typography.titleSmall.copy(
                      fontWeight = FontWeight.Black,
                      fontFamily = FontFamily.Monospace,
                      color = TextPrimary
                    )
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(4.dp))
                      .background(CardBorder.copy(alpha = 0.35f))
                      .padding(horizontal = 5.dp, vertical = 2.dp)
                  ) {
                    Text(text = stock.sector, style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 9.sp))
                  }
                }
                Text(
                  text = stock.companyName,
                  style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp),
                  maxLines = 1
                )
              }
            }

            // Right Price & Sparkline
            Row(verticalAlignment = Alignment.CenterVertically) {
              SparklineChart(
                dataPoints = parseSparkline(stock.sparklineJson),
                isPositive = isPositive,
                modifier = Modifier
                  .width(52.dp)
                  .height(26.dp)
              )

              Spacer(modifier = Modifier.width(8.dp))

              Column(horizontalAlignment = Alignment.End) {
                Text(
                  text = "₹${String.format(Locale.US, "%,.2f", stock.currentPrice)}",
                  style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = TextPrimary,
                    fontSize = 13.sp
                  )
                )
                Text(
                  text = "$sign${String.format(Locale.US, "%.2f", stock.changePercent)}%",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = changeColor,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                  )
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Bottom metrics & Trade button
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
              Column {
                Text("DAY HIGH", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 9.sp))
                Text("₹${String.format(Locale.US, "%.1f", stock.dayHigh)}", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontFamily = FontFamily.Monospace, fontSize = 10.sp))
              }
              Column {
                Text("DAY LOW", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 9.sp))
                Text("₹${String.format(Locale.US, "%.1f", stock.dayLow)}", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontFamily = FontFamily.Monospace, fontSize = 10.sp))
              }
              Column {
                Text("P/E RATIO", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 9.sp))
                Text("${stock.peRatio}", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontFamily = FontFamily.Monospace, fontSize = 10.sp))
              }
              Column {
                Text("MCAP", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 9.sp))
                Text(stock.marketCap, style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontFamily = FontFamily.Monospace, fontSize = 10.sp))
              }
            }

            OutlinedButton(
              onClick = { onTradeClick(stock) },
              modifier = Modifier
                .height(30.dp)
                .testTag("trade_quote_${stock.symbol}"),
              colors = ButtonDefaults.outlinedButtonColors(contentColor = GainEmerald),
              border = BorderStroke(1.dp, GainEmerald.copy(alpha = 0.5f)),
              contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 2.dp)
            ) {
              Icon(imageVector = Icons.AutoMirrored.Filled.ShowChart, contentDescription = null, modifier = Modifier.size(12.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Trade", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp))
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
