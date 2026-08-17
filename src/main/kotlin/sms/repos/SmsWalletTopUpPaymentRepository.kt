package sms.repos


import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import sms.tables.SmsWalletTopUpPaymentsTable
import java.time.Instant

object SmsWalletTopUpPaymentRepository {

    fun findByReference(
        reference: String
    ): WalletTopUpPaymentRecord? {

        return transaction {

            SmsWalletTopUpPaymentsTable
                .selectAll()
                .where {
                    SmsWalletTopUpPaymentsTable.reference eq reference
                }
                .singleOrNull()
                ?.let { row ->

                    WalletTopUpPaymentRecord(
                        id = row[SmsWalletTopUpPaymentsTable.id].value,
                        tenantCode = row[SmsWalletTopUpPaymentsTable.tenantCode],
                        email = row[SmsWalletTopUpPaymentsTable.email],
                        amount = row[SmsWalletTopUpPaymentsTable.amount],
                        amountInPesewas = row[SmsWalletTopUpPaymentsTable.amountInPesewas],
                        reference = row[SmsWalletTopUpPaymentsTable.reference],
                        verified = row[SmsWalletTopUpPaymentsTable.verified],
                        walletCredited = row[SmsWalletTopUpPaymentsTable.walletCredited],
                        status = row[SmsWalletTopUpPaymentsTable.status]
                    )
                }
        }
    }

    fun markVerifiedButNotCredited(
        reference: String,
        paystackStatus: String,
        paystackMessage: String?
    ) {

        val now =
            Instant.now().toString()

        transaction {

            SmsWalletTopUpPaymentsTable.update(
                {
                    SmsWalletTopUpPaymentsTable.reference eq reference
                }
            ) {
                it[SmsWalletTopUpPaymentsTable.verified] = true
                it[SmsWalletTopUpPaymentsTable.status] = "success"
                it[SmsWalletTopUpPaymentsTable.paystackStatus] = paystackStatus
                it[SmsWalletTopUpPaymentsTable.paystackMessage] = paystackMessage
                it[verifiedAt] = now
                it[updatedAt] = now
            }
        }
    }

    fun markWalletCredited(
        reference: String
    ) {

        val now =
            Instant.now().toString()

        transaction {

            SmsWalletTopUpPaymentsTable.update(
                {
                    SmsWalletTopUpPaymentsTable.reference eq reference
                }
            ) {
                it[walletCredited] = true
                it[updatedAt] = now
            }
        }
    }

    fun markFailed(
        reference: String,
        paystackStatus: String?,
        paystackMessage: String?
    ) {

        val now =
            Instant.now().toString()

        transaction {

            SmsWalletTopUpPaymentsTable.update(
                {
                    SmsWalletTopUpPaymentsTable.reference eq reference
                }
            ) {
                it[status] = "failed"
                it[verified] = false
                it[SmsWalletTopUpPaymentsTable.paystackStatus] = paystackStatus
                it[SmsWalletTopUpPaymentsTable.paystackMessage] = paystackMessage
                it[updatedAt] = now
            }
        }
    }
}
