package account.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateTenantPinsRequest(
    val tenantCode: String,
    val adminPin: String?,
    val principalPin: String?
)