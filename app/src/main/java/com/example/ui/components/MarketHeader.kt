package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import com.example.data.model.MarketIndex
import com.example.data.model.UserSession
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CyanTeal
import com.example.ui.theme.DeepNavyBg
import com.example.ui.theme.GainEmerald
import com.example.ui.theme.LocalThemeIsDark
import com.example.ui.theme.LossCrimson
import com.example.ui.theme.RegulatoryAmber
import com.example.ui.theme.SecondaryBlue
import com.example.ui.theme.SlateNavySurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.ThemeMode
import com.example.ui.viewmodel.PrimaryViewMode
import java.util.Locale

@Composable
fun MarketHeader(
  indices: List<MarketIndex>,
  primaryMode: PrimaryViewMode,
  onSelectPrimaryMode: (PrimaryViewMode) -> Unit,
  isProMode: Boolean,
  onToggleMode: () -> Unit,
  onRefresh: () -> Unit,
  themeMode: ThemeMode = ThemeMode.DARK,
  onToggleTheme: () -> Unit = {},
  currentUser: UserSession? = null,
  onProfileClick: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(SlateNavySurface)
  ) {
    // 1. Top Bar with Brand, Market Status & Pro/Beginner Switch
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(GainEmerald)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "MARKETPULSE",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Black,
            letterSpacing = 1.2.sp,
            color = TextPrimary
          )
        )
        Spacer(modifier = Modifier.width(6.dp))
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(CyanTeal.copy(alpha = 0.15f))
            .border(0.5.dp, CyanTeal.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Text(
            text = "NSE • BSE",
            style = MaterialTheme.typography.labelSmall.copy(
              color = CyanTeal,
              fontWeight = FontWeight.Bold,
              fontSize = 9.sp
            )
          )
        }
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        // Dual Mode Switch Button
        val modeBgColor by animateColorAsState(
          targetValue = if (isProMode) SecondaryBlue.copy(alpha = 0.2f) else CyanTeal.copy(alpha = 0.2f),
          label = "modeBg"
        )
        val modeBorderColor by animateColorAsState(
          targetValue = if (isProMode) SecondaryBlue else CyanTeal,
          label = "modeBorder"
        )

        Row(
          modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(modeBgColor)
            .border(1.dp, modeBorderColor, RoundedCornerShape(20.dp))
            .clickable(onClick = onToggleMode)
            .testTag("mode_toggle_button")
            .padding(horizontal = 8.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = if (isProMode) Icons.Default.Analytics else Icons.Default.School,
            contentDescription = "Toggle Mode",
            tint = if (isProMode) SecondaryBlue else CyanTeal,
            modifier = Modifier.size(13.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = if (isProMode) "PRO" else "BEGINNER",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.ExtraBold,
              color = if (isProMode) SecondaryBlue else CyanTeal,
              fontSize = 9.sp
            )
          )
        }

        Spacer(modifier = Modifier.width(4.dp))

        IconButton(
          onClick = onRefresh,
          modifier = Modifier
            .size(30.dp)
            .testTag("refresh_button")
        ) {
          Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Refresh",
            tint = TextSecondary,
            modifier = Modifier.size(16.dp)
          )
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Theme Mode Toggle Button (Light Mode / Dark Mode)
        val isDarkTheme = LocalThemeIsDark.current
        IconButton(
          onClick = onToggleTheme,
          modifier = Modifier
            .size(30.dp)
            .testTag("theme_toggle_button")
        ) {
          Icon(
            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
            contentDescription = if (isDarkTheme) "Switch to Light Mode" else "Switch to Dark Mode",
            tint = if (isDarkTheme) RegulatoryAmber else SecondaryBlue,
            modifier = Modifier.size(17.dp)
          )
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Profile / Auth Avatar Button
        if (currentUser != null) {
          val initial = currentUser.displayName.firstOrNull()?.uppercase() ?: "T"
          Row(
            modifier = Modifier
              .clip(RoundedCornerShape(16.dp))
              .background(CyanTeal.copy(alpha = 0.15f))
              .border(1.dp, CyanTeal.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
              .clickable(onClick = onProfileClick)
              .testTag("header_profile_button")
              .padding(horizontal = 7.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(CyanTeal),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = initial,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Black,
                  fontSize = 9.sp,
                  color = DeepNavyBg
                )
              )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (currentUser.isGuest) "GUEST" else currentUser.displayName.take(7),
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = CyanTeal,
                fontSize = 9.sp
              )
            )
          }
        } else {
          Row(
            modifier = Modifier
              .clip(RoundedCornerShape(16.dp))
              .background(CyanTeal.copy(alpha = 0.2f))
              .border(1.dp, CyanTeal, RoundedCornerShape(16.dp))
              .clickable(onClick = onProfileClick)
              .testTag("header_sign_in_button")
              .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Lock,
              contentDescription = "Sign In",
              tint = CyanTeal,
              modifier = Modifier.size(11.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
              text = "LOGIN",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = CyanTeal,
                fontSize = 9.sp
              )
            )
          }
        }
      }
    }

    // 2. Segmented Capsule Pill Switch: [ News & Circulars ] | [ Paper Trading ]
    SegmentedPillSwitcher(
      selectedMode = primaryMode,
      onModeChanged = onSelectPrimaryMode,
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp)
    )

    Spacer(modifier = Modifier.height(4.dp))

    // 3. Persistent Horizontal Scrolling Index Strip (Always visible across both views)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState())
        .padding(horizontal = 12.dp, vertical = 2.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      indices.forEach { indexItem ->
        IndexCard(indexItem = indexItem)
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    // 4. Real Source Health & Timestamp Freshness Strip
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(Color(0xFF0F172A))
        .padding(horizontal = 14.dp, vertical = 4.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(6.dp)
            .clip(CircleShape)
            .background(GainEmerald)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "SEBI • RBI • NSE • BSE Ingestion: ONLINE",
          style = MaterialTheme.typography.labelSmall.copy(
            fontSize = 9.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Bold
          )
        )
      }
      Text(
        text = "Polling Rate: 5m/10m • UTC-IST Normalized",
        style = MaterialTheme.typography.labelSmall.copy(
          fontSize = 9.sp,
          color = TextMuted
        )
      )
    }
  }
}

