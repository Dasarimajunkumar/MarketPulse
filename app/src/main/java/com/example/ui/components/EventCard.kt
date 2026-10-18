package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.data.model.MarketEvent
import com.example.data.model.VerificationType
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CyanTeal
import com.example.ui.theme.GainEmerald
import com.example.ui.theme.LocalThemeIsDark
import com.example.ui.theme.LossCrimson
import com.example.ui.theme.RegulatoryAmber
import com.example.ui.theme.SecondaryBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EventCard(
  event: MarketEvent,
  isProMode: Boolean,
  onEventClick: () -> Unit,
  onExplainClick: () -> Unit,
  onTradeClick: (symbol: String) -> Unit,
  onToggleBookmark: () -> Unit,
  modifier: Modifier = Modifier
) {
  val verificationBadgeColor = when (event.verificationType) {
    VerificationType.OFFICIAL_SOURCE_VERIFIED.name -> GainEmerald
    VerificationType.REPORTED_BY_PUBLISHER.name -> SecondaryBlue
    else -> RegulatoryAmber
  }

  val verificationLabel = when (event.verificationType) {
    VerificationType.OFFICIAL_SOURCE_VERIFIED.name -> "CONFIRMED FACT"
    VerificationType.REPORTED_BY_PUBLISHER.name -> "REPORTED MEDIA"
    else -> "CONTEXT ANALYSIS"
  }

  val materialityColor = when (event.materiality) {
    "HIGH" -> LossCrimson
    "MEDIUM" -> RegulatoryAmber
    else -> CyanTeal
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(10.dp))
      .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
      .clickable(onClick = onEventClick)
      .testTag("event_card_${event.id}"),
    colors = CardDefaults.cardColors(containerColor = CardSurface)
  ) {
    Column(
      modifier = Modifier.padding(if (isProMode) 10.dp else 14.dp)
    ) {
      // Top Tag Bar: Truth Verification & Time
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          // Truth Tag
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(verificationBadgeColor.copy(alpha = 0.15f))
              .border(0.5.dp, verificationBadgeColor.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Verified,
                contentDescription = null,
                tint = verificationBadgeColor,
                modifier = Modifier.size(10.dp)
              )
              Spacer(modifier = Modifier.width(3.dp))
              Text(
                text = verificationLabel,
                style = MaterialTheme.typography.labelSmall.copy(
                  color = verificationBadgeColor,
                  fontWeight = FontWeight.Bold,
                  fontSize = 9.sp
                )
              )
            }
          }

          // Source Tag
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(CardBorder.copy(alpha = 0.35f))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = event.sourceName,
              style = MaterialTheme.typography.labelSmall.copy(
                color = TextSecondary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 9.sp
              )
            )
          }

          // Pro-mode Materiality badge
          if (isProMode) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(materialityColor.copy(alpha = 0.15f))
                .border(0.5.dp, materialityColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
              Text(
                text = "${event.materiality} IMPACT",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = materialityColor,
                  fontWeight = FontWeight.Bold,
                  fontSize = 8.sp,
                  fontFamily = FontFamily.Monospace
                )
              )
            }
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = event.formattedTime,
            style = MaterialTheme.typography.labelSmall.copy(
              color = TextMuted,
              fontFamily = FontFamily.Monospace,
              fontSize = 10.sp
            )
          )

          IconButton(
            onClick = onToggleBookmark,
            modifier = Modifier.size(24.dp)
          ) {
            Icon(
              imageVector = if (event.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
              contentDescription = "Save Event",
              tint = if (event.isBookmarked) CyanTeal else TextMuted,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Entity and Sector tags
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(CyanTeal.copy(alpha = 0.12f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Text(
            text = event.entitySymbol,
            style = MaterialTheme.typography.labelSmall.copy(
              color = CyanTeal,
              fontWeight = FontWeight.Black,
              fontFamily = FontFamily.Monospace,
              fontSize = 11.sp
            )
          )
        }

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(CardBorder.copy(alpha = 0.35f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Text(
            text = event.sector,
            style = MaterialTheme.typography.labelSmall.copy(
              color = TextSecondary,
              fontSize = 10.sp
            )
          )
        }

        if (event.disclosedValue != null) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(RegulatoryAmber.copy(alpha = 0.15f))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = event.disclosedValue,
              style = MaterialTheme.typography.labelSmall.copy(
                color = RegulatoryAmber,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp
              )
            )
          }
        }

        if (isProMode && event.filingId != null) {
          Text(
            text = "Doc: ${event.filingId}",
            style = MaterialTheme.typography.labelSmall.copy(
              color = TextMuted,
              fontFamily = FontFamily.Monospace,
              fontSize = 9.sp
            ),
            modifier = Modifier.padding(top = 2.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Title
      Text(
        text = event.title,
        style = MaterialTheme.typography.titleSmall.copy(
          fontWeight = FontWeight.Bold,
          color = TextPrimary,
          fontSize = if (isProMode) 13.sp else 14.sp,
          lineHeight = if (isProMode) 18.sp else 20.sp
        )
      )

      Spacer(modifier = Modifier.height(4.dp))

      // Summary
      Text(
        text = event.summary,
        style = MaterialTheme.typography.bodySmall.copy(
          color = TextSecondary,
          fontSize = if (isProMode) 11.sp else 12.sp,
          lineHeight = 16.sp
        ),
        maxLines = if (isProMode) 2 else 3
      )

      // Beginner Mode Explainer callout
      if (!isProMode) {
        Spacer(modifier = Modifier.height(10.dp))
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(if (LocalThemeIsDark.current) Color(0xFF0F1E2E) else CyanTeal.copy(alpha = 0.08f))
            .border(1.dp, CyanTeal.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
            .padding(10.dp)
        ) {
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                contentDescription = null,
                tint = CyanTeal,
                modifier = Modifier.size(13.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "BEGINNER TAKEAWAY",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = CyanTeal,
                  fontWeight = FontWeight.Bold,
                  fontSize = 9.sp
                )
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = event.beginnerExplainer.lines().firstOrNull { it.isNotBlank() && !it.startsWith("WHAT") }
                ?: event.beginnerExplainer.take(140) + "...",
              style = MaterialTheme.typography.bodySmall.copy(
                color = TextPrimary,
                fontSize = 11.sp,
                lineHeight = 15.sp
              ),
              maxLines = 2
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Action Buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        OutlinedButton(
          onClick = onExplainClick,
          modifier = Modifier
            .height(30.dp)
            .testTag("explain_button_${event.id}"),
          colors = ButtonDefaults.outlinedButtonColors(
            contentColor = CyanTeal
          ),
          border = BorderStroke(1.dp, CyanTeal.copy(alpha = 0.5f)),
          contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Description,
            contentDescription = null,
            modifier = Modifier.size(12.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "Explain Filing",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold)
          )
        }

        // Only show Paper Trade if it's a tradable company (not regulatory agency)
        val isTradable = event.entitySymbol !in listOf("SEBI", "RBI", "MACRO")
        if (isTradable) {
          OutlinedButton(
            onClick = { onTradeClick(event.entitySymbol) },
            modifier = Modifier
              .height(30.dp)
              .testTag("trade_button_${event.entitySymbol}"),
            colors = ButtonDefaults.outlinedButtonColors(
              contentColor = GainEmerald
            ),
            border = BorderStroke(1.dp, GainEmerald.copy(alpha = 0.5f)),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ShowChart,
              contentDescription = null,
              modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Paper Trade",
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold)
            )
          }
        }
      }
    }
  }
}
