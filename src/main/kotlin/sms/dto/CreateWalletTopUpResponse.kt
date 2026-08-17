package sms.dto



import kotlinx.serialization.Serializable

@Serializable
data class CreateWalletTopUpResponse(
    val success: Boolean,
    val message: String,
    val reference: String
)