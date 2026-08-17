package sms.services



import sms.dto.CreateWalletTopUpRequest
import sms.dto.CreateWalletTopUpResponse
import sms.dto.VerifyWalletTopUpRequest
import sms.dto.VerifyWalletTopUpResponse
import java.math.BigDecimal
import java.security.SecureRandom
import java.time.Instant
import java.util.Base64



import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

import sms.repos.SmsWalletTopUpPaymentRepository
import sms.tables.SmsWalletTopUpPaymentsTable


import java.math.RoundingMode


object SmsWalletTopUpService {

    private val httpClient =
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                    }
                )
            }
        }

    private val paystackClient =
        PaystackSmsWalletClient(
            httpClient = httpClient
        )

    private val tenantWalletClient =
        TenantWalletClient(
            httpClient = httpClient
        )

    fun createTopUp(
        request: CreateWalletTopUpRequest
    ): CreateWalletTopUpResponse {

        val tenantCode =
            request.tenantCode.trim()

        val email =
            request.email.trim()

        val amount =
            request.amount
                .toBigDecimalOrNull()
                ?.setScale(2, RoundingMode.HALF_UP)
                ?: throw IllegalArgumentException(
                    "Invalid amount."
                )

        require(tenantCode.isNotBlank()) {
            "Tenant code is required."
        }

        require(email.isNotBlank()) {
            "Email is required."
        }

        require(amount >= BigDecimal("10.00")) {
            "You cannot load less than GHS 10.00."
        }

        require(amount <= BigDecimal("100.00")) {
            "You cannot load more than GHS 100.00."
        }

        val amountInPesewas =
            amount
                .multiply(BigDecimal("100"))
                .setScale(0, RoundingMode.HALF_UP)
                .toInt()

        val reference =
            generateReference()

        val now =
            Instant.now().toString()

        transaction {

            SmsWalletTopUpPaymentsTable.insert {
                it[SmsWalletTopUpPaymentsTable.tenantCode] = tenantCode
                it[SmsWalletTopUpPaymentsTable.schoolName] = null
                it[SmsWalletTopUpPaymentsTable.email] = email
                it[SmsWalletTopUpPaymentsTable.amount] = amount
                it[SmsWalletTopUpPaymentsTable.amountInPesewas] = amountInPesewas
                it[SmsWalletTopUpPaymentsTable.reference] = reference
                it[SmsWalletTopUpPaymentsTable.authorizationUrl] = null
                it[SmsWalletTopUpPaymentsTable.accessCode] = null
                it[SmsWalletTopUpPaymentsTable.status] = "pending"
                it[SmsWalletTopUpPaymentsTable.verified] = false
                it[SmsWalletTopUpPaymentsTable.walletCredited] = false
                it[SmsWalletTopUpPaymentsTable.paystackStatus] = null
                it[SmsWalletTopUpPaymentsTable.paystackMessage] = null
                it[SmsWalletTopUpPaymentsTable.createdAt] = now
                it[SmsWalletTopUpPaymentsTable.verifiedAt] = null
                it[SmsWalletTopUpPaymentsTable.updatedAt] = now
            }
        }

        return CreateWalletTopUpResponse(
            success = true,
            message = "Wallet top-up transaction created successfully.",
            reference = reference
        )
    }

    suspend fun verifyTopUp(
        request: VerifyWalletTopUpRequest
    ): VerifyWalletTopUpResponse {

        val tenantCode =
            request.tenantCode.trim()

        val reference =
            request.reference.trim()

        require(tenantCode.isNotBlank()) {
            "Tenant code is required."
        }

        require(reference.isNotBlank()) {
            "Payment reference is required."
        }

        val payment =
            SmsWalletTopUpPaymentRepository.findByReference(
                reference = reference
            ) ?: throw IllegalArgumentException(
                "Payment reference not found."
            )

        if (payment.tenantCode != tenantCode) {

            throw IllegalArgumentException(
                "Payment reference does not belong to this tenant."
            )
        }

        if (payment.walletCredited) {

            return VerifyWalletTopUpResponse(
                success = true,
                message = "Wallet already credited.",
                cashBalance = null
            )
        }

        val paystackResponse =
            paystackClient.verifyTransaction(
                reference = reference
            )

        val paystackData =
            paystackResponse.data
                ?: throw IllegalStateException(
                    paystackResponse.message
                )

        if (paystackData.status != "success") {

            SmsWalletTopUpPaymentRepository.markFailed(
                reference = reference,
                paystackStatus = paystackData.status,
                paystackMessage = paystackResponse.message
            )

            return VerifyWalletTopUpResponse(
                success = false,
                message = "Payment was not successful."
            )
        }

        if (paystackData.amount != payment.amountInPesewas) {

            SmsWalletTopUpPaymentRepository.markFailed(
                reference = reference,
                paystackStatus = paystackData.status,
                paystackMessage = "Amount mismatch."
            )

            return VerifyWalletTopUpResponse(
                success = false,
                message = "Payment amount mismatch."
            )
        }

        SmsWalletTopUpPaymentRepository.markVerifiedButNotCredited(
            reference = reference,
            paystackStatus = paystackData.status,
            paystackMessage = paystackResponse.message
        )

        tenantWalletClient.creditCashWallet(
            tenantCode = tenantCode,
            amount = payment.amount,
            reference = reference
        )

        SmsWalletTopUpPaymentRepository.markWalletCredited(
            reference = reference
        )

        return VerifyWalletTopUpResponse(
            success = true,
            message = "Wallet credited successfully.",
            cashBalance = null
        )
    }

    private fun generateReference(): String {

        val randomBytes =
            ByteArray(32)

        SecureRandom()
            .nextBytes(randomBytes)

        val token =
            Base64
                .getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes)

        return "phena_wallet_$token"
    }
}