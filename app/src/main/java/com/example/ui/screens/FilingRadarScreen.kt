package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CorporateDisclosure
import com.example.data.model.MarketEvent
import com.example.data.model.RegulatoryCircular
import com.example.ui.components.EventCard
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CyanTeal
import com.example.ui.theme.GainEmerald
import com.example.ui.theme.LossCrimson
import com.example.ui.theme.RegulatoryAmber
import com.example.ui.theme.SecondaryBlue
import com.example.ui.theme.SlateNavySurface
import com.example.ui.theme.LocalThemeIsDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.saveable.rememberSaveable

@Composable
fun FilingRadarScreen(
  events: List<MarketEvent>,
  disclosures: List<CorporateDisclosure>,
  circulars: List<RegulatoryCircular>,
  isProMode: Boolean,
  onEventClick: (MarketEvent) -> Unit,
  onExplainClick: (MarketEvent) -> Unit,
  onTradeClick: (String) -> Unit,
  onToggleBookmark: (MarketEvent) -> Unit,
  modifier: Modifier = Modifier,
  listState: LazyListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
) {
  var selectedTab by remember { mutableStateOf("DISCLOSURES") } // "DISCLOSURES", "CIRCULARS", "ALL_EVENTS"

  LazyColumn(
    state = listState,
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
          .testTag("filing_radar_banner"),
        colors = CardDefaults.cardColors(containerColor = CardSurface)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Policy,
              contentDescription = null,
              tint = RegulatoryAmber,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "REGULATORY & CORPORATE DISCLOSURE RADAR",
              style = MaterialTheme.typography.labelSmall.copy(
                color = RegulatoryAmber,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                fontSize = 10.sp
              )
            )
          }

          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Official primary filings directly from NSE, BSE, SEBI, and Reserve Bank of India with document reference IDs and offline historical fixtures.",
            style = MaterialTheme.typography.bodySmall.copy(
              color = TextPrimary,
              fontSize = 12.sp,
              lineHeight = 16.sp
            )
          )
        }
      }
    }

    // Segmented Source Switch
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        listOf(
          "DISCLOSURES" to "Corporate Filings (${disclosures.size})",
          "CIRCULARS" to "SEBI & RBI (${circulars.size})",
          "ALL_EVENTS" to "All Radar (${events.size})"
        ).forEach { (tabKey, tabLabel) ->
          val isSelected = selectedTab == tabKey
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(8.dp))
              .background(if (isSelected) RegulatoryAmber.copy(alpha = 0.2f) else CardSurface)
              .border(1.dp, if (isSelected) RegulatoryAmber else CardBorder, RoundedCornerShape(8.dp))
              .clickable { selectedTab = tabKey }
              .testTag("filing_tab_$tabKey")
              .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = tabLabel,
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = if (isSelected) RegulatoryAmber else TextSecondary,
                fontSize = 10.sp
              )
            )
          }
        }
      }
    }

    // Render depending on active tab
    when (selectedTab) {
      "DISCLOSURES" -> {
        items(disclosures, key = { it.id }) { disclosure ->
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
              .testTag("disclosure_card_${disclosure.id}"),
            colors = CardDefaults.cardColors(containerColor = CardSurface)
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(4.dp))
                      .background(CyanTeal.copy(alpha = 0.15f))
                      .padding(horizontal = 6.dp, vertical = 2.dp)
                  ) {
                    Text(
                      text = disclosure.companySymbol,
                      style = MaterialTheme.typography.labelSmall.copy(
                        color = CyanTeal,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                      )
                    )
                  }
                  Spacer(modifier = Modifier.width(6.dp))
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(4.dp))
                      .background(CardBorder.copy(alpha = 0.35f))
                      .padding(horizontal = 6.dp, vertical = 2.dp)
                  ) {
                    Text(
                      text = "${disclosure.exchange} • ${disclosure.filingCategory}",
                      style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 9.sp)
                    )
                  }
                }

                Text(
                  text = disclosure.formattedDate,
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = TextMuted,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                  )
                )
              }

              Spacer(modifier = Modifier.height(8.dp))

              Text(
                text = disclosure.title,
                style = MaterialTheme.typography.titleSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = TextPrimary,
                  fontSize = 13.sp
                )
              )

              Spacer(modifier = Modifier.height(4.dp))

              Text(
                text = disclosure.summary,
                style = MaterialTheme.typography.bodySmall.copy(
                  color = TextSecondary,
                  fontSize = 12.sp,
                  lineHeight = 16.sp
                )
              )

              if (disclosure.disclosedValue != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                  modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(RegulatoryAmber.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "Disclosed Value: ",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp)
                  )
                  Text(
                    text = disclosure.disclosedValue,
                    style = MaterialTheme.typography.labelSmall.copy(
                      color = RegulatoryAmber,
                      fontWeight = FontWeight.Bold,
                      fontFamily = FontFamily.Monospace,
                      fontSize = 11.sp
                    )
                  )
                }
              }

              Spacer(modifier = Modifier.height(10.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "Ref: ${disclosure.filingReferenceId}",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = TextMuted,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp
                  )
                )

                OutlinedButton(
                  onClick = { onTradeClick(disclosure.companySymbol) },
                  modifier = Modifier.height(28.dp),
                  colors = ButtonDefaults.outlinedButtonColors(contentColor = GainEmerald),
                  border = BorderStroke(1.dp, GainEmerald.copy(alpha = 0.5f)),
                  contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                  Icon(imageVector = Icons.AutoMirrored.Filled.ShowChart, contentDescription = null, modifier = Modifier.size(12.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Trade", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        }
      }

      "CIRCULARS" -> {
        items(circulars, key = { it.id }) { circular ->
          val regColor = if (circular.regulator == "SEBI") CyanTeal else RegulatoryAmber

          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
              .testTag("circular_card_${circular.id}"),
            colors = CardDefaults.cardColors(containerColor = CardSurface)
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(4.dp))
                      .background(regColor.copy(alpha = 0.15f))
                      .border(0.5.dp, regColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                      .padding(horizontal = 6.dp, vertical = 2.dp)
                  ) {
                    Text(
                      text = circular.regulator,
                      style = MaterialTheme.typography.labelSmall.copy(
                        color = regColor,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                      )
                    )
                  }
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = circular.circularNumber,
                    style = MaterialTheme.typography.labelSmall.copy(
                      color = TextMuted,
                      fontFamily = FontFamily.Monospace,
                      fontSize = 9.sp
                    )
                  )
                }

                Text(
                  text = circular.formattedDate,
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = TextMuted,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                  )
                )
              }

              Spacer(modifier = Modifier.height(8.dp))

              Text(
                text = circular.title,
                style = MaterialTheme.typography.titleSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = TextPrimary,
                  fontSize = 13.sp
                )
              )

              Spacer(modifier = Modifier.height(4.dp))

              Text(
                text = circular.summary,
                style = MaterialTheme.typography.bodySmall.copy(
                  color = TextSecondary,
                  fontSize = 12.sp,
                  lineHeight = 16.sp
                )
              )

              Spacer(modifier = Modifier.height(8.dp))

              // Plain Language Takeaway Callout
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(6.dp))
                  .background(if (LocalThemeIsDark.current) Color(0xFF0F1E2E) else CyanTeal.copy(alpha = 0.08f))
                  .padding(8.dp)
              ) {
                Column {
                  Text(
                    text = "PLAIN-LANGUAGE TAKEAWAY",
                    style = MaterialTheme.typography.labelSmall.copy(
                      color = CyanTeal,
                      fontWeight = FontWeight.Bold,
                      fontSize = 9.sp
                    )
                  )
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(
                    text = circular.plainLanguageTakeaway,
                    style = MaterialTheme.typography.bodySmall.copy(
                      color = TextPrimary,
                      fontSize = 11.sp
                    )
                  )
                }
              }

              Spacer(modifier = Modifier.height(8.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "Effective: ${circular.effectiveDate}",
                  style = MaterialTheme.typography.labelSmall.copy(color = RegulatoryAmber, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                )

                Text(
                  text = "Dept: ${circular.department}",
                  style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 9.sp)
                )
              }
            }
          }
        }
      }

      "ALL_EVENTS" -> {
        items(events, key = { it.id }) { event ->
          EventCard(
            event = event,
            isProMode = isProMode,
            onEventClick = { onEventClick(event) },
            onExplainClick = { onExplainClick(event) },
            onTradeClick = onTradeClick,
            onToggleBookmark = { onToggleBookmark(event) }
          )
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}
