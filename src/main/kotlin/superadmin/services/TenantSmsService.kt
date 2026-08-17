package superadmin.services

import config.AppConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import sms.dto.LatestSenderIdResponse
import sms.dto.PurchaseSmsCreditsRequest
import sms.dto.PurchaseSmsCreditsResponse
import sms.dto.RequestSenderIdRequest
import sms.dto.SenderIdMessageResponse
import sms.dto.SenderIdResponse
import superadmin.dto.sms.RejectSenderIdRequest
import superadmin.dto.sms.SmsWalletResponse
import superadmin.dto.sms.SmsWalletTransactionResponse

object TenantSmsService {

    private val json =
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            prettyPrint = true
        }

    private val client =
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(json)
            }
        }







    // for school / clients

    suspend fun purchaseSmsCredits(
        request: PurchaseSmsCreditsRequest
    ): PurchaseSmsCreditsResponse {

        val response =
            client.post(
                "${AppConfig.tenantApiBaseUrl}/internal/sms/wallet/purchase-sms"
            ) {

                contentType(
                    ContentType.Application.Json
                )

                header(
                    "X-Internal-Api-Key",
                    AppConfig.tenantInternalApiKey
                )

                setBody(
                    request
                )
            }

        val bodyText =
            response.bodyAsText()

        if (response.status.value !in 200..299) {

            throw IllegalStateException(
                extractMessageOrDefault(
                    bodyText = bodyText,
                    defaultMessage = "Unable to purchase SMS credits."
                )
            )
        }

        return json.decodeFromString<PurchaseSmsCreditsResponse>(
            bodyText
        )
    }

    suspend fun getClientSmsWallet(
        tenantCode: String
    ): SmsWalletResponse {

        val response =
            client.get(
                "${AppConfig.tenantApiBaseUrl}/internal/sms/wallet/$tenantCode"
            ) {

                header(
                    "X-Internal-Api-Key",
                    AppConfig.tenantInternalApiKey
                )
            }

        val bodyText =
            response.bodyAsText()

        if (response.status.value !in 200..299) {

            throw IllegalStateException(
                extractMessageOrDefault(
                    bodyText = bodyText,
                    defaultMessage = "Unable to retrieve SMS wallet."
                )
            )
        }

        return json.decodeFromString<SmsWalletResponse>(
            bodyText
        )
    }

    suspend fun requestSenderId(
        request: RequestSenderIdRequest
    ): SenderIdResponse {

        val response =
            client.post(
                "${AppConfig.tenantApiBaseUrl}/internal/sms/sender-id/request"
            ) {
                contentType(
                    ContentType.Application.Json
                )

                header(
                    "X-Internal-Api-Key",
                    AppConfig.tenantInternalApiKey
                )

                setBody(
                    request
                )
            }

        val bodyText =
            response.bodyAsText()

        if (response.status.value !in 200..299) {

            throw IllegalStateException(
                extractMessageOrDefault(
                    bodyText = bodyText,
                    defaultMessage = "Unable to request sender ID."
                )
            )
        }

        return json.decodeFromString<SenderIdResponse>(
            bodyText
        )
    }

    suspend fun getLatestSenderId(
        tenantCode: String
    ): LatestSenderIdResponse {

        val response =
            client.get(
                "${AppConfig.tenantApiBaseUrl}/internal/sms/sender-id/latest/$tenantCode"
            ) {
                header(
                    "X-Internal-Api-Key",
                    AppConfig.tenantInternalApiKey
                )
            }

        val bodyText =
            response.bodyAsText()

        if (response.status.value !in 200..299) {

            throw IllegalStateException(
                extractMessageOrDefault(
                    bodyText = bodyText,
                    defaultMessage = "Unable to get latest sender ID."
                )
            )
        }

        return json.decodeFromString<LatestSenderIdResponse>(
            bodyText
        )
    }







    // for superadmin only
    suspend fun getAllSenderIds(): List<SenderIdResponse> {

        val response =
            client.get(
                "${AppConfig.tenantApiBaseUrl}/api/internal/sms/sender-id/all"
            ) {
                header(
                    "X-Internal-Api-Key",
                    AppConfig.tenantInternalApiKey
                )
            }

        println("response1 isssss $response")

        val bodyText =
            response.bodyAsText()
        println("response2 isssssssssssssss $bodyText")

        if (response.status.value !in 200..299) {

            println("response3 isssssssssssss $response")

            throw IllegalStateException(
                extractMessageOrDefault(
                    bodyText = bodyText,
                    defaultMessage = "Unable to retrieve sender ID requests."
                )
            )
        }

        return json.decodeFromString<List<SenderIdResponse>>(
            bodyText
        )
    }

    suspend fun approveSenderId(
        id: Int
    ): SenderIdResponse {

        val response =
            client.put(
                "${AppConfig.tenantApiBaseUrl}/api/internal/sms/sender-id/$id/approve"
            ) {
                header(
                    "X-Internal-Api-Key",
                    AppConfig.tenantInternalApiKey
                )
            }

        val bodyText =
            response.bodyAsText()

        if (response.status.value !in 200..299) {

            throw IllegalStateException(
                extractMessageOrDefault(
                    bodyText = bodyText,
                    defaultMessage = "Unable to approve sender ID."
                )
            )
        }

        return json.decodeFromString<SenderIdResponse>(
            bodyText
        )
    }

    suspend fun rejectSenderId(
        id: Int,
        request: RejectSenderIdRequest
    ): SenderIdResponse {

        val response =
            client.put(
                "${AppConfig.tenantApiBaseUrl}/api/internal/sms/sender-id/$id/reject"
            ) {
                contentType(
                    ContentType.Application.Json
                )

                header(
                    "X-Internal-Api-Key",
                    AppConfig.tenantInternalApiKey
                )

                setBody(
                    request
                )
            }

        val bodyText =
            response.bodyAsText()

        if (response.status.value !in 200..299) {

            throw IllegalStateException(
                extractMessageOrDefault(
                    bodyText = bodyText,
                    defaultMessage = "Unable to reject sender ID."
                )
            )
        }

        return json.decodeFromString<SenderIdResponse>(
            bodyText
        )
    }

    /*
     * Used by school/client side.
     * Requires tenantCode so a school can only delete its own sender ID.
     */
    suspend fun deleteSenderId(
        id: Int,
        tenantCode: String
    ): Boolean {

        val response =
            client.delete(
                "${AppConfig.tenantApiBaseUrl}/internal/sms/sender-id/$id"
            ) {
                header(
                    "X-Internal-Api-Key",
                    AppConfig.tenantInternalApiKey
                )

                header(
                    "X-Tenant-Code",
                    tenantCode
                )
            }

        val bodyText =
            response.bodyAsText()

        if (response.status.value !in 200..299) {

            throw IllegalStateException(
                extractMessageOrDefault(
                    bodyText = bodyText,
                    defaultMessage = "Unable to delete sender ID."
                )
            )
        }

        return true
    }

    /*
     * Used by super admin side.
     * Super admin can delete any sender ID by id.
     */
    suspend fun deleteSenderId(
        id: Int
    ): Boolean {

        val response =
            client.delete(
                "${AppConfig.tenantApiBaseUrl}/api/internal/sms/sender-id/$id"
            ) {
                header(
                    "X-Internal-Api-Key",
                    AppConfig.tenantInternalApiKey
                )
            }

        val bodyText =
            response.bodyAsText()

        if (response.status.value !in 200..299) {

            throw IllegalStateException(
                extractMessageOrDefault(
                    bodyText = bodyText,
                    defaultMessage = "Unable to delete sender ID."
                )
            )
        }

        return true
    }

    private fun extractMessageOrDefault(
        bodyText: String,
        defaultMessage: String
    ): String {

        return try {

            json.decodeFromString<SenderIdMessageResponse>(
                bodyText
            ).message

        } catch (_: Exception) {

            defaultMessage
        }
    }


    suspend fun getSmsWalletByTenantCode(
        tenantCode: String
    ): SmsWalletResponse {

        val response =
            client.get(
                "${AppConfig.tenantApiBaseUrl}/internal/sms/wallets/$tenantCode"
            ) {

                header(
                    "X-Internal-Api-Key",
                    AppConfig.tenantInternalApiKey
                )
            }

        val bodyText =
            response.bodyAsText()

        if (response.status.value !in 200..299) {

            throw IllegalStateException(
                extractMessageOrDefault(
                    bodyText = bodyText,
                    defaultMessage = "Unable to retrieve SMS wallet."
                )
            )
        }

        return json.decodeFromString<SmsWalletResponse>(
            bodyText
        )
    }


    suspend fun getAllSmsWallets(): List<SmsWalletResponse> {

        val response =
            client.get(
                "${AppConfig.tenantApiBaseUrl}/api/internal/sms/wallets"
            ) {

                header(
                    "X-Internal-Api-Key",
                    AppConfig.tenantInternalApiKey
                )
            }

        val bodyText =
            response.bodyAsText()

        if (response.status.value !in 200..299) {

            throw IllegalStateException(
                extractMessageOrDefault(
                    bodyText = bodyText,
                    defaultMessage = "Unable to retrieve SMS wallets."
                )
            )
        }

        return json.decodeFromString<List<SmsWalletResponse>>(
            bodyText
        )
    }

    suspend fun getSmsWalletTransactionsByTenantCode(
        tenantCode: String
    ): List<SmsWalletTransactionResponse> {

        val response =
            client.get(
                "${AppConfig.tenantApiBaseUrl}/internal/sms/wallet-transactions/$tenantCode"
            ) {

                header(
                    "X-Internal-Api-Key",
                    AppConfig.tenantInternalApiKey
                )
            }

        val bodyText =
            response.bodyAsText()

        if (response.status.value !in 200..299) {

            throw IllegalStateException(
                extractMessageOrDefault(
                    bodyText = bodyText,
                    defaultMessage = "Unable to retrieve SMS wallet transactions."
                )
            )
        }

        return json.decodeFromString<List<SmsWalletTransactionResponse>>(
            bodyText
        )
    }

    suspend fun getAllSmsWalletTransactions(): List<SmsWalletTransactionResponse> {

        val response =
            client.get(
                "${AppConfig.tenantApiBaseUrl}/api/internal/sms/wallet-transactions"
            ) {

                header(
                    "X-Internal-Api-Key",
                    AppConfig.tenantInternalApiKey
                )
            }

        val bodyText =
            response.bodyAsText()

        if (response.status.value !in 200..299) {

            throw IllegalStateException(
                extractMessageOrDefault(
                    bodyText = bodyText,
                    defaultMessage = "Unable to retrieve SMS wallet transactions."
                )
            )
        }

        return json.decodeFromString<List<SmsWalletTransactionResponse>>(
            bodyText
        )
    }
}