package sms.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateWalletTopUpRequest(
    val tenantCode: String,
    val amount: String,
    val email: String
)