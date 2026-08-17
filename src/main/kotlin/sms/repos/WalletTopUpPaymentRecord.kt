package sms.repos


import java.math.BigDecimal

data class WalletTopUpPaymentRecord(
    val id: Int,
    val tenantCode: String,
    val email: String,
    val amount: BigDecimal,
    val amountInPesewas: Int,
    val reference: String,
    val verified: Boolean,
    val walletCredited: Boolean,
    val status: String,
)