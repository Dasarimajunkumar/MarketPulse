package com.example.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import com.example.data.model.UserSession
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CyanTeal
import com.example.ui.theme.GainEmerald
import com.example.ui.theme.LossCrimson
import com.example.ui.theme.SecondaryBlue
import com.example.ui.theme.SlateNavySurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.ThemeMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun UserProfileDialog(
  user: UserSession,
  themeMode: ThemeMode = ThemeMode.DARK,
  onSelectThemeMode: (ThemeMode) -> Unit = {},
  onDismiss: () -> Unit,
  onLogout: () -> Unit
) {
  val initial = user.displayName.firstOrNull()?.uppercase() ?: "T"
  val formattedDate = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(user.loginTimestamp))

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(18.dp),
      color = CardSurface,
      border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("user_profile_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Header Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Shield,
              contentDescription = null,
              tint = CyanTeal,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "TRADER CREDENTIALS",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = CyanTeal,
                letterSpacing = 1.sp
              )
            )
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(28.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = TextMuted,
              modifier = Modifier.size(18.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Avatar
        Box(
          modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(CyanTeal.copy(alpha = 0.2f))
            .border(2.dp, CyanTeal, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = initial,
            style = MaterialTheme.typography.headlineMedium.copy(
              fontWeight = FontWeight.Black,
              color = CyanTeal
            )
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = user.displayName,
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = TextPrimary
          )
        )

        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(top = 4.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Email,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(13.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = user.email,
            style = MaterialTheme.typography.bodySmall.copy(
              color = TextSecondary,
              fontSize = 12.sp
            )
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Security / Account Details Box
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SlateNavySurface)
            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
            .padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          ProfileDetailRow(
            label = "TRADER ID",
            value = user.traderId,
            isMonospace = true,
            valueColor = CyanTeal
          )
          ProfileDetailRow(
            label = "STATUS",
            value = if (user.isGuest) "Sandbox Guest" else "OTP Verified (2FA)",
            valueColor = if (user.isGuest) SecondaryBlue else GainEmerald
          )
          ProfileDetailRow(
            label = "COMPLIANCE TIER",
            value = user.kycTier,
            valueColor = TextPrimary
          )
          ProfileDetailRow(
            label = "SESSION START",
            value = formattedDate,
            valueColor = TextMuted
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // THEME & APPEARANCE SELECTION
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SlateNavySurface)
            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
            .padding(14.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Palette,
                contentDescription = null,
                tint = CyanTeal,
                modifier = Modifier.size(15.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "DISPLAY THEME",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = TextMuted,
                  fontWeight = FontWeight.Bold,
                  fontSize = 10.sp,
                  letterSpacing = 0.8.sp
                )
              )
            }
            Text(
              text = when (themeMode) {
                ThemeMode.DARK -> "Terminal Dark"
                ThemeMode.LIGHT -> "Paper Light"
                ThemeMode.SYSTEM -> "System Auto"
              },
              style = MaterialTheme.typography.labelSmall.copy(
                color = CyanTeal,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
              )
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          // 3-way toggle pills: Dark, Light, System
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            listOf(
              Triple(ThemeMode.DARK, "Dark", Icons.Default.DarkMode),
              Triple(ThemeMode.LIGHT, "Light", Icons.Default.LightMode),
              Triple(ThemeMode.SYSTEM, "System", Icons.Default.SettingsBrightness)
            ).forEach { (mode, label, icon) ->
              val isSelected = themeMode == mode
              Box(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(8.dp))
                  .background(if (isSelected) CyanTeal.copy(alpha = 0.18f) else CardSurface)
                  .border(
                    1.dp,
                    if (isSelected) CyanTeal else CardBorder,
                    RoundedCornerShape(8.dp)
                  )
                  .clickable { onSelectThemeMode(mode) }
                  .testTag("theme_chip_${mode.name.lowercase()}")
                  .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.Center
                ) {
                  Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isSelected) CyanTeal else TextSecondary,
                    modifier = Modifier.size(14.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                      color = if (isSelected) CyanTeal else TextSecondary,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                      fontSize = 11.sp
                    )
                  )
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Actions: Log Out / Switch Account
        Button(
          onClick = {
            onLogout()
            onDismiss()
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .testTag("logout_button"),
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = LossCrimson.copy(alpha = 0.15f),
            contentColor = LossCrimson
          )
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
            contentDescription = "Log Out",
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Log Out / Switch Account",
            style = MaterialTheme.typography.labelLarge.copy(
              fontWeight = FontWeight.Bold
            )
          )
        }
      }
    }
  }
}

@Composable
private fun ProfileDetailRow(
  label: String,
  value: String,
  isMonospace: Boolean = false,
  valueColor: Color = TextPrimary
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall.copy(
        color = TextMuted,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp
      )
    )
    Text(
      text = value,
      style = MaterialTheme.typography.labelSmall.copy(
        color = valueColor,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default
      )
    )
  }
}
