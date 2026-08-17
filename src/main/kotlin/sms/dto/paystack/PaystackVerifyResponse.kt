package sms.dto.paystack


import kotlinx.serialization.Serializable

@Serializable
data class PaystackVerifyResponse(
    val status: Boolean,
    val message: String,
    val data: PaystackVerifyData
)

@Serializable
data class PaystackVerifyData(
    val status: String,
    val reference: String,
    val amount: Int,
    val currency: String? = null
)