@Composable
fun IndexCard(indexItem: MarketIndex, modifier: Modifier = Modifier) {
  val isPositive = indexItem.changeValue >= 0
  val changeColor = if (isPositive) GainEmerald else LossCrimson
  val sign = if (isPositive) "+" else ""

  val statusColor = when (indexItem.tradingStatus.uppercase()) {
    "LIVE" -> GainEmerald
    "DELAYED" -> RegulatoryAmber
    "DEMO" -> CyanTeal
    "STALE" -> TextMuted
    else -> LossCrimson
  }

  Surface(
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .border(1.dp, CardBorder, RoundedCornerShape(8.dp)),
    color = CardSurface,
    tonalElevation = 2.dp
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = indexItem.symbol,
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              color = TextSecondary,
              fontSize = 10.sp
            )
          )
          Spacer(modifier = Modifier.width(4.dp))
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(3.dp))
              .background(statusColor.copy(alpha = 0.15f))
              .padding(horizontal = 4.dp, vertical = 1.dp)
          ) {
            Text(
              text = indexItem.tradingStatus,
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 7.sp,
                fontWeight = FontWeight.Bold,
                color = statusColor
              )
            )
          }
        }

        Text(
          text = String.format(Locale.US, "%,.2f", indexItem.currentValue),
          style = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = TextPrimary,
            fontSize = 12.sp
          )
        )
        Text(
          text = "$sign${String.format(Locale.US, "%.2f", indexItem.changeValue)} ($sign${String.format(Locale.US, "%.2f", indexItem.changePercent)}%)",
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.SemiBold,
            color = changeColor,
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp
          )
        )
      }

      Spacer(modifier = Modifier.width(8.dp))

      SparklineChart(
        dataPoints = parseSparkline(indexItem.sparklineJson),
        isPositive = isPositive,
        modifier = Modifier
          .width(48.dp)
          .height(24.dp)
      )
    }
  }
}
