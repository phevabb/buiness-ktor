package com.example.superadmin.client



import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.math.RoundingMode

class PaystackClient(
    private val httpClient: HttpClient,
    private val secretKey: String
) {
    suspend fun initializeTransaction(
        email: String,
        amountCedis: BigDecimal,
        reference: String,
        callbackUrl: String
    ): PaystackInitializeResponse {
        val amountForPaystack = amountCedis
            .multiply(BigDecimal("100"))
            .setScale(0, RoundingMode.HALF_UP)
            .toLong()

        return httpClient.post("https://api.paystack.co/transaction/initialize") {
            bearerAuth(secretKey)
            contentType(ContentType.Application.Json)
            setBody(
                PaystackInitializeRequest(
                    email = email,
                    amount = amountForPaystack,
                    reference = reference,
                    currency = "GHS",
                    callback_url = callbackUrl
                )
            )
        }.body()
    }

    suspend fun verifyTransaction(reference: String): PaystackVerifyResponse {
        return httpClient.get("https://api.paystack.co/transaction/verify/$reference") {
            bearerAuth(secretKey)
        }.body()
    }
}

@Serializable
data class PaystackInitializeRequest(
    val email: String,
    val amount: Long,
    val reference: String,
    val currency: String = "GHS",
    val callback_url: String
)

@Serializable
data class PaystackInitializeResponse(
    val status: Boolean,
    val message: String,
    val data: PaystackInitializeData? = null
)

@Serializable
data class PaystackInitializeData(
    val authorization_url: String,
    val access_code: String,
    val reference: String
)

@Serializable
data class PaystackVerifyResponse(
    val status: Boolean,
    val message: String,
    val data: PaystackVerifyData? = null
)

@Serializable
data class PaystackVerifyData(
    val status: String,
    val reference: String,
    val amount: Long? = null,
    val currency: String? = null,
    val paid_at: String? = null
)