package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import com.example.ui.theme.LocalThemeIsDark
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CyanTeal
import com.example.ui.theme.CyanTealLight
import com.example.ui.theme.DeepNavyBg
import com.example.ui.theme.GainEmerald
import com.example.ui.theme.LossCrimson
import com.example.ui.theme.RegulatoryAmber
import com.example.ui.theme.SecondaryBlue
import com.example.ui.theme.SlateNavySurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

/**
 * Dedicated UI Screen for the OTP Verification Flow in MarketPulse.
 *
 * Includes:
 * - Top back navigation with minimum 48dp touch target
 * - Security header with encrypted session status
 * - Target email verification pill with edit shortcut
 * - Interactive 6-digit PIN input field with blinking active cursor and custom animated boxes
 * - Countdown timer for re-sending the code with remaining seconds ring & action button
 * - Live Demo OTP auto-fill HUD badge for frictionless testing
 * - On-screen PIN pad alternative for tablet/emulator reliability
 * - Enterprise security footer compliance notice
 */
@Composable
fun OtpVerificationScreen(
  email: String,
  otp: String,
  generatedOtp: String,
  isLoading: Boolean,
  errorMessage: String?,
  onOtpChanged: (String) -> Unit,
  onVerifyOtp: (String) -> Unit,
  onResendOtp: () -> Unit,
  onBackToEmail: () -> Unit,
  onToggleTheme: () -> Unit = {},
  modifier: Modifier = Modifier,
  initialSeconds: Int = 30,
  canDismiss: Boolean = false,
  onDismiss: () -> Unit = {},
  onGuestLogin: (() -> Unit)? = null
) {
  // Intercept system back press to return cleanly to email entry
  BackHandler {
    onBackToEmail()
  }

  val focusRequester = remember { FocusRequester() }
  val keyboardController = LocalSoftwareKeyboardController.current

  // Countdown timer state
  var secondsLeft by remember { mutableIntStateOf(initialSeconds) }
  var timerIteration by remember { mutableIntStateOf(0) }
  var showResentConfirmation by remember { mutableStateOf(false) }
  var maskDigits by remember { mutableStateOf(false) }

  // Timer countdown coroutine
  LaunchedEffect(key1 = timerIteration) {
    secondsLeft = initialSeconds
    while (secondsLeft > 0) {
      delay(1000L)
      secondsLeft--
    }
  }

  // Auto-hide resent confirmation after 3 seconds
  LaunchedEffect(showResentConfirmation) {
    if (showResentConfirmation) {
      delay(3500L)
      showResentConfirmation = false
    }
  }

  // Auto-focus input when screen mounts
  LaunchedEffect(Unit) {
    delay(150L)
    try {
      focusRequester.requestFocus()
    } catch (_: Exception) {
      // Ignored if focus fails
    }
  }

  // Blinking cursor transition for focused PIN box
  val infiniteTransition = rememberInfiniteTransition(label = "cursor_blink")
  val cursorAlpha by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 0f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 600),
      repeatMode = RepeatMode.Reverse
    ),
    label = "cursor_alpha"
  )

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(DeepNavyBg)
      .imePadding()
  ) {
    // Ambient cyan accent glow
    Box(
      modifier = Modifier
        .size(360.dp)
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
        .padding(horizontal = 20.dp, vertical = 16.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // ==========================================
      // 1. TOP APP BAR / NAVIGATION
      // ==========================================
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 8.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = onBackToEmail,
            modifier = Modifier
              .size(48.dp)
              .clip(CircleShape)
              .background(SlateNavySurface)
              .border(1.dp, CardBorder, CircleShape)
              .testTag("otp_back_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back to Email",
              tint = TextPrimary,
              modifier = Modifier.size(20.dp)
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column {
            Text(
              text = "Two-Factor Auth",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(6.dp)
                  .clip(CircleShape)
                  .background(GainEmerald)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "TLS 1.3 SECURE SESSION",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = GainEmerald,
                  fontSize = 9.sp,
                  fontWeight = FontWeight.SemiBold,
                  letterSpacing = 0.5.sp
                )
              )
            }
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          val isDark = LocalThemeIsDark.current
          IconButton(
            onClick = onToggleTheme,
            modifier = Modifier
              .size(48.dp)
              .clip(CircleShape)
              .background(SlateNavySurface)
              .border(1.dp, CardBorder, CircleShape)
              .testTag("otp_theme_toggle_button")
          ) {
            Icon(
              imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
              contentDescription = if (isDark) "Switch to Light Mode" else "Switch to Dark Mode",
              tint = if (isDark) RegulatoryAmber else SecondaryBlue,
              modifier = Modifier.size(20.dp)
            )
          }

          if (canDismiss) {
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
              onClick = onDismiss,
              modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(SlateNavySurface)
                .border(1.dp, CardBorder, CircleShape)
                .testTag("otp_dismiss_button")
            ) {
              Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "Close",
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // ==========================================
      // 2. MAIN VERIFICATION CARD
      // ==========================================
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = CardSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          // Key Icon with glowing circle
          Box(
            modifier = Modifier
              .size(56.dp)
              .clip(CircleShape)
              .background(
                brush = Brush.radialGradient(
                  listOf(CyanTeal.copy(alpha = 0.25f), SlateNavySurface)
                )
              )
              .border(1.5.dp, CyanTeal.copy(alpha = 0.6f), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Security,
              contentDescription = "Security",
              tint = CyanTeal,
              modifier = Modifier.size(28.dp)
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          Text(
            text = "Enter 6-Digit Security Code",
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Bold,
              color = TextPrimary,
              fontSize = 20.sp
            ),
            textAlign = TextAlign.Center
          )

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = "We sent a 6-digit confirmation code to your authorized trading email address.",
            style = MaterialTheme.typography.bodySmall.copy(
              color = TextSecondary,
              lineHeight = 16.sp
            ),
            textAlign = TextAlign.Center
          )

          Spacer(modifier = Modifier.height(14.dp))

          // Target Email Pill with quick "Change" button
          Row(
            modifier = Modifier
              .clip(RoundedCornerShape(24.dp))
              .background(SlateNavySurface)
              .border(1.dp, CardBorder, RoundedCornerShape(24.dp))
              .clickable { onBackToEmail() }
              .padding(horizontal = 14.dp, vertical = 7.dp)
              .testTag("otp_email_pill"),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Lock,
              contentDescription = null,
              tint = CyanTeal,
              modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = email.ifBlank { "trader@marketpulse.in" },
              style = MaterialTheme.typography.labelSmall.copy(
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
              ),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
              imageVector = Icons.Default.Edit,
              contentDescription = "Change email",
              tint = SecondaryBlue,
              modifier = Modifier.size(12.dp)
            )
          }

          Spacer(modifier = Modifier.height(20.dp))

          // ==========================================
          // 3. INTERACTIVE 6-DIGIT PIN INPUT FIELD
          // ==========================================
          Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
          ) {
            // Hidden BasicTextField capturing system/hardware keyboard input
            BasicTextField(
              value = otp,
              onValueChange = { input ->
                val digitsOnly = input.filter { it.isDigit() }
                if (digitsOnly.length <= 6) {
                  onOtpChanged(digitsOnly)
                  if (digitsOnly.length == 6) {
                    keyboardController?.hide()
                    onVerifyOtp(digitsOnly)
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
                .size(0.dp)
            )

            // 6 Visual Pin Slots
            Row(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              for (index in 0 until 6) {
                val digit = otp.getOrNull(index)?.toString() ?: ""
                val isCurrent = otp.length == index
                val isFilled = otp.length > index
                val hasError = !errorMessage.isNullOrBlank()

                val boxBorderColor by animateColorAsState(
                  targetValue = when {
                    hasError -> LossCrimson
                    isCurrent -> CyanTeal
                    isFilled -> CyanTealLight.copy(alpha = 0.8f)
                    else -> CardBorder
                  },
                  label = "box_border_$index"
                )

                val boxBgColor by animateColorAsState(
                  targetValue = when {
                    hasError -> LossCrimson.copy(alpha = 0.08f)
                    isCurrent -> CyanTeal.copy(alpha = 0.12f)
                    isFilled -> SlateNavySurface
                    else -> SlateNavySurface
                  },
                  label = "box_bg_$index"
                )

                Box(
                  modifier = Modifier
                    .size(width = 46.dp, height = 54.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(boxBgColor)
                    .border(
                      width = if (isCurrent) 2.dp else 1.dp,
                      color = boxBorderColor,
                      shape = RoundedCornerShape(10.dp)
                    )
                    .clickable {
                      focusRequester.requestFocus()
                      keyboardController?.show()
                    }
                    .testTag("otp_pin_box_$index"),
                  contentAlignment = Alignment.Center
                ) {
                  if (digit.isNotEmpty()) {
                    Text(
                      text = if (maskDigits) "•" else digit,
                      style = MaterialTheme.typography.titleLarge.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = if (maskDigits) 24.sp else 22.sp
                      )
                    )
                  } else if (isCurrent) {
                    // Pulsating Cursor Line when slot is active
                    Box(
                      modifier = Modifier
                        .width(2.dp)
                        .height(24.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(CyanTeal.copy(alpha = cursorAlpha))
                    )
                  } else {
                    // Subtly visible slot placeholder dot
                    Box(
                      modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(TextMuted.copy(alpha = 0.4f))
                    )
                  }
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Mask/Reveal PIN toggle button
          Row(
            modifier = Modifier
              .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
              ) { maskDigits = !maskDigits }
              .padding(vertical = 4.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = if (maskDigits) Icons.Default.Visibility else Icons.Default.VisibilityOff,
              contentDescription = "Toggle mask",
              tint = TextMuted,
              modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (maskDigits) "Show Digits" else "Hide Digits",
              style = MaterialTheme.typography.labelSmall.copy(
                color = TextMuted,
                fontSize = 10.sp
              )
            )
          }

          // Error banner
          AnimatedVisibility(
            visible = !errorMessage.isNullOrBlank(),
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(LossCrimson.copy(alpha = 0.12f))
                .border(1.dp, LossCrimson.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = LossCrimson,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = errorMessage.orEmpty(),
                style = MaterialTheme.typography.labelSmall.copy(
                  color = LossCrimson,
                  fontWeight = FontWeight.Medium,
                  fontSize = 11.sp
                )
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // ==========================================
          // 4. DEMO OTP AUTO-FILL HUD PILL
          // ==========================================
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(CyanTeal.copy(alpha = 0.10f))
              .border(1.dp, CyanTeal.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
              .clickable {
                onOtpChanged(generatedOtp)
                keyboardController?.hide()
              }
              .padding(horizontal = 12.dp, vertical = 9.dp)
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
                modifier = Modifier.size(15.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Live Demo OTP: ",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = TextSecondary,
                  fontSize = 11.sp
                )
              )
              Text(
                text = generatedOtp,
                style = MaterialTheme.typography.labelMedium.copy(
                  color = CyanTeal,
                  fontWeight = FontWeight.Black,
                  fontFamily = FontFamily.Monospace,
                  letterSpacing = 1.2.sp,
                  fontSize = 13.sp
                )
              )
              Spacer(modifier = Modifier.width(10.dp))
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(CyanTeal)
                  .padding(horizontal = 7.dp, vertical = 3.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = null,
                    tint = DeepNavyBg,
                    modifier = Modifier.size(10.dp)
                  )
                  Spacer(modifier = Modifier.width(3.dp))
                  Text(
                    text = "AUTO-FILL",
                    style = MaterialTheme.typography.labelSmall.copy(
                      color = DeepNavyBg,
                      fontWeight = FontWeight.Black,
                      fontSize = 9.sp
                    )
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(20.dp))

          // ==========================================
          // 5. PRIMARY ACTION: VERIFY OTP BUTTON
          // ==========================================
          Button(
            onClick = {
              keyboardController?.hide()
              onVerifyOtp(otp)
            },
            enabled = otp.length == 6 && !isLoading,
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("verify_otp_button"),
            shape = RoundedCornerShape(12.dp),
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
                strokeWidth = 2.5.dp
              )
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = "Authenticating Session...",
                style = MaterialTheme.typography.labelLarge.copy(
                  fontWeight = FontWeight.Bold,
                  color = DeepNavyBg
                )
              )
            } else {
              Text(
                text = "Verify & Launch Terminal",
                style = MaterialTheme.typography.labelLarge.copy(
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 0.5.sp,
                  fontSize = 15.sp
                )
              )
              Spacer(modifier = Modifier.width(8.dp))
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // ==========================================
          // 6. COUNTDOWN TIMER & RESEND ACTION ROW
          // ==========================================
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .background(SlateNavySurface)
              .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Countdown Timer Display
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = null,
                tint = if (secondsLeft > 0) SecondaryBlue else CyanTeal,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))

              if (secondsLeft > 0) {
                Column {
                  Text(
                    text = "Resend code in",
                    style = MaterialTheme.typography.labelSmall.copy(
                      color = TextMuted,
                      fontSize = 10.sp
                    )
                  )
                  Text(
                    text = "00:${secondsLeft.toString().padStart(2, '0')}s",
                    style = MaterialTheme.typography.labelMedium.copy(
                      color = SecondaryBlue,
                      fontWeight = FontWeight.Bold,
                      fontFamily = FontFamily.Monospace,
                      fontSize = 12.sp
                    ),
                    modifier = Modifier.testTag("countdown_timer_text")
                  )
                }
              } else {
                Text(
                  text = "Code expired",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = RegulatoryAmber,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                  ),
                  modifier = Modifier.testTag("countdown_timer_text")
                )
              }
            }

            // Resend Button
            if (secondsLeft > 0) {
              Text(
                text = "Resend Disabled",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = TextMuted.copy(alpha = 0.6f),
                  fontSize = 11.sp
                )
              )
            } else {
              Button(
                onClick = {
                  timerIteration++
                  showResentConfirmation = true
                  onResendOtp()
                },
                modifier = Modifier
                  .height(36.dp)
                  .testTag("resend_otp_button"),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                  containerColor = CyanTeal.copy(alpha = 0.2f),
                  contentColor = CyanTeal
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                  horizontal = 12.dp,
                  vertical = 4.dp
                )
              ) {
                Icon(
                  imageVector = Icons.Default.Refresh,
                  contentDescription = null,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Resend Code",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                  )
                )
              }
            }
          }

          // Resent notification pill
          AnimatedVisibility(
            visible = showResentConfirmation,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(GainEmerald.copy(alpha = 0.15f))
                .border(1.dp, GainEmerald.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center
            ) {
              Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = GainEmerald,
                modifier = Modifier.size(13.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "New 6-digit OTP dispatched to $email",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = GainEmerald,
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 11.sp
                )
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // ==========================================
      // 7. COMPACT ON-SCREEN KEYPAD (PIN PAD)
      // ==========================================
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CardSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = "NUMERIC KEYPAD",
            style = MaterialTheme.typography.labelSmall.copy(
              color = TextMuted,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp,
              fontSize = 9.sp
            )
          )

          Spacer(modifier = Modifier.height(8.dp))

          val keyRows = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("CLEAR", "0", "BACK")
          )

          for (row in keyRows) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              for (key in row) {
                Box(
                  modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SlateNavySurface)
                    .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                    .clickable {
                      when (key) {
                        "CLEAR" -> onOtpChanged("")
                        "BACK" -> {
                          if (otp.isNotEmpty()) {
                            onOtpChanged(otp.dropLast(1))
                          }
                        }
                        else -> {
                          if (otp.length < 6) {
                            val nextOtp = otp + key
                            onOtpChanged(nextOtp)
                            if (nextOtp.length == 6) {
                              onVerifyOtp(nextOtp)
                            }
                          }
                        }
                      }
                    }
                    .testTag("keypad_key_$key"),
                  contentAlignment = Alignment.Center
                ) {
                  when (key) {
                    "BACK" -> {
                      Icon(
                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                        contentDescription = "Backspace",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                      )
                    }
                    "CLEAR" -> {
                      Text(
                        text = "CLEAR",
                        style = MaterialTheme.typography.labelSmall.copy(
                          color = TextMuted,
                          fontWeight = FontWeight.Bold,
                          fontSize = 10.sp
                        )
                      )
                    }
                    else -> {
                      Text(
                        text = key,
                        style = MaterialTheme.typography.titleMedium.copy(
                          color = TextPrimary,
                          fontWeight = FontWeight.Bold,
                          fontFamily = FontFamily.Monospace,
                          fontSize = 18.sp
                        )
                      )
                    }
                  }
                }
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // ==========================================
      // 8. ALTERNATIVE ACTIONS (Guest Sandbox / Change Email)
      // ==========================================
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        TextButton(
          onClick = onBackToEmail,
          modifier = Modifier.testTag("change_email_text_button")
        ) {
          Text(
            text = "← Use Different Email",
            style = MaterialTheme.typography.labelSmall.copy(
              color = SecondaryBlue,
              fontWeight = FontWeight.SemiBold
            )
          )
        }

        if (onGuestLogin != null) {
          TextButton(
            onClick = onGuestLogin,
            modifier = Modifier.testTag("otp_guest_fallback_button")
          ) {
            Text(
              text = "Skip to Guest Sandbox →",
              style = MaterialTheme.typography.labelSmall.copy(
                color = TextMuted,
                fontWeight = FontWeight.Medium
              )
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // ==========================================
      // 9. SECURITY & ADVISORY NOTICE
      // ==========================================
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
          contentDescription = null,
          tint = CyanTeal,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = "Security Advisory: MarketPulse will never ask for your verification PIN via telephone call, SMS, or external messaging links.",
          style = MaterialTheme.typography.labelSmall.copy(
            color = TextMuted,
            fontSize = 10.sp,
            lineHeight = 13.sp
          )
        )
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}
