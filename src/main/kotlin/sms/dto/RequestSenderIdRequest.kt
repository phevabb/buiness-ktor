package sms.dto



import kotlinx.serialization.Serializable

@Serializable
data class RequestSenderIdRequest(
    val tenantCode: String,
    val schoolName: String,
    val senderId: String,
    val reason: String? = null
)