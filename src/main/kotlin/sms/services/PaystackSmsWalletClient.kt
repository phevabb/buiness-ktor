package sms.services



import config.AppConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import sms.dto.paystack.PaystackInitializeRequest
import sms.dto.paystack.PaystackInitializeResponse
import sms.dto.paystack.PaystackVerifyResponse

class PaystackSmsWalletClient(
    private val httpClient: HttpClient
) {

    suspend fun initializeTransaction(
        request: PaystackInitializeRequest
    ): PaystackInitializeResponse {

        return httpClient.post(
            "https://api.paystack.co/transaction/initialize"
        ) {
            header(
                "Authorization",
                "Bearer ${AppConfig.paystackSecretKey}"
            )

            contentType(
                ContentType.Application.Json
            )

            setBody(
                request
            )
        }.body()
    }

    suspend fun verifyTransaction(
        reference: String
    ): PaystackVerifyResponse {

        return httpClient.get(
            "https://api.paystack.co/transaction/verify/$reference"
        ) {
            header(
                "Authorization",
                "Bearer ${AppConfig.paystackSecretKey}"
            )
        }.body()
    }
}