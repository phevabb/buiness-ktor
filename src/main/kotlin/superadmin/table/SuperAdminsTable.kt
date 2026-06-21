package com.example.superadmin.table



import org.jetbrains.exposed.v1.core.dao.id.IntIdTable

object SuperAdminsTable : IntIdTable("super_admins") {
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val fullName = varchar("full_name", 255)
    val isActive = bool("is_active").default(true)
    val createdAtEpochMillis = long("created_at_epoch_millis")
}