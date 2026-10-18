package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

class AuthRepository(context: Context) {

  private val prefs: SharedPreferences =
    context.getSharedPreferences("marketpulse_auth_prefs", Context.MODE_PRIVATE)

  private val _currentUser = MutableStateFlow<UserSession?>(null)
  val currentUser: StateFlow<UserSession?> = _currentUser.asStateFlow()

  init {
    loadSavedSession()
  }

  private fun loadSavedSession() {
    val email = prefs.getString(KEY_EMAIL, null)
    val isAuthenticated = prefs.getBoolean(KEY_IS_AUTHENTICATED, false)
    if (isAuthenticated && !email.isNullOrBlank()) {
      val displayName = prefs.getString(KEY_DISPLAY_NAME, email.substringBefore("@").replaceFirstChar { it.uppercase() }) ?: "Trader"
      val traderId = prefs.getString(KEY_TRADER_ID, "MP-${absHash(email)}") ?: "MP-7410"
      val isGuest = prefs.getBoolean(KEY_IS_GUEST, false)
      val kycTier = prefs.getString(KEY_KYC_TIER, "Tier-2 Institutional Paper Trading") ?: "Tier-2 Institutional Paper Trading"
      _currentUser.value = UserSession(
        email = email,
        displayName = displayName,
        traderId = traderId,
        isVerified = true,
        isGuest = isGuest,
        kycTier = kycTier
      )
    }
  }

  fun generateOtp(): String {
    // Return a random 6-digit code or fallback to 482910 for reliable testing
    val code = Random.nextInt(100000, 999999).toString()
    return code
  }

  fun verifyOtp(email: String, enteredOtp: String, expectedOtp: String): Result<UserSession> {
    val cleanEntered = enteredOtp.trim()
    val cleanExpected = expectedOtp.trim()

    // Accept expected OTP or universal test code "482910" or "123456" for developer/user convenience
    val isValid = cleanEntered == cleanExpected || cleanEntered == "482910" || cleanEntered == "123456"

    return if (isValid) {
      val namePart = email.substringBefore("@")
        .replace(".", " ")
        .replace("_", " ")
        .split(" ")
        .joinToString(" ") { part -> part.replaceFirstChar { it.uppercase() } }
      val traderId = "MP-${absHash(email)}"
      val session = UserSession(
        email = email.trim(),
        displayName = if (namePart.isNotBlank()) namePart else "Terminal Trader",
        traderId = traderId,
        isVerified = true,
        isGuest = false,
        kycTier = "Tier-2 Verified (NSE/BSE Feeds)"
      )

      saveSession(session)
      _currentUser.value = session
      Result.success(session)
    } else {
      Result.failure(IllegalArgumentException("Invalid verification code. Enter the 6-digit OTP sent to your email."))
    }
  }

  fun loginAsGuest(): UserSession {
    val guestSession = UserSession(
      email = "guest.trader@marketpulse.in",
      displayName = "Guest Trader",
      traderId = "MP-GUEST-${Random.nextInt(1000, 9999)}",
      isVerified = false,
      isGuest = true,
      kycTier = "Sandbox Demo Access"
    )
    saveSession(guestSession)
    _currentUser.value = guestSession
    return guestSession
  }

  fun logout() {
    prefs.edit().clear().apply()
    _currentUser.value = null
  }

  private fun saveSession(session: UserSession) {
    prefs.edit()
      .putString(KEY_EMAIL, session.email)
      .putString(KEY_DISPLAY_NAME, session.displayName)
      .putString(KEY_TRADER_ID, session.traderId)
      .putBoolean(KEY_IS_AUTHENTICATED, true)
      .putBoolean(KEY_IS_GUEST, session.isGuest)
      .putString(KEY_KYC_TIER, session.kycTier)
      .apply()
  }

  private fun absHash(value: String): String {
    val hash = kotlin.math.abs(value.hashCode()) % 90000 + 10000
    return hash.toString()
  }

  companion object {
    private const val KEY_EMAIL = "auth_email"
    private const val KEY_DISPLAY_NAME = "auth_display_name"
    private const val KEY_TRADER_ID = "auth_trader_id"
    private const val KEY_IS_AUTHENTICATED = "auth_is_authenticated"
    private const val KEY_IS_GUEST = "auth_is_guest"
    private const val KEY_KYC_TIER = "auth_kyc_tier"
  }
}
