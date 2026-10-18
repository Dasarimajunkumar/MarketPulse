package com.example.data.model

/**
 * Represents the authenticated trader session in MarketPulse.
 */
data class UserSession(
  val email: String,
  val displayName: String,
  val traderId: String,
  val isVerified: Boolean = true,
  val isGuest: Boolean = false,
  val loginTimestamp: Long = System.currentTimeMillis(),
  val kycTier: String = "Tier-2 Institutional Paper Trading"
)

/**
 * Authentication flow steps
 */
enum class AuthStep {
  EMAIL_INPUT,
  OTP_VERIFY,
  AUTHENTICATED
}

/**
 * State for Login and OTP verification UI
 */
data class AuthUiState(
  val step: AuthStep = AuthStep.EMAIL_INPUT,
  val emailInput: String = "",
  val otpInput: String = "",
  val generatedOtp: String = "482910",
  val isLoading: Boolean = false,
  val errorMessage: String? = null,
  val cooldownSeconds: Int = 0,
  val activeUser: UserSession? = null
)
