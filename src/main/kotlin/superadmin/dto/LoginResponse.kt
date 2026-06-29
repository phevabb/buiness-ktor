package superadmin.dto


import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val message: String,
    val token: String
)

