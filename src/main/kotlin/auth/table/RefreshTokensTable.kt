package com.example.auth.table




import com.example.account.table.AccountsTable
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable

object RefreshTokensTable : IntIdTable("refresh_tokens") {
    val accountId = reference("account_id", AccountsTable)
    val token = varchar("token", 512).uniqueIndex()
    val isRevoked = bool("is_revoked").default(false)
    val expiresAtEpochMillis = long("expires_at_epoch_millis")
    val createdAtEpochMillis = long("created_at_epoch_millis")
}