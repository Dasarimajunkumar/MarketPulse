package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MarketEvent
import com.example.ui.components.EventCard
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CyanTeal
import com.example.ui.theme.GainEmerald
import com.example.ui.theme.RegulatoryAmber
import com.example.ui.theme.SecondaryBlue
import com.example.ui.theme.SlateNavySurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

private data class PulseFilterCategory(
  val key: String,
  val label: String,
  val count: Int,
  val icon: ImageVector,
  val testTag: String,
  val activeColor: Color,
  val description: String
)

@Composable
fun DailyPulseScreen(
  events: List<MarketEvent>,
  isProMode: Boolean,
  selectedCategory: String,
  searchQuery: String,
  onCategorySelected: (String) -> Unit,
  onSearchQueryChanged: (String) -> Unit,
  onEventClick: (MarketEvent) -> Unit,
  onExplainClick: (MarketEvent) -> Unit,
  onTradeClick: (String) -> Unit,
  onToggleBookmark: (MarketEvent) -> Unit,
  modifier: Modifier = Modifier,
  allEvents: List<MarketEvent> = emptyList(),
  listState: LazyListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
) {
  // Use allEvents to compute accurate category event counts for the filter chips
  val sourceList = if (allEvents.isNotEmpty()) allEvents else events

  val countAll = sourceList.size
  val countSebi = sourceList.count {
    it.entitySymbol.equals("SEBI", ignoreCase = true) ||
      it.sourceName.contains("SEBI", ignoreCase = true) ||
      it.title.contains("SEBI", ignoreCase = true)
  }
  val countRbi = sourceList.count {
    it.entitySymbol.equals("RBI", ignoreCase = true) ||
      it.sourceName.contains("RBI", ignoreCase = true) ||
      it.title.contains("RBI", ignoreCase = true)
  }
  val countCorporate = sourceList.count {
    it.entitySymbol !in listOf("SEBI", "RBI", "MACRO")
  }

  val filterCategories = listOf(
    PulseFilterCategory(
      key = "ALL",
      label = "All Events",
      count = countAll,
      icon = Icons.Default.FilterList,
      testTag = "filter_chip_all",
      activeColor = CyanTeal,
      description = "All canonical market pulses"
    ),
    PulseFilterCategory(
      key = "SEBI",
      label = "SEBI",
      count = countSebi,
      icon = Icons.Default.Gavel,
      testTag = "filter_chip_sebi",
      activeColor = CyanTeal,
      description = "Securities & Exchange Board circulars & market norms"
    ),
    PulseFilterCategory(
      key = "RBI",
      label = "RBI",
      count = countRbi,
      icon = Icons.Default.AccountBalance,
      testTag = "filter_chip_rbi",
      activeColor = RegulatoryAmber,
      description = "Reserve Bank monetary policies & banking liquidity"
    ),
    PulseFilterCategory(
      key = "CORPORATE",
      label = "General Corporate",
      count = countCorporate,
      icon = Icons.Default.Business,
      testTag = "filter_chip_corporate",
      activeColor = Color(0xFF4FC3F7),
      description = "NSE/BSE corporate filings, earnings & contracts"
    )
  )

  // Toggle handler: tapping the already selected category toggles it off back to "ALL"
  val handleCategoryToggle: (String) -> Unit = { targetKey ->
    if (targetKey == "ALL") {
      onCategorySelected("ALL")
    } else if (selectedCategory.equals(targetKey, ignoreCase = true)) {
      // Toggle OFF back to ALL
      onCategorySelected("ALL")
    } else {
      // Toggle ON
      onCategorySelected(targetKey)
    }
  }

  val isFiltered = selectedCategory.isNotBlank() && !selectedCategory.equals("ALL", ignoreCase = true)

  LazyColumn(
    state = listState,
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 14.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    // "Since Previous Close" Daily Briefing Card
    item {
      DailyBriefingHeader(isProMode = isProMode)
    }

    // Search Box
    item {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChanged,
        placeholder = {
          Text(
            text = "Search Indian companies, filings, or circulars...",
            style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
          )
        },
        leadingIcon = {
          Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = CyanTeal,
            modifier = Modifier.size(18.dp)
          )
        },
        trailingIcon = {
          if (searchQuery.isNotEmpty()) {
            IconButton(onClick = { onSearchQueryChanged("") }) {
              Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = TextSecondary, modifier = Modifier.size(16.dp))
            }
          }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("daily_pulse_search_input"),
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

    // Category Filter Chips Section Header & Toggle Chips
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 2.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "CATEGORY PULSE FILTER",
            style = MaterialTheme.typography.labelSmall.copy(
              color = TextMuted,
              fontFamily = FontFamily.Monospace,
              letterSpacing = 1.sp,
              fontWeight = FontWeight.Bold,
              fontSize = 9.sp
            )
          )

          if (isFiltered) {
            Text(
              text = "TAP CHIP TO TOGGLE OFF",
              style = MaterialTheme.typography.labelSmall.copy(
                color = CyanTeal,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold
              )
            )
          }
        }

        // Horizontal Row of Filter Chips
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          filterCategories.forEach { category ->
            val isSelected = if (category.key == "ALL") {
              selectedCategory.isBlank() || selectedCategory.equals("ALL", ignoreCase = true)
            } else {
              selectedCategory.equals(category.key, ignoreCase = true)
            }

            FilterChip(
              selected = isSelected,
              onClick = { handleCategoryToggle(category.key) },
              modifier = Modifier
                .testTag(category.testTag)
                .height(38.dp),
              leadingIcon = {
                Icon(
                  imageVector = category.icon,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp),
                  tint = if (isSelected) category.activeColor else TextMuted
                )
              },
              label = {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Text(
                    text = category.label,
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                      fontSize = 12.sp,
                      color = if (isSelected) TextPrimary else TextSecondary
                    )
                  )

                  // Category count badge
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(10.dp))
                      .background(
                        if (isSelected) category.activeColor.copy(alpha = 0.25f)
                        else SlateNavySurface
                      )
                      .padding(horizontal = 6.dp, vertical = 2.dp)
                  ) {
                    Text(
                      text = category.count.toString(),
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) category.activeColor else TextMuted
                      )
                    )
                  }
                }
              },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = category.activeColor.copy(alpha = 0.16f),
                containerColor = CardSurface,
                selectedLabelColor = TextPrimary,
                labelColor = TextSecondary
              ),
              border = FilterChipDefaults.filterChipBorder(
                borderColor = CardBorder,
                selectedBorderColor = category.activeColor,
                borderWidth = 1.dp,
                selectedBorderWidth = 1.5.dp,
                enabled = true,
                selected = isSelected
              ),
              shape = RoundedCornerShape(19.dp)
            )
          }
        }
      }
    }

    // Active Category Filter Indicator Banner (shows when a non-ALL filter is active)
    if (isFiltered) {
      item {
        val activeCat = filterCategories.firstOrNull { it.key.equals(selectedCategory, ignoreCase = true) }
        val activeTitle = when (selectedCategory.uppercase().trim()) {
          "SEBI" -> "SEBI Regulatory Mandates & Circulars"
          "RBI" -> "Reserve Bank of India Monetary & Liquidity Actions"
          "CORPORATE", "GENERAL_CORPORATE" -> "General Corporate Filings, Earnings & Contracts"
          else -> selectedCategory
        }
        val activeColor = activeCat?.activeColor ?: CyanTeal

        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .testTag("active_category_banner"),
          colors = CardDefaults.cardColors(containerColor = activeColor.copy(alpha = 0.08f)),
          shape = RoundedCornerShape(10.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, activeColor.copy(alpha = 0.35f))
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              modifier = Modifier.weight(1f),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = activeCat?.icon ?: Icons.Default.FilterList,
                contentDescription = null,
                tint = activeColor,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = activeTitle,
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = activeColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                  )
                )
                Text(
                  text = "Displaying ${events.size} verified event${if (events.size == 1) "" else "s"} • Tap chip again to reset",
                  style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondary,
                    fontSize = 10.sp
                  )
                )
              }
            }

            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(activeColor.copy(alpha = 0.16f))
                .clickable { onCategorySelected("ALL") }
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .testTag("filter_chip_clear"),
              contentAlignment = Alignment.Center
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Close,
                  contentDescription = "Clear Category Filter",
                  tint = activeColor,
                  modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Clear",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = activeColor,
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

    // Stream Header
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (isFiltered) "FILTERED MARKET EVENTS (${events.size})" else "CANONICAL MARKET EVENTS (${events.size})",
          style = MaterialTheme.typography.labelSmall.copy(
            color = TextSecondary,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp,
            fontSize = 10.sp
          )
        )

        Text(
          text = if (isFiltered) "CATEGORY: ${selectedCategory.uppercase()}" else "PROVENANCE VERIFIED",
          style = MaterialTheme.typography.labelSmall.copy(
            color = if (isFiltered) CyanTeal else GainEmerald,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp
          )
        )
      }
    }

    // Events Feed
    if (events.isEmpty()) {
      item {
        Card(
          colors = CardDefaults.cardColors(containerColor = CardSurface),
          modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
            .padding(vertical = 32.dp)
            .testTag("daily_pulse_empty_card")
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = TextMuted, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = if (isFiltered) "No events match category '$selectedCategory'" else "No events match your active filters",
              style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary, fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Try clearing your search query or toggling back to 'All Events'.",
              style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 11.sp)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Button(
              onClick = {
                onCategorySelected("ALL")
                onSearchQueryChanged("")
              },
              colors = ButtonDefaults.buttonColors(containerColor = CyanTeal, contentColor = SlateNavySurface),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.testTag("reset_filters_button")
            ) {
              Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Reset All Filters", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
          }
        }
      }
    } else {
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

    item {
      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
fun DailyBriefingHeader(isProMode: Boolean) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .border(1.dp, CardBorder, RoundedCornerShape(12.dp)),
    colors = CardDefaults.cardColors(containerColor = CardSurface)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = CyanTeal,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "DAILY MARKET BRIEFING",
            style = MaterialTheme.typography.labelSmall.copy(
              color = CyanTeal,
              fontWeight = FontWeight.Black,
              letterSpacing = 1.sp,
              fontSize = 10.sp
            )
          )
        }

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(SecondaryBlue.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Text(
            text = "SINCE PREVIOUS CLOSE",
            style = MaterialTheme.typography.labelSmall.copy(
              color = SecondaryBlue,
              fontWeight = FontWeight.Bold,
              fontSize = 9.sp
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "Key catalysts: L&T bagged ₹4,500 Cr Middle-East order, SEBI updated index derivative lot sizes, and Tata Motors demerger received court clearance.",
        style = MaterialTheme.typography.bodySmall.copy(
          color = TextPrimary,
          fontSize = 12.sp,
          lineHeight = 17.sp
        )
      )

      if (!isProMode) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "Tip: Tap 'Explain Filing' on any card to read our 4-point plain-English breakdown of official disclosures.",
          style = MaterialTheme.typography.labelSmall.copy(
            color = CyanTeal,
            fontSize = 10.sp
          )
        )
      }
    }
  }
}
