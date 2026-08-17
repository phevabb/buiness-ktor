package account.dto



import kotlinx.serialization.Serializable

@Serializable
data class AccountEmailResponse(
    val tenantCode: String,
    val email: String
)