package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.PaperAccount
import com.example.data.model.StockQuote
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
fun PaperOrderSheet(
  stock: StockQuote,
  account: PaperAccount?,
  initialSide: String = "BUY",
  onDismiss: () -> Unit,
  onConfirmOrder: (symbol: String, side: String, type: String, quantity: Int, limitPrice: Double?) -> Unit
) {
  var side by remember { mutableStateOf(initialSide) }
  var orderType by remember { mutableStateOf("MARKET") } // "MARKET" or "LIMIT"
  var quantity by remember { mutableIntStateOf(10) }
  var limitPriceInput by remember { mutableStateOf(String.format(Locale.US, "%.2f", stock.currentPrice)) }

  val effectivePrice = if (orderType == "LIMIT") {
    limitPriceInput.toDoubleOrNull() ?: stock.currentPrice
  } else {
    stock.currentPrice
  }

  val totalValue = effectivePrice * quantity
  val simulatedCharges = 20.0 // Simulated flat brokerage & exchange turnover charge
  val netAmount = if (side == "BUY") totalValue + simulatedCharges else totalValue - simulatedCharges
  val availableCash = account?.cashBalance ?: 1000000.0

  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(16.dp))
        .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
        .testTag("paper_order_dialog"),
      colors = CardDefaults.cardColors(containerColor = SlateNavySurface)
    ) {
      Column(
        modifier = Modifier
          .padding(20.dp)
          .verticalScroll(rememberScrollState())
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = stock.symbol,
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Black,
                  fontFamily = FontFamily.Monospace,
                  color = TextPrimary
                )
              )
              Spacer(modifier = Modifier.width(6.dp))
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .background(CyanTeal.copy(alpha = 0.15f))
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Text(
                  text = "NSE",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = CyanTeal,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp
                  )
                )
              }
            }
            Text(
              text = stock.companyName,
              style = MaterialTheme.typography.bodySmall.copy(
                color = TextSecondary,
                fontSize = 11.sp
              )
            )
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(32.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = TextSecondary,
              modifier = Modifier.size(20.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Price Row
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(CardSurface)
            .padding(12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "LATEST MARKET PRICE",
              style = MaterialTheme.typography.labelSmall.copy(
                color = TextMuted,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp
              )
            )
            Text(
              text = "₹${String.format(Locale.US, "%,.2f", stock.currentPrice)}",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                color = TextPrimary
              )
            )
          }

          val isPositive = stock.changePercent >= 0
          val changeColor = if (isPositive) GainEmerald else LossCrimson
          val sign = if (isPositive) "+" else ""

          Text(
            text = "$sign${String.format(Locale.US, "%.2f", stock.changePercent)}%",
            style = MaterialTheme.typography.bodyMedium.copy(
              fontWeight = FontWeight.Bold,
              color = changeColor,
              fontFamily = FontFamily.Monospace
            )
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // BUY vs SELL Segmented Switch
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(CardSurface)
            .padding(4.dp),
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          // BUY Option
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(6.dp))
              .background(if (side == "BUY") GainEmerald else Color.Transparent)
              .clickable { side = "BUY" }
              .testTag("select_buy_side")
              .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "BUY (LONG)",
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = if (side == "BUY") Color.White else TextSecondary
              )
            )
          }

          // SELL Option
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(6.dp))
              .background(if (side == "SELL") LossCrimson else Color.Transparent)
              .clickable { side = "SELL" }
              .testTag("select_sell_side")
              .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "SELL (SHORT/EXIT)",
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = if (side == "SELL") Color.White else TextSecondary
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Order Type (MARKET vs LIMIT)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(6.dp))
              .background(if (orderType == "MARKET") CyanTeal.copy(alpha = 0.2f) else CardSurface)
              .border(
                1.dp,
                if (orderType == "MARKET") CyanTeal else CardBorder,
                RoundedCornerShape(6.dp)
              )
              .clickable { orderType = "MARKET" }
              .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "MARKET ORDER",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = if (orderType == "MARKET") CyanTeal else TextSecondary
              )
            )
          }

          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(6.dp))
              .background(if (orderType == "LIMIT") CyanTeal.copy(alpha = 0.2f) else CardSurface)
              .border(
                1.dp,
                if (orderType == "LIMIT") CyanTeal else CardBorder,
                RoundedCornerShape(6.dp)
              )
              .clickable { orderType = "LIMIT" }
              .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "LIMIT ORDER",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = if (orderType == "LIMIT") CyanTeal else TextSecondary
              )
            )
          }
        }

        // Limit Price Input if LIMIT is chosen
        if (orderType == "LIMIT") {
          Spacer(modifier = Modifier.height(10.dp))
          OutlinedTextField(
            value = limitPriceInput,
            onValueChange = { limitPriceInput = it },
            label = { Text("Limit Price (₹)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = CyanTeal,
              unfocusedBorderColor = CardBorder,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            )
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Quantity Selector
        Text(
          text = "QUANTITY (SHARES)",
          style = MaterialTheme.typography.labelSmall.copy(
            color = TextMuted,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp
          )
        )
        Spacer(modifier = Modifier.height(4.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          listOf(1, 5, 10, 25, 50, 100).forEach { qtyStep ->
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (quantity == qtyStep) CyanTeal else CardSurface)
                .clickable { quantity = qtyStep }
                .padding(horizontal = 10.dp, vertical = 6.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "$qtyStep",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = if (quantity == qtyStep) SlateNavySurface else TextPrimary,
                  fontFamily = FontFamily.Monospace
                )
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Calculations & Breakdown
        Card(
          colors = CardDefaults.cardColors(containerColor = CardSurface),
          modifier = Modifier.border(1.dp, CardBorder, RoundedCornerShape(8.dp))
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("Gross Order Value:", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp))
              Text("₹${String.format(Locale.US, "%,.2f", totalValue)}", style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("Simulated Taxes & Brokerage:", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp))
              Text("₹20.00", style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontFamily = FontFamily.Monospace, fontSize = 11.sp))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("Available Paper Balance:", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp))
              Text("₹${String.format(Locale.US, "%,.2f", availableCash)}", style = MaterialTheme.typography.bodySmall.copy(color = CyanTeal, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp))
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Zero Risk Notice
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          Icon(
            imageVector = Icons.Default.Security,
            contentDescription = null,
            tint = CyanTeal,
            modifier = Modifier.size(13.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "Virtual simulation • No real capital is ever at risk.",
            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 9.sp)
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Confirm Button
        val buttonColor = if (side == "BUY") GainEmerald else LossCrimson
        Button(
          onClick = {
            val limitDouble = if (orderType == "LIMIT") limitPriceInput.toDoubleOrNull() else null
            onConfirmOrder(stock.symbol, side, orderType, quantity, limitDouble)
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .testTag("submit_order_button"),
          colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor,
            contentColor = Color.White
          ),
          shape = RoundedCornerShape(8.dp)
        ) {
          Text(
            text = "EXECUTE SIMULATED $side ORDER",
            style = MaterialTheme.typography.bodyMedium.copy(
              fontWeight = FontWeight.Black,
              letterSpacing = 0.8.sp
            )
          )
        }
      }
    }
  }
}
