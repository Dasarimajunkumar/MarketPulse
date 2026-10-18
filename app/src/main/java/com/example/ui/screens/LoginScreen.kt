package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AuthStep
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import com.example.ui.theme.LocalThemeIsDark
import com.example.ui.theme.RegulatoryAmber
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CyanTeal
import com.example.ui.theme.CyanTealLight
import com.example.ui.theme.DeepNavyBg
import com.example.ui.theme.GainEmerald
import com.example.ui.theme.LossCrimson
import com.example.ui.theme.SecondaryBlue
import com.example.ui.theme.SlateNavySurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LoginScreen(
  authStep: AuthStep,
  emailInput: String,
  otpInput: String,
  generatedOtp: String,
  isLoading: Boolean,
  errorMessage: String?,
  onEmailChanged: (String) -> Unit,
  onOtpChanged: (String) -> Unit,
  onRequestOtp: (String) -> Unit,
  onVerifyOtp: (String) -> Unit,
  onBackToEmail: () -> Unit,
  onGuestLogin: () -> Unit,
  onToggleTheme: () -> Unit = {},
  canDismiss: Boolean = false,
  onDismiss: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  if (authStep == AuthStep.OTP_VERIFY) {
    OtpVerificationScreen(
      email = emailInput,
      otp = otpInput,
      generatedOtp = generatedOtp,
      isLoading = isLoading,
      errorMessage = errorMessage,
      onOtpChanged = onOtpChanged,
      onVerifyOtp = onVerifyOtp,
      onResendOtp = { onRequestOtp(emailInput) },
      onBackToEmail = onBackToEmail,
      onToggleTheme = onToggleTheme,
      canDismiss = canDismiss,
      onDismiss = onDismiss,
      onGuestLogin = onGuestLogin,
      modifier = modifier
    )
    return
  }

  // If in Email input step and can dismiss, BackHandler dismisses login screen
  BackHandler(enabled = authStep == AuthStep.EMAIL_INPUT && canDismiss) {
    onDismiss()
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(DeepNavyBg)
      .imePadding()
  ) {
    // Top Row Controls: Theme Toggle & Dismiss Button
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 16.dp, start = 16.dp, end = 16.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      val isDark = LocalThemeIsDark.current
      IconButton(
        onClick = onToggleTheme,
        modifier = Modifier
          .size(36.dp)
          .testTag("login_theme_toggle_button")
      ) {
        Icon(
          imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
          contentDescription = if (isDark) "Switch to Light Mode" else "Switch to Dark Mode",
          tint = if (isDark) RegulatoryAmber else SecondaryBlue,
          modifier = Modifier.size(20.dp)
        )
      }

      if (canDismiss) {
        IconButton(
          onClick = onDismiss,
          modifier = Modifier
            .size(36.dp)
            .testTag("login_dismiss_button")
        ) {
          Icon(
            imageVector = Icons.Default.Clear,
            contentDescription = "Close Login",
            tint = TextSecondary,
            modifier = Modifier.size(22.dp)
          )
        }
      } else {
        Spacer(modifier = Modifier.size(36.dp))
      }
    }
    // Subtle background ambient financial glow
    Box(
      modifier = Modifier
        .size(320.dp)
        .align(Alignment.TopCenter)
        .background(
          brush = Brush.radialGradient(
            colors = listOf(
              CyanTeal.copy(alpha = 0.08f),
              Color.Transparent
            )
          )
        )
    )

    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 24.dp, vertical = 32.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // 1. BRAND HEADER & STATUS
      Spacer(modifier = Modifier.height(20.dp))
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
      ) {
        Box(
          modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
              brush = Brush.linearGradient(
                listOf(CyanTeal.copy(alpha = 0.25f), SecondaryBlue.copy(alpha = 0.25f))
              )
            )
            .border(1.5.dp, CyanTeal, RoundedCornerShape(12.dp)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
            contentDescription = "MarketPulse Logo",
            tint = CyanTeal,
            modifier = Modifier.size(24.dp)
          )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "MARKETPULSE",
              style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                color = TextPrimary
              )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(GainEmerald.copy(alpha = 0.15f))
                .border(0.5.dp, GainEmerald, RoundedCornerShape(4.dp))
                .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
              Text(
                text = "TERMINAL",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = GainEmerald,
                  fontWeight = FontWeight.Bold,
                  fontSize = 9.sp
                )
              )
            }
          }
          Text(
            text = "Institutional Feeds & Paper Trading",
            style = MaterialTheme.typography.bodySmall.copy(
              color = TextMuted,
              fontSize = 11.sp
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(32.dp))

      // 2. ANIMATED STEP CONTAINER
      AnimatedContent(
        targetState = authStep,
        transitionSpec = {
          if (targetState == AuthStep.OTP_VERIFY) {
            (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
              slideOutHorizontally { width -> -width } + fadeOut()
            )
          } else {
            (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
              slideOutHorizontally { width -> width } + fadeOut()
            )
          }
        },
        label = "auth_steps_transition"
      ) { step ->
        when (step) {
          AuthStep.EMAIL_INPUT -> {
            EmailInputContent(
              email = emailInput,
              isLoading = isLoading,
              errorMessage = errorMessage,
              onEmailChanged = onEmailChanged,
              onRequestOtp = { onRequestOtp(emailInput) },
              onGuestLogin = onGuestLogin
            )
          }
          AuthStep.OTP_VERIFY -> {
            OtpVerificationContent(
              email = emailInput,
              otp = otpInput,
              generatedOtp = generatedOtp,
              isLoading = isLoading,
              errorMessage = errorMessage,
              onOtpChanged = onOtpChanged,
              onVerifyOtp = onVerifyOtp,
              onResendOtp = { onRequestOtp(emailInput) },
              onBackToEmail = onBackToEmail
            )
          }
          AuthStep.AUTHENTICATED -> {
            // Success state placeholder
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
              contentAlignment = Alignment.Center
            ) {
              CircularProgressIndicator(color = CyanTeal)
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(32.dp))

      // 3. SECURITY & COMPLIANCE FOOTER
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(10.dp))
          .background(SlateNavySurface)
          .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
          .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.Shield,
          contentDescription = "Security",
          tint = CyanTeal,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(
            text = "Enterprise-Grade Auth & Data Integrity",
            style = MaterialTheme.typography.labelSmall.copy(
              color = TextPrimary,
              fontWeight = FontWeight.SemiBold
            )
          )
          Text(
            text = "Zero-knowledge one-time OTP passwords. No stored plaintext secrets. Official NSE & BSE disclosure feeds.",
            style = MaterialTheme.typography.labelSmall.copy(
              color = TextMuted,
              fontSize = 10.sp,
              lineHeight = 13.sp
            )
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EmailInputContent(
  email: String,
  isLoading: Boolean,
  errorMessage: String?,
  onEmailChanged: (String) -> Unit,
  onRequestOtp: () -> Unit,
  onGuestLogin: () -> Unit
) {
  val keyboardController = LocalSoftwareKeyboardController.current
  val isValidEmail = email.contains("@") && email.contains(".") && email.length >= 5

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .background(CardSurface)
      .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
      .padding(20.dp),
    horizontalAlignment = Alignment.Start
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.fillMaxWidth()
    ) {
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(CyanTeal.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Lock,
          contentDescription = null,
          tint = CyanTeal,
          modifier = Modifier.size(18.dp)
        )
      }
      Spacer(modifier = Modifier.width(10.dp))
      Column {
        Text(
          text = "Trader Sign-In",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = TextPrimary
          )
        )
        Text(
          text = "Enter your email to receive a 6-digit OTP",
          style = MaterialTheme.typography.bodySmall.copy(
            color = TextSecondary,
            fontSize = 12.sp
          )
        )
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    Text(
      text = "EMAIL ADDRESS",
      style = MaterialTheme.typography.labelSmall.copy(
        color = TextMuted,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        fontSize = 10.sp
      )
    )

    Spacer(modifier = Modifier.height(6.dp))

    OutlinedTextField(
      value = email,
      onValueChange = onEmailChanged,
      modifier = Modifier
        .fillMaxWidth()
        .testTag("login_email_input"),
      placeholder = {
        Text(
          text = "trader@domain.com",
          style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted)
        )
      },
      leadingIcon = {
        Icon(
          imageVector = Icons.Default.Email,
          contentDescription = "Email Icon",
          tint = if (email.isNotEmpty()) CyanTeal else TextMuted
        )
      },
      trailingIcon = {
        if (email.isNotEmpty()) {
          IconButton(
            onClick = { onEmailChanged("") },
            modifier = Modifier.size(24.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Clear,
              contentDescription = "Clear",
              tint = TextMuted
            )
          }
        }
      },
      singleLine = true,
      keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Email,
        imeAction = ImeAction.Done
      ),
      keyboardActions = KeyboardActions(
        onDone = {
          keyboardController?.hide()
          if (isValidEmail && !isLoading) {
            onRequestOtp()
          }
        }
      ),
      shape = RoundedCornerShape(10.dp),
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = CyanTeal,
        unfocusedBorderColor = CardBorder,
        focusedContainerColor = SlateNavySurface,
        unfocusedContainerColor = SlateNavySurface,
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary,
        cursorColor = CyanTeal
      )
    )

    // Error text if any
    if (!errorMessage.isNullOrBlank()) {
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = errorMessage,
        style = MaterialTheme.typography.labelSmall.copy(
          color = LossCrimson,
          fontWeight = FontWeight.Medium
        )
      )
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Quick Tap Profile Chips
    Text(
      text = "QUICK FILL DEMO ACCOUNTS",
      style = MaterialTheme.typography.labelSmall.copy(
        color = TextMuted,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp,
        fontSize = 9.sp
      )
    )

    Spacer(modifier = Modifier.height(8.dp))

    FlowRow(
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      listOf(
        "majundasari7410@gmail.com" to "User Account",
        "trader.pro@marketpulse.in" to "Pro Trader",
        "analyst@dalalstreet.org" to "Analyst"
      ).forEach { (accountEmail, label) ->
        val isSelected = email == accountEmail
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) CyanTeal.copy(alpha = 0.2f) else SlateNavySurface)
            .border(
              1.dp,
              if (isSelected) CyanTeal else CardBorder,
              RoundedCornerShape(8.dp)
            )
            .clickable { onEmailChanged(accountEmail) }
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("quick_email_${accountEmail.substringBefore("@")}")
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.Star,
              contentDescription = null,
              tint = if (isSelected) CyanTeal else SecondaryBlue,
              modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
              text = "$label: $accountEmail",
              style = MaterialTheme.typography.labelSmall.copy(
                color = if (isSelected) CyanTeal else TextSecondary,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
              )
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Primary Action Button: Send OTP
    Button(
      onClick = {
        keyboardController?.hide()
        onRequestOtp()
      },
      enabled = isValidEmail && !isLoading,
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp)
        .testTag("send_otp_button"),
      shape = RoundedCornerShape(10.dp),
      colors = ButtonDefaults.buttonColors(
        containerColor = CyanTeal,
        contentColor = DeepNavyBg,
        disabledContainerColor = CardBorder,
        disabledContentColor = TextMuted
      )
    ) {
      if (isLoading) {
        CircularProgressIndicator(
          modifier = Modifier.size(20.dp),
          color = DeepNavyBg,
          strokeWidth = 2.dp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Dispatching OTP...",
          style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
        )
      } else {
        Text(
          text = "Send One-Time Password",
          style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
          )
        )
        Spacer(modifier = Modifier.width(6.dp))
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowForward,
          contentDescription = null,
          modifier = Modifier.size(16.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Guest Mode / Sandbox Access
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(8.dp))
        .background(SlateNavySurface)
        .clickable(onClick = onGuestLogin)
        .padding(vertical = 10.dp, horizontal = 12.dp)
        .testTag("guest_login_button"),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = "Or continue as Guest (Sandbox Demo)",
        style = MaterialTheme.typography.labelMedium.copy(
          color = SecondaryBlue,
          fontWeight = FontWeight.SemiBold
        )
      )
    }
  }
}

