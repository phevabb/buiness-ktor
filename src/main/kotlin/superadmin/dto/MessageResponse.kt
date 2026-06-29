package superadmin.dto



import kotlinx.serialization.Serializable

@Serializable
data class MessageResponse(
    val message: String
)