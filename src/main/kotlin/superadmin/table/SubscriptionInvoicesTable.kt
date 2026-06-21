package com.example.superadmin.table


import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import java.math.BigDecimal

object SubscriptionInvoicesTable : IntIdTable("subscription_invoices") {
    val accountId = integer("account_id")
    val tenantCode = varchar("tenant_code", 100)

    val academicYearId = integer("academic_year_id")
    val academicTermId = integer("academic_term_id")

    val studentCount = integer("student_count")

    // Cedis, not pesewas
    val amountPerStudentCedis = decimal("amount_per_student_cedis", 12, 2)
        .default(BigDecimal("5.00"))

    val totalAmountCedis = decimal("total_amount_cedis", 12, 2)

    // Real boolean you requested
    val isPaid = bool("is_paid").default(false)

    // free_trial, pending, paid, overdue, waived, cancelled
    val paymentStatus = varchar("payment_status", 50).default("pending")

    val paystackReference = varchar("paystack_reference", 150).nullable()

    val dueDateEpochMillis = long("due_date_epoch_millis")
    val paidAtEpochMillis = long("paid_at_epoch_millis").nullable()
    val createdAtEpochMillis = long("created_at_epoch_millis")

    init {
        uniqueIndex("ux_invoice_account_term", accountId, academicTermId)
    }
}