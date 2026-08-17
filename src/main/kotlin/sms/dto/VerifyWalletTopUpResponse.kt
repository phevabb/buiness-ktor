package sms.dto



import kotlinx.serialization.Serializable

@Serializable
data class VerifyWalletTopUpResponse(
    val success: Boolean,
    val message: String,
    val cashBalance: String? = null
)