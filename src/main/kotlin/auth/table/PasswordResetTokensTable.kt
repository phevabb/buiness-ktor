package com.example.auth.table



import com.example.account.table.AccountsTable

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable

object PasswordResetTokensTable : IntIdTable("password_reset_tokens") {
    val accountId = reference("account_id", AccountsTable)
    val token = varchar("token", 255).uniqueIndex()
    val expiresAtEpochMillis = long("expires_at_epoch_millis")
    val usedAtEpochMillis = long("used_at_epoch_millis").nullable()
    val createdAtEpochMillis = long("created_at_epoch_millis")
}