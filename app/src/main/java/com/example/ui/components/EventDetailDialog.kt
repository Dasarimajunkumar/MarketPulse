package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
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
import androidx.compose.ui.window.Dialog
import com.example.data.model.MarketEvent
import com.example.data.model.VerificationType
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

@Composable
fun EventDetailDialog(
  event: MarketEvent,
  onDismiss: () -> Unit,
  onOpenTrade: (symbol: String) -> Unit,
  onAddNote: (symbol: String, title: String) -> Unit
) {
  val verificationBadgeColor = when (event.verificationType) {
    VerificationType.OFFICIAL_SOURCE_VERIFIED.name -> GainEmerald
    VerificationType.REPORTED_BY_PUBLISHER.name -> SecondaryBlue
    else -> RegulatoryAmber
  }

  val verificationLabel = when (event.verificationType) {
    VerificationType.OFFICIAL_SOURCE_VERIFIED.name -> "CONFIRMED PRIMARY-SOURCE FACT"
    VerificationType.REPORTED_BY_PUBLISHER.name -> "REPORTED BY FINANCIAL MEDIA"
    else -> "MODEL-GENERATED CONTEXT"
  }

  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(16.dp))
        .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
        .testTag("event_detail_dialog"),
      colors = CardDefaults.cardColors(containerColor = SlateNavySurface)
    ) {
      Column(
        modifier = Modifier
          .padding(20.dp)
          .verticalScroll(rememberScrollState())
      ) {
        // Dialog Top Bar
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
                .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
              Text(
                text = event.entitySymbol,
                style = MaterialTheme.typography.labelSmall.copy(
                  color = CyanTeal,
                  fontWeight = FontWeight.Black,
                  fontFamily = FontFamily.Monospace,
                  fontSize = 12.sp
                )
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = event.category,
              style = MaterialTheme.typography.labelSmall.copy(
                color = TextSecondary,
                fontWeight = FontWeight.SemiBold,
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

        Spacer(modifier = Modifier.height(12.dp))

        // Title
        Text(
          text = event.title,
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            lineHeight = 22.sp
          )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Truth Tagging & Provenance Banner
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(verificationBadgeColor.copy(alpha = 0.12f))
            .border(1.dp, verificationBadgeColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(10.dp)
        ) {
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Verified,
                contentDescription = null,
                tint = verificationBadgeColor,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = verificationLabel,
                style = MaterialTheme.typography.labelSmall.copy(
                  color = verificationBadgeColor,
                  fontWeight = FontWeight.Bold,
                  fontSize = 10.sp
                )
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Source: ${event.sourceName} • ${event.formattedTime}" +
                if (event.filingId != null) " • Filing ID: ${event.filingId}" else "",
              style = MaterialTheme.typography.labelSmall.copy(
                color = TextSecondary,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Disclosed Value highlight if present
        if (event.disclosedValue != null) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(6.dp))
              .background(RegulatoryAmber.copy(alpha = 0.15f))
              .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Disclosed Deal / Value:",
              style = MaterialTheme.typography.bodySmall.copy(
                color = TextSecondary,
                fontSize = 11.sp
              )
            )
            Text(
              text = event.disclosedValue,
              style = MaterialTheme.typography.bodyMedium.copy(
                color = RegulatoryAmber,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp
              )
            )
          }
          Spacer(modifier = Modifier.height(14.dp))
        }

        // Canonical Summary
        Text(
          text = "EXECUTIVE SUMMARY",
          style = MaterialTheme.typography.labelSmall.copy(
            color = CyanTeal,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp,
            fontSize = 10.sp
          )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = event.summary,
          style = MaterialTheme.typography.bodyMedium.copy(
            color = TextPrimary,
            fontSize = 13.sp,
            lineHeight = 19.sp
          )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Structured "Explain This Filing" breakdown
        Text(
          text = "STRUCTURED BREAKDOWN & INTELLIGENCE",
          style = MaterialTheme.typography.labelSmall.copy(
            color = CyanTeal,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp,
            fontSize = 10.sp
          )
        )
        Spacer(modifier = Modifier.height(6.dp))

        Card(
          colors = CardDefaults.cardColors(containerColor = CardSurface),
          modifier = Modifier.border(1.dp, CardBorder, RoundedCornerShape(8.dp))
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Text(
              text = event.beginnerExplainer,
              style = MaterialTheme.typography.bodySmall.copy(
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 18.sp
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Source Provenance Link Notice
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Link,
            contentDescription = null,
            tint = CyanTeal,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Official Source Document Verified via ${event.sourceName}",
            style = MaterialTheme.typography.labelSmall.copy(
              color = CyanTeal,
              fontSize = 10.sp
            )
          )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Bottom Actions
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          val isTradable = event.entitySymbol !in listOf("SEBI", "RBI", "MACRO")
          if (isTradable) {
            Button(
              onClick = {
                onDismiss()
                onOpenTrade(event.entitySymbol)
              },
              modifier = Modifier
                .weight(1f)
                .height(42.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = GainEmerald,
                contentColor = Color.White
              ),
              shape = RoundedCornerShape(8.dp)
            ) {
              Icon(imageVector = Icons.AutoMirrored.Filled.ShowChart, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Paper Trade", fontWeight = FontWeight.Bold)
            }
          }

          OutlinedButton(
            onClick = {
              onAddNote(event.entitySymbol, event.title)
              onDismiss()
            },
            modifier = Modifier
              .weight(1f)
              .height(42.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
            border = BorderStroke(1.dp, CardBorder),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text("Add to Journal", fontSize = 12.sp)
          }
        }
      }
    }
  }
}
