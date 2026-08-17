package sms.dto.paystack



import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaystackInitializeResponse(
    val status: Boolean,
    val message: String,
    val data: PaystackInitializeData
)

@Serializable
data class PaystackInitializeData(
    @SerialName("authorization_url")
    val authorizationUrl: String,

    @SerialName("access_code")
    val accessCode: String,

    val reference: String
)