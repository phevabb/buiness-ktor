package sms.dto



import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class InitializeWalletTopUpRequest(
    val tenantCode: String,
    val schoolName: String? = null,
    val email: String,
    val amount: String
)