package sms.dto



import kotlinx.serialization.Serializable

@Serializable
data class InitializeWalletTopUpResponse(
    val success: Boolean,
    val message: String,
    val authorizationUrl: String,
    val accessCode: String,
    val reference: String
)