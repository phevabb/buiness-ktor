package com.example.superadmin.table



import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import java.math.BigDecimal

object AcademicTermsTable : IntIdTable("academic_terms") {
    val academicYearId = integer("academic_year_id")

    // FIRST_TERM, SECOND_TERM, THIRD_TERM
    val termCode = varchar("term_code", 30)

    // First Term, Second Term, Third Term
    val termName = varchar("term_name", 100)

    // 1, 2, 3
    val termNumber = integer("term_number")

    val reopeningDateEpochMillis = long("reopening_date_epoch_millis")
    val closingDateEpochMillis = long("closing_date_epoch_millis")

    val vacationStartDateEpochMillis = long("vacation_start_date_epoch_millis").nullable()
    val vacationEndDateEpochMillis = long("vacation_end_date_epoch_millis").nullable()

    val graceStartDateEpochMillis = long("grace_start_date_epoch_millis")
    val graceEndDateEpochMillis = long("grace_end_date_epoch_millis")

    val paymentDeadlineEpochMillis = long("payment_deadline_epoch_millis")

    // Store as cedis: 5.00
    val amountPerStudentCedis = decimal("amount_per_student_cedis", 12, 2)
        .default(BigDecimal("5.00"))

    // upcoming, active, closed
    val status = varchar("status", 30).default("upcoming")

    val createdAtEpochMillis = long("created_at_epoch_millis")

    init {
        uniqueIndex("ux_academic_year_term", academicYearId, termCode)
    }
}