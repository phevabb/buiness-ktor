package superadmin.client

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter

class KtorTenantInternalBillingClient(
    private val httpClient: HttpClient,
    private val tenantApiBaseUrl: String,
    private val internalApiKey: String
) : TenantInternalBillingClient {

    override suspend fun getTenantBillingSummary(
        search: String?,
        status: String?
    ): List<InternalTenantBillingSummaryItem> {
        val cleanBaseUrl = tenantApiBaseUrl.trimEnd('/')

        val url = "$cleanBaseUrl/api/internal/superadmin/tenants/billing-summary"

        return httpClient.get(url) {
            header("X-Internal-Api-Key", internalApiKey.trim())

            if (!search.isNullOrBlank()) {
                parameter("search", search)
            }

            if (!status.isNullOrBlank()) {
                parameter("status", status)
            }
        }.body()
    }
}