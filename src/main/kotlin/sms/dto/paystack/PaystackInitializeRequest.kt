package sms.dto.paystack



import kotlinx.serialization.Serializable

@Serializable
data class PaystackInitializeRequest(
    val email: String,
    val amount: Int,
    val reference: String,
    val callback_url: String
)