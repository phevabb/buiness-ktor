package com.example.superadmin.table



import org.jetbrains.exposed.v1.core.dao.id.IntIdTable

object PaymentTransactionsTable : IntIdTable("payment_transactions") {
    val invoiceId = integer("invoice_id")
    val accountId = integer("account_id")
    val tenantCode = varchar("tenant_code", 100)

    val provider = varchar("provider", 50).default("paystack")

    val paystackReference = varchar("paystack_reference", 150).uniqueIndex()
    val paystackAccessCode = varchar("paystack_access_code", 255).nullable()
    val paystackAuthorizationUrl = varchar("paystack_authorization_url", 500).nullable()

    // Store cedis as string-friendly decimal in invoice table.
    // Here keep transaction amount as text to avoid serialization issues.
    val amountCedis = decimal("amount_cedis", 12, 2)

    val currency = varchar("currency", 10).default("GHS")

    // pending, successful, failed, abandoned
    val status = varchar("status", 50).default("pending")

    val rawResponse = text("raw_response").nullable()

    val createdAtEpochMillis = long("created_at_epoch_millis")
    val paidAtEpochMillis = long("paid_at_epoch_millis").nullable()
}