@Composable
private fun OtpVerificationContent(
  email: String,
  otp: String,
  generatedOtp: String,
  isLoading: Boolean,
  errorMessage: String?,
  onOtpChanged: (String) -> Unit,
  onVerifyOtp: (String) -> Unit,
  onResendOtp: () -> Unit,
  onBackToEmail: () -> Unit
) {
  val focusRequester = remember { FocusRequester() }
  val keyboardController = LocalSoftwareKeyboardController.current

  // 30 seconds countdown timer
  var secondsLeft by remember { mutableIntStateOf(30) }
  LaunchedEffect(key1 = email) {
    secondsLeft = 30
    while (secondsLeft > 0) {
      delay(1000)
      secondsLeft--
    }
  }

  // Auto-focus the OTP input when entering this step
  LaunchedEffect(Unit) {
    delay(200)
    focusRequester.requestFocus()
  }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .background(CardSurface)
      .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
      .padding(20.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Top Row with Back Button & Title
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(
        onClick = onBackToEmail,
        modifier = Modifier
          .size(32.dp)
          .testTag("otp_back_button")
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = "Back to Email",
          tint = TextSecondary,
          modifier = Modifier.size(18.dp)
        )
      }
      Spacer(modifier = Modifier.width(6.dp))
      Column {
        Text(
          text = "Enter Verification Code",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = TextPrimary
          )
        )
        Text(
          text = "Sent to $email",
          style = MaterialTheme.typography.bodySmall.copy(
            color = CyanTeal,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp
          )
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Interactive Demo HUD Pill: Tap to auto-fill generated OTP
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(10.dp))
        .background(CyanTeal.copy(alpha = 0.12f))
        .border(1.dp, CyanTeal.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
        .clickable {
          onOtpChanged(generatedOtp)
          keyboardController?.hide()
        }
        .padding(horizontal = 12.dp, vertical = 8.dp)
        .testTag("auto_fill_otp_chip"),
      contentAlignment = Alignment.Center
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
      ) {
        Icon(
          imageVector = Icons.Default.Key,
          contentDescription = null,
          tint = CyanTeal,
          modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "Demo OTP: ",
          style = MaterialTheme.typography.labelSmall.copy(
            color = TextSecondary,
            fontSize = 11.sp
          )
        )
        Text(
          text = generatedOtp,
          style = MaterialTheme.typography.labelSmall.copy(
            color = CyanTeal,
            fontWeight = FontWeight.Black,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
          )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(CyanTeal)
            .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Text(
            text = "TAP TO FILL",
            style = MaterialTheme.typography.labelSmall.copy(
              color = DeepNavyBg,
              fontWeight = FontWeight.Black,
              fontSize = 9.sp
            )
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // 6-Digit OTP Box Grid
    Box(
      modifier = Modifier.fillMaxWidth(),
      contentAlignment = Alignment.Center
    ) {
      // Hidden transparent BasicTextField capturing numeric keyboard input
      BasicTextField(
        value = otp,
        onValueChange = { input ->
          if (input.length <= 6 && input.all { it.isDigit() }) {
            onOtpChanged(input)
            if (input.length == 6) {
              keyboardController?.hide()
              onVerifyOtp(input)
            }
          }
        },
        keyboardOptions = KeyboardOptions(
          keyboardType = KeyboardType.NumberPassword,
          imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
          onDone = {
            keyboardController?.hide()
            if (otp.length == 6) {
              onVerifyOtp(otp)
            }
          }
        ),
        modifier = Modifier
          .focusRequester(focusRequester)
          .testTag("otp_hidden_input")
          .size(0.dp) // Invisible but focusable
      )

      // Visual 6-cell representation
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        for (i in 0 until 6) {
          val char = otp.getOrNull(i)?.toString() ?: ""
          val isCurrentIndex = otp.length == i
          val isFilled = otp.length > i

          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(SlateNavySurface)
              .border(
                width = if (isCurrentIndex) 1.5.dp else 1.dp,
                color = when {
                  isCurrentIndex -> CyanTeal
                  isFilled -> CyanTeal.copy(alpha = 0.6f)
                  else -> CardBorder
                },
                shape = RoundedCornerShape(8.dp)
              )
              .clickable { focusRequester.requestFocus() },
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = char,
              style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = if (isFilled) TextPrimary else TextMuted,
                fontFamily = FontFamily.Monospace,
                fontSize = 20.sp
              )
            )
          }
        }
      }
    }

    // Error message
    if (!errorMessage.isNullOrBlank()) {
      Spacer(modifier = Modifier.height(10.dp))
      Text(
        text = errorMessage,
        style = MaterialTheme.typography.labelSmall.copy(
          color = LossCrimson,
          fontWeight = FontWeight.Medium
        ),
        textAlign = TextAlign.Center
      )
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Verify & Sign In Action
    Button(
      onClick = {
        keyboardController?.hide()
        onVerifyOtp(otp)
      },
      enabled = otp.length == 6 && !isLoading,
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp)
        .testTag("verify_otp_button"),
      shape = RoundedCornerShape(10.dp),
      colors = ButtonDefaults.buttonColors(
        containerColor = CyanTeal,
        contentColor = DeepNavyBg,
        disabledContainerColor = CardBorder,
        disabledContentColor = TextMuted
      )
    ) {
      if (isLoading) {
        CircularProgressIndicator(
          modifier = Modifier.size(20.dp),
          color = DeepNavyBg,
          strokeWidth = 2.dp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Authenticating...",
          style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
        )
      } else {
        Text(
          text = "Verify OTP & Launch Terminal",
          style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
          )
        )
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Resend OTP Countdown / Action
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      TextButton(
        onClick = onBackToEmail,
        modifier = Modifier.testTag("change_email_button")
      ) {
        Text(
          text = "Change Email",
          style = MaterialTheme.typography.labelSmall.copy(
            color = TextSecondary,
            fontWeight = FontWeight.SemiBold
          )
        )
      }

      if (secondsLeft > 0) {
        Text(
          text = "Resend OTP in ${secondsLeft}s",
          style = MaterialTheme.typography.labelSmall.copy(
            color = TextMuted,
            fontWeight = FontWeight.Medium
          )
        )
      } else {
        TextButton(
          onClick = {
            secondsLeft = 30
            onResendOtp()
          },
          modifier = Modifier.testTag("resend_otp_button")
        ) {
          Text(
            text = "Resend Code",
            style = MaterialTheme.typography.labelSmall.copy(
              color = CyanTeal,
              fontWeight = FontWeight.Bold
            )
          )
        }
      }
    }
  }
}
