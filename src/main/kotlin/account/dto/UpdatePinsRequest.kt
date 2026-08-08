package account.dto


import kotlinx.serialization.Serializable

@Serializable
data class UpdatePinsRequest(
    val tenantCode: String,
    val adminPin: String? = null,
    val principalPin: String? = null
)