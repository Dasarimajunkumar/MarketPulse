package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GainEmerald
import com.example.ui.theme.LossCrimson

@Composable
fun SparklineChart(
  dataPoints: List<Float>,
  isPositive: Boolean = true,
  modifier: Modifier = Modifier
    .width(72.dp)
    .height(32.dp)
) {
  val strokeColor = if (isPositive) GainEmerald else LossCrimson
  val gradientColor = if (isPositive) GainEmerald.copy(alpha = 0.25f) else LossCrimson.copy(alpha = 0.25f)

  if (dataPoints.size < 2) return

  Canvas(modifier = modifier) {
    val minVal = dataPoints.minOrNull() ?: 0f
    val maxVal = dataPoints.maxOrNull() ?: 1f
    val range = (maxVal - minVal).coerceAtLeast(0.001f)

    val width = size.width
    val height = size.height
    val stepX = width / (dataPoints.size - 1)

    val path = Path()
    val fillPath = Path()

    dataPoints.forEachIndexed { index, value ->
      val x = index * stepX
      val normalizedY = 1f - ((value - minVal) / range)
      // Leave 2dp padding top & bottom
      val y = 2f + (normalizedY * (height - 4f))

      if (index == 0) {
        path.moveTo(x, y)
        fillPath.moveTo(x, height)
        fillPath.lineTo(x, y)
      } else {
        path.lineTo(x, y)
        fillPath.lineTo(x, y)
      }

      if (index == dataPoints.size - 1) {
        fillPath.lineTo(x, height)
        fillPath.close()
      }
    }

    // Draw gradient fill
    drawPath(
      path = fillPath,
      brush = Brush.verticalGradient(
        colors = listOf(gradientColor, Color.Transparent),
        startY = 0f,
        endY = height
      )
    )

    // Draw stroke
    drawPath(
      path = path,
      color = strokeColor,
      style = Stroke(
        width = 2.dp.toPx(),
        cap = StrokeCap.Round,
        join = StrokeJoin.Round
      )
    )
  }
}

fun parseSparkline(sparklineJson: String): List<Float> {
  return try {
    sparklineJson.split(",")
      .mapNotNull { it.trim().toFloatOrNull() }
      .ifEmpty { listOf(10f, 12f, 11f, 14f, 15f) }
  } catch (e: Exception) {
    listOf(10f, 12f, 11f, 14f, 15f)
  }
}
