package com.example.superadmin.services



import com.example.account.table.AccountsTable
import com.example.superadmin.client.PaystackClient
import com.example.superadmin.dto.InitializePaymentResponse
import com.example.superadmin.dto.VerifyPaymentResponse
import com.example.superadmin.repos.BillingRepository
import com.example.superadmin.table.PaymentTransactionsTable
import com.example.superadmin.table.SubscriptionInvoicesTable
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.util.UUID

class PaymentService(
    private val paystackClient: PaystackClient,
    private val callbackBaseUrl: String
) {
    suspend fun initializePayment(invoiceId: Int): InitializePaymentResponse {
        val data = transaction {
            val invoice = SubscriptionInvoicesTable
                .selectAll()
                .where { SubscriptionInvoicesTable.id eq invoiceId }
                .singleOrNull()
                ?: error("Invoice not found")

            if (invoice[SubscriptionInvoicesTable.isPaid]) {
                error("Invoice already paid")
            }

            if (invoice[SubscriptionInvoicesTable.paymentStatus] != "pending") {
                error("Invoice is not payable")
            }

            val accountId = invoice[SubscriptionInvoicesTable.accountId]

            val account = AccountsTable
                .selectAll()
                .where { AccountsTable.id eq accountId }
                .singleOrNull()
                ?: error("Account not found")

            val reference = "PHENA-${invoiceId}-${UUID.randomUUID().toString().replace("-", "").take(12)}"

            Triple(
                account[AccountsTable.email],
                invoice[SubscriptionInvoicesTable.totalAmountCedis],
                reference
            )
        }

        val email = data.first
        val amountCedis = data.second
        val reference = data.third

        val callbackUrl = "$callbackBaseUrl/payment/verify?reference=$reference"

        val paystackResponse = paystackClient.initializeTransaction(
            email = email,
            amountCedis = amountCedis,
            reference = reference,
            callbackUrl = callbackUrl
        )

        if (!paystackResponse.status || paystackResponse.data == null) {
            error(paystackResponse.message)
        }

        transaction {
            val invoice = SubscriptionInvoicesTable
                .selectAll()
                .where { SubscriptionInvoicesTable.id eq invoiceId }
                .single()

            SubscriptionInvoicesTable.update({ SubscriptionInvoicesTable.id eq invoiceId }) {
                it[paystackReference] = reference
            }

            PaymentTransactionsTable.insert {
                it[PaymentTransactionsTable.invoiceId] = invoiceId
                it[accountId] = invoice[SubscriptionInvoicesTable.accountId]
                it[tenantCode] = invoice[SubscriptionInvoicesTable.tenantCode]
                it[provider] = "paystack"
                it[paystackReference] = reference
                it[paystackAccessCode] = paystackResponse.data.access_code
                it[paystackAuthorizationUrl] = paystackResponse.data.authorization_url
                it[PaymentTransactionsTable.amountCedis] = invoice[SubscriptionInvoicesTable.totalAmountCedis]
                it[currency] = "GHS"
                it[status] = "pending"
                it[rawResponse] = paystackResponse.message
                it[createdAtEpochMillis] = System.currentTimeMillis()
                it[paidAtEpochMillis] = null
            }
        }

        return InitializePaymentResponse(
            invoiceId = invoiceId,
            reference = reference,
            authorizationUrl = paystackResponse.data.authorization_url,
            accessCode = paystackResponse.data.access_code
        )
    }

    suspend fun verifyPayment(reference: String): VerifyPaymentResponse {
        val verifyResponse = paystackClient.verifyTransaction(reference)

        if (!verifyResponse.status || verifyResponse.data == null) {
            return VerifyPaymentResponse(
                success = false,
                message = verifyResponse.message,
                invoiceId = null,
                reference = reference
            )
        }

        val paystackStatus = verifyResponse.data.status

        if (paystackStatus != "success") {
            transaction {
                PaymentTransactionsTable.update({
                    PaymentTransactionsTable.paystackReference eq reference
                }) {
                    it[status] = paystackStatus
                    it[rawResponse] = verifyResponse.message
                }
            }

            return VerifyPaymentResponse(
                success = false,
                message = "Payment not successful. Status: $paystackStatus",
                invoiceId = null,
                reference = reference
            )
        }

        val invoiceId = transaction {
            val invoice = SubscriptionInvoicesTable
                .selectAll()
                .where { SubscriptionInvoicesTable.paystackReference eq reference }
                .singleOrNull()
                ?: return@transaction null

            invoice[SubscriptionInvoicesTable.id].value
        }

        if (invoiceId == null) {
            return VerifyPaymentResponse(
                success = false,
                message = "Invoice not found for reference",
                invoiceId = null,
                reference = reference
            )
        }

        BillingRepository.markInvoicePaidByPaystack(reference)

        transaction {
            PaymentTransactionsTable.update({
                PaymentTransactionsTable.paystackReference eq reference
            }) {
                it[status] = "successful"
                it[rawResponse] = verifyResponse.message
                it[paidAtEpochMillis] = System.currentTimeMillis()
            }
        }

        return VerifyPaymentResponse(
            success = true,
            message = "Payment verified successfully",
            invoiceId = invoiceId,
            reference = reference
        )
    }
}