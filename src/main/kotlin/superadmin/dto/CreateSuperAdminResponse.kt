package superadmin.dto



import kotlinx.serialization.Serializable

@Serializable
data class CreateSuperAdminResponse(
    val message: String,
    val id: Int
)