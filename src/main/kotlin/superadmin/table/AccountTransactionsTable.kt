package com.example.superadmin.table



import org.jetbrains.exposed.v1.core.dao.id.IntIdTable

object AccountTransactionsTable : IntIdTable("account_transactions") {
    val accountId = integer("account_id")
    val tenantCode = varchar("tenant_code", 100)

    val amountPesewas = long("amount_pesewas")
    val currency = varchar("currency", 10).default("GHS")

    val provider = varchar("provider", 50).nullable()
    val providerReference = varchar("provider_reference", 150).nullable()
    val internalReference = varchar("internal_reference", 150).uniqueIndex()

    val status = varchar("status", 50).default("pending")
    val description = text("description").nullable()

    val createdAtEpochMillis = long("created_at_epoch_millis")
    val paidAtEpochMillis = long("paid_at_epoch_millis").nullable()
}