package com.example.superadmin.repos

import com.example.superadmin.dto.AcademicYearCalendarResponse
import com.example.account.table.AccountsTable

import com.example.superadmin.dto.AcademicTermCalendarResponse
import com.example.superadmin.table.AcademicYearsTable
import com.example.superadmin.dto.CreateAcademicYearRequest
import com.example.superadmin.dto.SuperAdminInvoiceResponse
import com.example.superadmin.dto.CurrentBillingResponse
import com.example.superadmin.dto.SuperAdminTransactionResponse
import com.example.superadmin.table.AcademicTermsTable
import com.example.superadmin.table.PaymentTransactionsTable
import com.example.superadmin.table.SubscriptionInvoicesTable
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.core.less
import org.jetbrains.exposed.v1.core.lessEq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.select
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
                it[AcademicTermsTable.termCode] = term.termCode
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

    fun updateAcademicYearCalendar(
        academicYearId: Int,
        request: CreateAcademicYearRequest
    ): Boolean = transaction {
        val existingYear = AcademicYearsTable
            .selectAll()
            .where { AcademicYearsTable.id eq academicYearId }
            .singleOrNull()
            ?: return@transaction false

        val now = System.currentTimeMillis()

        AcademicYearsTable.update({ AcademicYearsTable.id eq academicYearId }) {
            it[name] = request.name
            it[startDateEpochMillis] = request.startDateEpochMillis
            it[endDateEpochMillis] = request.endDateEpochMillis

            /**
             * Keep existing status instead of forcing active.
             */
            it[status] = existingYear[AcademicYearsTable.status]
        }

        /**
         * Simple clean update:
         * Delete existing terms for this academic year,
         * then recreate the 3 terms from the request.
         *
         * This is okay if invoices are not yet linked.
         * If invoices already exist for this year/terms, use a safer update-by-termCode approach.
         */
        AcademicTermsTable.deleteWhere {
            AcademicTermsTable.academicYearId eq academicYearId
        }

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
                it[amountPerStudentCedis] = term.amountPerStudentCedis.toBigDecimal()

                it[status] = "active"
                it[createdAtEpochMillis] = now
            }
        }

        true
    }


    fun deleteAcademicYearCalendar(
        academicYearId: Int
    ): Boolean = transaction {
        val existingYear = AcademicYearsTable
            .selectAll()
            .where { AcademicYearsTable.id eq academicYearId }
            .singleOrNull()
            ?: return@transaction false

        /**
         * Important safety check:
         * Do not delete an academic year if invoices already exist for it.
         * This prevents breaking billing history.
         */
        val hasInvoices = SubscriptionInvoicesTable
            .selectAll()
            .where { SubscriptionInvoicesTable.academicYearId eq academicYearId }
            .limit(1)
            .any()

        if (hasInvoices) {
            error("Cannot delete academic year because invoices already exist for this academic year.")
        }

        AcademicTermsTable.deleteWhere {
            AcademicTermsTable.academicYearId eq academicYearId
        }

        AcademicYearsTable.deleteWhere {
            AcademicYearsTable.id eq academicYearId
        }

        true
    }

    fun listAcademicYearCalendars(
        search: String? = null
    ): List<AcademicYearCalendarResponse> = transaction {
        val years = AcademicYearsTable
            .selectAll()
            .orderBy(AcademicYearsTable.startDateEpochMillis, SortOrder.DESC)
            .map { yearRow ->
                val academicYearId = yearRow[AcademicYearsTable.id].value

                val terms = AcademicTermsTable
                    .selectAll()
                    .where { AcademicTermsTable.academicYearId eq academicYearId }
                    .orderBy(AcademicTermsTable.termNumber, SortOrder.ASC)
                    .map { termRow ->
                        AcademicTermCalendarResponse(
                            id = termRow[AcademicTermsTable.id].value,
                            academicYearId = termRow[AcademicTermsTable.academicYearId],
                            termCode = termRow[AcademicTermsTable.termCode],
                            termName = termRow[AcademicTermsTable.termName],
                            termNumber = termRow[AcademicTermsTable.termNumber],

                            reopeningDateEpochMillis = termRow[AcademicTermsTable.reopeningDateEpochMillis],
                            closingDateEpochMillis = termRow[AcademicTermsTable.closingDateEpochMillis],

                            vacationStartDateEpochMillis = termRow[AcademicTermsTable.vacationStartDateEpochMillis],
                            vacationEndDateEpochMillis = termRow[AcademicTermsTable.vacationEndDateEpochMillis],

                            graceStartDateEpochMillis = termRow[AcademicTermsTable.graceStartDateEpochMillis],
                            graceEndDateEpochMillis = termRow[AcademicTermsTable.graceEndDateEpochMillis],

                            paymentDeadlineEpochMillis = termRow[AcademicTermsTable.paymentDeadlineEpochMillis],

                            amountPerStudentCedis = termRow[AcademicTermsTable.amountPerStudentCedis].toPlainString(),

                            status = termRow[AcademicTermsTable.status],
                            createdAtEpochMillis = termRow[AcademicTermsTable.createdAtEpochMillis]
                        )
                    }

                AcademicYearCalendarResponse(
                    id = academicYearId,
                    name = yearRow[AcademicYearsTable.name],
                    startDateEpochMillis = yearRow[AcademicYearsTable.startDateEpochMillis],
                    endDateEpochMillis = yearRow[AcademicYearsTable.endDateEpochMillis],
                    status = yearRow[AcademicYearsTable.status],
                    createdAtEpochMillis = yearRow[AcademicYearsTable.createdAtEpochMillis],
                    terms = terms
                )
            }

        val normalizedSearch = search?.trim()?.lowercase()

        if (normalizedSearch.isNullOrBlank()) {
            years
        } else {
            years.filter { year ->
                val searchableText = listOf(
                    year.name,
                    year.status,
                    year.terms.joinToString(" ") { term ->
                        "${term.termCode} ${term.termName} ${term.status}"
                    }
                )
                    .joinToString(" ")
                    .lowercase()

                searchableText.contains(normalizedSearch)
            }
        }
    }




    fun getCurrentBillingForAccount(
        accountId: Int,
        dateEpochMillis: Long? = null
    ): CurrentBillingResponse? = transaction {
        val now = dateEpochMillis ?: System.currentTimeMillis()

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

        val currentTermId = currentTerm[AcademicTermsTable.id].value
        val currentAcademicYearId = currentTerm[AcademicTermsTable.academicYearId]

        val academicYear = AcademicYearsTable
            .selectAll()
            .where { AcademicYearsTable.id eq currentAcademicYearId }
            .singleOrNull()
            ?: return@transaction null

        val existingInvoice = SubscriptionInvoicesTable
            .selectAll()
            .where {
                (SubscriptionInvoicesTable.accountId eq accountId) and
                        (SubscriptionInvoicesTable.academicTermId eq currentTermId)
            }
            .singleOrNull()

        val invoice = existingInvoice ?: createInvoiceForCurrentTerm(
            accountId = accountId,
            accountTenantCode = account[AccountsTable.tenantCode],
            estimatedStudents = account[AccountsTable.estimatedStudents],
            currentAcademicYearId = currentAcademicYearId,
            currentTermId = currentTermId,
            amountPerStudentCedis = currentTerm[AcademicTermsTable.amountPerStudentCedis],
            dueDateEpochMillis = currentTerm[AcademicTermsTable.paymentDeadlineEpochMillis],
            isFirstTime = account[AccountsTable.trialTermId] == null,
            now = System.currentTimeMillis(),
            termStart = currentTerm[AcademicTermsTable.reopeningDateEpochMillis],
            termEnd = currentTerm[AcademicTermsTable.closingDateEpochMillis]
        )

        val invoiceId = invoice[SubscriptionInvoicesTable.id].value
        val paymentStatus = invoice[SubscriptionInvoicesTable.paymentStatus]
        val isPaid = invoice[SubscriptionInvoicesTable.isPaid]

        val canMakePayment = paymentStatus == "pending" || paymentStatus == "overdue"

        val buttonLabel = when {
            isPaid -> "Paid"
            paymentStatus == "free_trial" -> "Free Trial Active"
            paymentStatus == "overdue" -> "Pay Now To Reactivate"
            paymentStatus == "pending" -> "Make Payment"
            else -> "Unavailable"
        }

        CurrentBillingResponse(
            accountId = accountId,
            tenantCode = invoice[SubscriptionInvoicesTable.tenantCode],

            invoiceId = invoiceId,

            academicYearId = currentAcademicYearId,
            academicYearName = academicYear[AcademicYearsTable.name],

            academicTermId = currentTermId,
            termCode = currentTerm[AcademicTermsTable.termCode],
            termName = currentTerm[AcademicTermsTable.termName],
            termNumber = currentTerm[AcademicTermsTable.termNumber],

            studentCount = invoice[SubscriptionInvoicesTable.studentCount],
            amountPerStudentCedis = invoice[SubscriptionInvoicesTable.amountPerStudentCedis].toPlainString(),
            totalAmountCedis = invoice[SubscriptionInvoicesTable.totalAmountCedis].toPlainString(),

            isPaid = isPaid,
            paymentStatus = paymentStatus,

            canMakePayment = canMakePayment,
            paymentButtonLabel = buttonLabel,

            dueDateEpochMillis = invoice[SubscriptionInvoicesTable.dueDateEpochMillis],
            graceStartDateEpochMillis = currentTerm[AcademicTermsTable.graceStartDateEpochMillis],
            graceEndDateEpochMillis = currentTerm[AcademicTermsTable.graceEndDateEpochMillis]
        )
    }

    private fun createInvoiceForCurrentTerm(
        accountId: Int,
        accountTenantCode: String,
        estimatedStudents: Int,
        currentAcademicYearId: Int,
        currentTermId: Int,
        amountPerStudentCedis: BigDecimal,
        dueDateEpochMillis: Long,
        isFirstTime: Boolean,
        now: Long,
        termStart: Long,
        termEnd: Long
    ) = run {
        val totalAmount = amountPerStudentCedis.multiply(BigDecimal(estimatedStudents))

        val paymentStatus = if (isFirstTime) {
            "free_trial"
        } else {
            "pending"
        }

        val finalTotalAmount = if (isFirstTime) {
            BigDecimal("0.00")
        } else {
            totalAmount
        }

        val invoiceId = SubscriptionInvoicesTable.insertAndGetId {
            it[SubscriptionInvoicesTable.accountId] = accountId
            it[tenantCode] = accountTenantCode
            it[academicYearId] = currentAcademicYearId
            it[academicTermId] = currentTermId
            it[studentCount] = estimatedStudents
            it[SubscriptionInvoicesTable.amountPerStudentCedis] = amountPerStudentCedis
            it[totalAmountCedis] = finalTotalAmount
            it[isPaid] = false
            it[SubscriptionInvoicesTable.paymentStatus] = paymentStatus
            it[paystackReference] = null
            it[SubscriptionInvoicesTable.dueDateEpochMillis] = dueDateEpochMillis
            it[paidAtEpochMillis] = null
            it[createdAtEpochMillis] = now
        }.value

        if (isFirstTime) {
            AccountsTable.update({ AccountsTable.id eq accountId }) {
                it[trialAcademicYearId] = currentAcademicYearId
                it[trialTermId] = currentTermId
                it[trialStartedAtEpochMillis] = termStart
                it[trialEndedAtEpochMillis] = termEnd
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
    }

    fun listInvoices(
        status: String? = null,
        search: String? = null
    ): List<SuperAdminInvoiceResponse> = transaction {
        val invoices = SubscriptionInvoicesTable
            .selectAll()
            .orderBy(SubscriptionInvoicesTable.createdAtEpochMillis, SortOrder.DESC)
            .mapNotNull { invoice ->
                val accountId = invoice[SubscriptionInvoicesTable.accountId]

                val account = AccountsTable
                    .selectAll()
                    .where { AccountsTable.id eq accountId }
                    .singleOrNull()
                    ?: return@mapNotNull null

                val academicYearId = invoice[SubscriptionInvoicesTable.academicYearId]
                val academicTermId = invoice[SubscriptionInvoicesTable.academicTermId]

                val year = AcademicYearsTable
                    .selectAll()
                    .where { AcademicYearsTable.id eq academicYearId }
                    .singleOrNull()
                    ?: return@mapNotNull null

                val term = AcademicTermsTable
                    .selectAll()
                    .where { AcademicTermsTable.id eq academicTermId }
                    .singleOrNull()
                    ?: return@mapNotNull null

                SuperAdminInvoiceResponse(
                    invoiceId = invoice[SubscriptionInvoicesTable.id].value,
                    accountId = accountId,
                    tenantCode = invoice[SubscriptionInvoicesTable.tenantCode],
                    schoolName = account[AccountsTable.schoolName],
                    email = account[AccountsTable.email],

                    academicYearId = academicYearId,
                    academicYearName = year[AcademicYearsTable.name],
                    academicTermId = academicTermId,
                    termName = term[AcademicTermsTable.termName],

                    studentCount = invoice[SubscriptionInvoicesTable.studentCount],
                    amountPerStudentCedis = invoice[SubscriptionInvoicesTable.amountPerStudentCedis].toPlainString(),
                    totalAmountCedis = invoice[SubscriptionInvoicesTable.totalAmountCedis].toPlainString(),

                    isPaid = invoice[SubscriptionInvoicesTable.isPaid],
                    paymentStatus = invoice[SubscriptionInvoicesTable.paymentStatus],
                    paystackReference = invoice[SubscriptionInvoicesTable.paystackReference],

                    dueDateEpochMillis = invoice[SubscriptionInvoicesTable.dueDateEpochMillis],
                    paidAtEpochMillis = invoice[SubscriptionInvoicesTable.paidAtEpochMillis],
                    createdAtEpochMillis = invoice[SubscriptionInvoicesTable.createdAtEpochMillis]
                )
            }

        val byStatus = if (status.isNullOrBlank() || status == "all") {
            invoices
        } else {
            invoices.filter { it.paymentStatus == status }
        }

        val normalizedSearch = search?.trim()?.lowercase()

        if (normalizedSearch.isNullOrBlank()) {
            byStatus
        } else {
            byStatus.filter {
                listOf(
                    it.schoolName,
                    it.email,
                    it.tenantCode,
                    it.academicYearName,
                    it.termName,
                    it.paymentStatus
                ).joinToString(" ").lowercase().contains(normalizedSearch)
            }
        }
    }

    fun listTransactions(
        status: String? = null,
        search: String? = null
    ): List<SuperAdminTransactionResponse> = transaction {
        val transactions = PaymentTransactionsTable
            .selectAll()
            .orderBy(PaymentTransactionsTable.createdAtEpochMillis, SortOrder.DESC)
            .map {
                SuperAdminTransactionResponse(
                    transactionId = it[PaymentTransactionsTable.id].value,
                    invoiceId = it[PaymentTransactionsTable.invoiceId],
                    accountId = it[PaymentTransactionsTable.accountId],
                    tenantCode = it[PaymentTransactionsTable.tenantCode],
                    provider = it[PaymentTransactionsTable.provider],
                    paystackReference = it[PaymentTransactionsTable.paystackReference],
                    amountCedis = it[PaymentTransactionsTable.amountCedis].toPlainString(),
                    currency = it[PaymentTransactionsTable.currency],
                    status = it[PaymentTransactionsTable.status],
                    authorizationUrl = it[PaymentTransactionsTable.paystackAuthorizationUrl],
                    createdAtEpochMillis = it[PaymentTransactionsTable.createdAtEpochMillis],
                    paidAtEpochMillis = it[PaymentTransactionsTable.paidAtEpochMillis]
                )
            }

        val byStatus = if (status.isNullOrBlank() || status == "all") {
            transactions
        } else {
            transactions.filter { it.status == status }
        }

        val normalizedSearch = search?.trim()?.lowercase()

        if (normalizedSearch.isNullOrBlank()) {
            byStatus
        } else {
            byStatus.filter {
                listOf(
                    it.tenantCode,
                    it.provider,
                    it.paystackReference,
                    it.status
                ).joinToString(" ").lowercase().contains(normalizedSearch)
            }
        }
    }

    fun markOverdueInvoices(dateEpochMillis: Long = System.currentTimeMillis()): Int = transaction {
        val overdueInvoices = SubscriptionInvoicesTable
            .selectAll()
            .where {
                (SubscriptionInvoicesTable.isPaid eq false) and
                        (SubscriptionInvoicesTable.paymentStatus eq "pending") and
                        (SubscriptionInvoicesTable.dueDateEpochMillis less dateEpochMillis)
            }
            .toList()

        overdueInvoices.forEach { invoice ->
            val invoiceId = invoice[SubscriptionInvoicesTable.id].value
            val accountId = invoice[SubscriptionInvoicesTable.accountId]

            SubscriptionInvoicesTable.update({ SubscriptionInvoicesTable.id eq invoiceId }) {
                it[paymentStatus] = "overdue"
            }

            AccountsTable.update({ AccountsTable.id eq accountId }) {
                it[billingStatus] = "suspended"
                it[tenantStatus] = "suspended"
                it[isActive] = false
            }
        }

        overdueInvoices.size
    }
}