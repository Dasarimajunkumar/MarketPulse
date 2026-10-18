package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EconomicIndicator
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CyanTeal
import com.example.ui.theme.GainEmerald
import com.example.ui.theme.RegulatoryAmber
import com.example.ui.theme.SecondaryBlue
import com.example.ui.theme.LocalThemeIsDark
import com.example.ui.theme.SlateNavySurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun MacroEconomicScreen(
  indicators: List<EconomicIndicator>,
  isProMode: Boolean,
  modifier: Modifier = Modifier
) {
  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 14.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    // Header Banner
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
          .testTag("macro_header_banner"),
        colors = CardDefaults.cardColors(containerColor = CardSurface)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.AccountBalance,
              contentDescription = null,
              tint = CyanTeal,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "INDIAN MACROECONOMIC CALENDAR & INSTITUTIONS",
              style = MaterialTheme.typography.labelSmall.copy(
                color = CyanTeal,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                fontSize = 10.sp
              )
            )
          }

          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Official release data and policy decisions from the Reserve Bank of India (RBI), Ministry of Statistics (MoSPI), and Ministry of Finance.",
            style = MaterialTheme.typography.bodySmall.copy(
              color = TextPrimary,
              fontSize = 12.sp,
              lineHeight = 16.sp
            )
          )
        }
      }
    }

    // Educational Guide for Beginners
    if (!isProMode) {
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, CyanTeal.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
          colors = CardDefaults.cardColors(
            containerColor = if (LocalThemeIsDark.current) Color(0xFF0D1B2A) else CyanTeal.copy(alpha = 0.08f)
          )
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
          ) {
            Icon(
              imageVector = Icons.Default.Lightbulb,
              contentDescription = null,
              tint = RegulatoryAmber,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = "HOW DOES MACRO IMPACT INDIAN EQUITIES?",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = RegulatoryAmber,
                  fontWeight = FontWeight.Bold,
                  fontSize = 10.sp
                )
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "• Lower Inflation / Rate Cuts: Reduces loan EMIs for consumers and debt servicing costs for companies, driving stock market expansion.\n• High Crude Oil: Negative for India's trade deficit and auto/paint companies.\n• Rising Forex Reserves: Safeguards the Indian Rupee (INR) from sudden volatility.",
                style = MaterialTheme.typography.bodySmall.copy(
                  color = TextSecondary,
                  fontSize = 11.sp,
                  lineHeight = 16.sp
                )
              )
            }
          }
        }
      }
    }

    // List of key indicators
    items(indicators, key = { it.id }) { indicator ->
      val impactColor = when (indicator.impact) {
        "Bullish" -> GainEmerald
        "Hawkish" -> RegulatoryAmber
        else -> SecondaryBlue
      }

      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(10.dp))
          .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
          .testTag("indicator_card_${indicator.id}"),
        colors = CardDefaults.cardColors(containerColor = CardSurface)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = indicator.title,
              style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )
            )

            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(impactColor.copy(alpha = 0.15f))
                .border(0.5.dp, impactColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(
                text = "${indicator.impact.uppercase()} STANCE",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = impactColor,
                  fontWeight = FontWeight.Bold,
                  fontSize = 9.sp
                )
              )
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.CalendarMonth,
              contentDescription = null,
              tint = TextMuted,
              modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Authority: ${indicator.authority} • Release: ${indicator.nextReleaseDate}",
              style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp)
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Values
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(6.dp))
              .background(SlateNavySurface)
              .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text("LATEST VALUE", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 9.sp))
              Text(
                indicator.currentValue,
                style = MaterialTheme.typography.titleMedium.copy(
                  color = CyanTeal,
                  fontWeight = FontWeight.Black,
                  fontFamily = FontFamily.Monospace
                )
              )
            }

            Column(horizontalAlignment = Alignment.End) {
              Text("PREVIOUS PERIOD", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 9.sp))
              Text(
                indicator.previousValue,
                style = MaterialTheme.typography.titleMedium.copy(
                  color = TextSecondary,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace
                )
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = indicator.description,
            style = MaterialTheme.typography.bodySmall.copy(
              color = TextSecondary,
              fontSize = 11.sp,
              lineHeight = 16.sp
            )
          )
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}
