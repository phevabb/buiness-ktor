package sms.services



import config.AppConfig
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.math.BigDecimal

class TenantWalletClient(
    private val httpClient: HttpClient
) {

    private val json =
        Json {
            ignoreUnknownKeys = true
            isLenient = true
        }

    suspend fun creditCashWallet(
        tenantCode: String,
        amount: BigDecimal,
        reference: String
    ): String {

        val request =
            CreditCashWalletRequest(
                tenantCode = tenantCode,
                amount = amount.toPlainString(),
                reference = reference
            )

        val response =
            httpClient.post(
                "${AppConfig.tenantApiBaseUrl}/internal/sms/wallet/credit-cash"
            ) {
                contentType(ContentType.Application.Json)

                header(
                    "X-Internal-Api-Key",
                    AppConfig.tenantInternalApiKey
                )

                setBody(request)
            }

        val bodyText =
            response.bodyAsText()

        if (response.status.value !in 200..299) {

            throw IllegalStateException(
                "Tenant wallet credit failed. Status: ${response.status.value}. Body: $bodyText"
            )
        }

        return bodyText
    }
}

@Serializable
data class CreditCashWalletRequest(
    val tenantCode: String,
    val amount: String,
    val reference: String
)