package com.example.superadmin.table



import org.jetbrains.exposed.v1.core.dao.id.IntIdTable

object AcademicYearsTable : IntIdTable("academic_years") {
    val name = varchar("name", 30).uniqueIndex() // Example: 2026/2027

    val startDateEpochMillis = long("start_date_epoch_millis")
    val endDateEpochMillis = long("end_date_epoch_millis")

    // upcoming, active, closed
    val status = varchar("status", 30).default("upcoming")

    val createdAtEpochMillis = long("created_at_epoch_millis")
}