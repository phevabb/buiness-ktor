package com.example.superadmin.table



import org.jetbrains.exposed.v1.core.dao.id.IntIdTable

object AdminAuditLogsTable : IntIdTable("admin_audit_logs") {
    val adminId = integer("admin_id")
    val action = varchar("action", 150)
    val targetType = varchar("target_type", 100)
    val targetId = varchar("target_id", 100).nullable()
    val description = text("description").nullable()
    val createdAtEpochMillis = long("created_at_epoch_millis")
}