package sms.dto


import kotlinx.serialization.Serializable

@Serializable
data class SmsMessageResponse(
    val success: Boolean,
    val message: String
)