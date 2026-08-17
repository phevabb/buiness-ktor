package sms.dto



import kotlinx.serialization.Serializable

@Serializable
data class VerifyWalletTopUpRequest(
    val tenantCode: String,
    val reference: String
)