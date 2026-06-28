package com.example.superadmin.client



import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

class TenantSuperAdminClient(
    private val httpClient: HttpClient,
    private val tenantBaseUrl: String,
    private val internalApiKey: String
) {

    suspend fun getTenants(
        search: String?,
        status: String?
    ): List<SuperAdminTenantResponse> {
        return httpClient.get("$tenantBaseUrl/api/internal/superadmin/tenants") {
            header("X-Internal-Api-Key", internalApiKey)

            if (!search.isNullOrBlank()) {
                parameter("search", search)
            }

            if (!status.isNullOrBlank()) {
                parameter("status", status)
            }
        }.body()
    }

    suspend fun updateTenantStatus(
        tenantCode: String,
        status: String
    ): TenantStatusUpdateResponse {

        val url = "$tenantBaseUrl/api/internal/superadmin/tenants/$tenantCode/status"

        println("========== [BUSINESS CLIENT] UPDATE TENANT STATUS START ==========")
        println("[BUSINESS CLIENT] tenantBaseUrl=$tenantBaseUrl")
        println("[BUSINESS CLIENT] Final URL=$url")
        println("[BUSINESS CLIENT] tenantCode=$tenantCode")
        println("[BUSINESS CLIENT] status=$status")
        println("[BUSINESS CLIENT] internalApiKey loaded=${internalApiKey.isNotBlank()}")
        println("[BUSINESS CLIENT] internalApiKey length=${internalApiKey.length}")

        return try {
            val response = httpClient.patch(url) {
                header("X-Internal-Api-Key", internalApiKey)
                contentType(ContentType.Application.Json)
                setBody(UpdateTenantStatusRequest(status = status))
            }

            println("[BUSINESS CLIENT] Tenant service HTTP status: ${response.status}")

            val body = response.body<TenantStatusUpdateResponse>()

            println("[BUSINESS CLIENT] Response body parsed successfully: $body")
            println("========== [BUSINESS CLIENT] UPDATE TENANT STATUS SUCCESS ==========")

            body

        } catch (e: Exception) {
            println("[BUSINESS CLIENT] ERROR calling tenant service")
            println("[BUSINESS CLIENT] Exception type: ${e::class.qualifiedName}")
            println("[BUSINESS CLIENT] Exception message: ${e.message}")
            e.printStackTrace()

            println("========== [BUSINESS CLIENT] UPDATE TENANT STATUS FAILED ==========")

            throw e
        }
    }
}

@Serializable
data class SuperAdminTenantResponse(
    val id: Int,
    val schoolName: String,
    val tenantCode: String,
    val tenantSchema: String,
    val tenantSlug: String,
    val defaultDomain: String,

    val schoolType: String?,
    val location: String?,
    val contactEmail: String?,
    val accountOwnerName: String?,
    val primaryDomain: String?,

    val academicYear: String?,
    val status: String,
    val createdAt: String,

    val schemaName: String? = null,
    val schemaStatus: String? = null
)

@Serializable
data class UpdateTenantStatusRequest(
    val status: String
)

@Serializable
data class TenantStatusUpdateResponse(
    val success: Boolean,
    val message: String
)