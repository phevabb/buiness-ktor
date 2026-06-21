package com.example.superadmin.repos




import org.jetbrains.exposed.v1.core.eq
import com.example.account.table.AccountsTable
import com.example.superadmin.dto.CreateAcademicYearRequest
import com.example.superadmin.dto.CurrentBillingResponse
import com.example.superadmin.table.AcademicTermsTable
import com.example.superadmin.table.AcademicYearsTable
import com.example.superadmin.table.SubscriptionInvoicesTable
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.core.lessEq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.math.BigDecimal

object BillingRepository {

    fun createAcademicYearWithTerms(request: CreateAcademicYearRequest): Int = transaction {
        val now = System.currentTimeMillis()

        val academicYearId = AcademicYearsTable.insertAndGetId {
            it[name] = request.name
            it[startDateEpochMillis] = request.startDateEpochMillis
            it[endDateEpochMillis] = request.endDateEpochMillis
            it[status] = "active"
            it[createdAtEpochMillis] = now
        }.value

        request.terms.forEach { term ->
            AcademicTermsTable.insert {
                it[AcademicTermsTable.academicYearId] = academicYearId
                it[termCode] = term.termCode
                it[termName] = term.termName
                it[termNumber] = term.termNumber
                it[reopeningDateEpochMillis] = term.reopeningDateEpochMillis
                it[closingDateEpochMillis] = term.closingDateEpochMillis
                it[vacationStartDateEpochMillis] = term.vacationStartDateEpochMillis
                it[vacationEndDateEpochMillis] = term.vacationEndDateEpochMillis
                it[graceStartDateEpochMillis] = term.graceStartDateEpochMillis
                it[graceEndDateEpochMillis] = term.graceEndDateEpochMillis
                it[paymentDeadlineEpochMillis] = term.paymentDeadlineEpochMillis
                it[amountPerStudentCedis] = BigDecimal(term.amountPerStudentCedis)
                it[status] = "active"
                it[createdAtEpochMillis] = now
            }
        }

        academicYearId
    }

    fun getCurrentBillingForAccount(accountId: Int): CurrentBillingResponse? = transaction {
        val now = System.currentTimeMillis()

        val account = AccountsTable
            .selectAll()
            .where { AccountsTable.id eq accountId }
            .singleOrNull()
            ?: return@transaction null

        val currentTerm = AcademicTermsTable
            .selectAll()
            .where {
                (AcademicTermsTable.reopeningDateEpochMillis lessEq now) and
                        (AcademicTermsTable.closingDateEpochMillis greaterEq now)
            }
            .singleOrNull()
            ?: return@transaction null

        val currentAcademicYearId = currentTerm[AcademicTermsTable.academicYearId]
        val currentTermId = currentTerm[AcademicTermsTable.id].value

        val existingInvoice = SubscriptionInvoicesTable
            .selectAll()
            .where {
                (SubscriptionInvoicesTable.accountId eq accountId) and
                        (SubscriptionInvoicesTable.academicTermId eq currentTermId)
            }
            .singleOrNull()

        val invoice = existingInvoice ?: run {
            val isFirstTime = account[AccountsTable.trialTermId] == null
            val studentCount = account[AccountsTable.estimatedStudents]
            val amountPerStudent = currentTerm[AcademicTermsTable.amountPerStudentCedis]
            val totalAmount = amountPerStudent.multiply(BigDecimal(studentCount))

            val status = if (isFirstTime) "free_trial" else "pending"
            val isPaid = false

            val invoiceId = SubscriptionInvoicesTable.insertAndGetId {
                it[SubscriptionInvoicesTable.accountId] = accountId
                it[tenantCode] = account[AccountsTable.tenantCode]
                it[academicYearId] = currentAcademicYearId
                it[academicTermId] = currentTermId
                it[SubscriptionInvoicesTable.studentCount] = studentCount
                it[amountPerStudentCedis] = amountPerStudent
                it[totalAmountCedis] = if (isFirstTime) BigDecimal.ZERO else totalAmount
                it[SubscriptionInvoicesTable.isPaid] = isPaid
                it[paymentStatus] = status
                it[dueDateEpochMillis] = currentTerm[AcademicTermsTable.paymentDeadlineEpochMillis]
                it[paidAtEpochMillis] = null
                it[createdAtEpochMillis] = now
            }.value

            if (isFirstTime) {
                AccountsTable.update({ AccountsTable.id eq accountId }) {
                    it[trialAcademicYearId] = currentAcademicYearId
                    it[trialTermId] = currentTermId
                    it[trialStartedAtEpochMillis] = currentTerm[AcademicTermsTable.reopeningDateEpochMillis]
                    it[trialEndedAtEpochMillis] = currentTerm[AcademicTermsTable.closingDateEpochMillis]
                    it[billingStatus] = "trial"
                }
            } else {
                AccountsTable.update({ AccountsTable.id eq accountId }) {
                    it[billingStatus] = "pending_payment"
                }
            }

            SubscriptionInvoicesTable
                .selectAll()
                .where { SubscriptionInvoicesTable.id eq invoiceId }
                .single()
        }

        val paymentStatus = invoice[SubscriptionInvoicesTable.paymentStatus]
        val paid = invoice[SubscriptionInvoicesTable.isPaid]

        CurrentBillingResponse(
            accountId = accountId,
            tenantCode = invoice[SubscriptionInvoicesTable.tenantCode],
            academicYearId = invoice[SubscriptionInvoicesTable.academicYearId],
            academicTermId = invoice[SubscriptionInvoicesTable.academicTermId],
            termName = currentTerm[AcademicTermsTable.termName],
            studentCount = invoice[SubscriptionInvoicesTable.studentCount],
            amountPerStudentCedis = invoice[SubscriptionInvoicesTable.amountPerStudentCedis].toPlainString(),
            totalAmountCedis = invoice[SubscriptionInvoicesTable.totalAmountCedis].toPlainString(),
            isPaid = paid,
            paymentStatus = paymentStatus,
            canMakePayment = !paid && paymentStatus == "pending",
            dueDateEpochMillis = invoice[SubscriptionInvoicesTable.dueDateEpochMillis]
        )
    }

    fun markInvoicePaidByPaystack(reference: String): Boolean = transaction {
        val invoice = SubscriptionInvoicesTable
            .selectAll()
            .where { SubscriptionInvoicesTable.paystackReference eq reference }
            .singleOrNull()
            ?: return@transaction false

        val now = System.currentTimeMillis()
        val invoiceId = invoice[SubscriptionInvoicesTable.id].value
        val accountId = invoice[SubscriptionInvoicesTable.accountId]
        val academicYearId = invoice[SubscriptionInvoicesTable.academicYearId]
        val academicTermId = invoice[SubscriptionInvoicesTable.academicTermId]

        val updatedInvoiceRows = SubscriptionInvoicesTable.update({
            SubscriptionInvoicesTable.id eq invoiceId
        }) {
            it[isPaid] = true
            it[paymentStatus] = "paid"
            it[paidAtEpochMillis] = now
        }

        AccountsTable.update({
            AccountsTable.id eq accountId
        }) {
            it[billingStatus] = "active"
            it[lastPaidAcademicYearId] = academicYearId
            it[lastPaidTermId] = academicTermId
            it[lastPaymentAtEpochMillis] = now
            it[isActive] = true
            it[tenantStatus] = "active"
        }

        updatedInvoiceRows > 0
    }}