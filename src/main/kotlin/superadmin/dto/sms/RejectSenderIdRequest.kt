package superadmin.dto.sms




import kotlinx.serialization.Serializable

@Serializable
data class RejectSenderIdRequest(
    val rejectionReason: String
)