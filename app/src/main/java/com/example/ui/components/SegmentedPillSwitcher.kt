package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material3.ripple
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CyanTeal
import com.example.ui.theme.SlateNavySurface
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.PrimaryViewMode

/**
 * SegmentedPillSwitcher component featuring smooth animated sliding pill indicator
 * and internal state management with external sync callback.
 */
@Composable
fun SegmentedPillSwitcher(
  modifier: Modifier = Modifier,
  initialMode: PrimaryViewMode = PrimaryViewMode.NEWS_CIRCULARS,
  selectedMode: PrimaryViewMode? = null,
  onModeChanged: (PrimaryViewMode) -> Unit = {}
) {
  // Internal state management persists across configuration changes and re-compositions
  var internalMode by rememberSaveable { mutableStateOf(initialMode) }

  // Sync internal mode if external selectedMode changes
  LaunchedEffect(selectedMode) {
    if (selectedMode != null && selectedMode != internalMode) {
      internalMode = selectedMode
    }
  }

  val currentMode = selectedMode ?: internalMode
  val selectedIndex = if (currentMode == PrimaryViewMode.NEWS_CIRCULARS) 0 else 1

  // Animated sliding indicator offset fraction (0f for News, 1f for Paper)
  val indicatorOffsetFraction by animateFloatAsState(
    targetValue = if (selectedIndex == 0) 0f else 1f,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioLowBouncy,
      stiffness = Spring.StiffnessMediumLow
    ),
    label = "pillIndicatorOffset"
  )

  BoxWithConstraints(
    modifier = modifier
      .fillMaxWidth()
      .height(44.dp)
      .clip(RoundedCornerShape(22.dp))
      .background(CardSurface)
      .border(1.dp, CardBorder, RoundedCornerShape(22.dp))
      .padding(3.dp)
      .testTag("segmented_pill_switcher")
      .semantics { contentDescription = "Primary View Switcher" }
  ) {
    val totalWidth = maxWidth
    val itemWidth = (totalWidth / 2)

    // Animated Sliding Active Pill Background
    Box(
      modifier = Modifier
        .offset(x = itemWidth * indicatorOffsetFraction)
        .width(itemWidth)
        .fillMaxHeight()
        .clip(RoundedCornerShape(19.dp))
        .background(CyanTeal)
    )

    // Text & Icon Overlays
    Row(
      modifier = Modifier.fillMaxSize(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // 1. News & Circulars Tab
      val isNewsActive = currentMode == PrimaryViewMode.NEWS_CIRCULARS
      val newsTextColor by animateColorAsState(
        targetValue = if (isNewsActive) SlateNavySurface else TextSecondary,
        label = "newsTextColor"
      )

      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight()
          .clip(RoundedCornerShape(19.dp))
          .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(bounded = true, color = SlateNavySurface),
            role = Role.Tab
          ) {
            internalMode = PrimaryViewMode.NEWS_CIRCULARS
            onModeChanged(PrimaryViewMode.NEWS_CIRCULARS)
          }
          .testTag("top_tab_news_circulars"),
        contentAlignment = Alignment.Center
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Policy,
            contentDescription = null,
            tint = newsTextColor,
            modifier = Modifier.size(15.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "News / Circulars",
            style = MaterialTheme.typography.labelMedium.copy(
              fontWeight = FontWeight.Bold,
              color = newsTextColor,
              fontSize = 12.sp
            )
          )
        }
      }

      // 2. Paper Trading Tab
      val isPaperActive = currentMode == PrimaryViewMode.PAPER_TRADING
      val paperTextColor by animateColorAsState(
        targetValue = if (isPaperActive) SlateNavySurface else TextSecondary,
        label = "paperTextColor"
      )

      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight()
          .clip(RoundedCornerShape(19.dp))
          .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(bounded = true, color = SlateNavySurface),
            role = Role.Tab
          ) {
            internalMode = PrimaryViewMode.PAPER_TRADING
            onModeChanged(PrimaryViewMode.PAPER_TRADING)
          }
          .testTag("top_tab_paper_trading"),
        contentAlignment = Alignment.Center
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ShowChart,
            contentDescription = null,
            tint = paperTextColor,
            modifier = Modifier.size(15.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Paper Trading",
            style = MaterialTheme.typography.labelMedium.copy(
              fontWeight = FontWeight.Bold,
              color = paperTextColor,
              fontSize = 12.sp
            )
          )
        }
      }
    }
  }
}
