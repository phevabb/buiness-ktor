package com.example.account.service

import com.example.account.dto.AccountResponse
import com.example.config.AppConfig
import com.example.superadmin.dto.TenantAcademicCalendarSeed
import com.example.superadmin.repos.BillingRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json



object TenantProvisioningService {

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    prettyPrint = true
                }
            )
        }
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    suspend fun createTenantForAccount(
        account: AccountResponse
    ): CreateTenantResponse {
        val serverNow = System.currentTimeMillis()

        val academicCalendar = BillingRepository.findAcademicCalendarForDate(serverNow)
            ?: error("No active academic year/term found for current server time.")
            println("academic is $academicCalendar    ")
        val request = CreateTenantRequest(
            schoolName = account.schoolName,
            tenantCode = account.tenantCode,
            schoolType = "School",
            location = account.location,
            contactEmail = account.email,
            accountOwnerName = account.fullName,
            primaryDomain = "",
            academicYear = account.academicYear,
            features = emptyList(),
            academicCalendar = academicCalendar
        )

        val response = client.post("${AppConfig.tenantApiBaseUrl}/internal/tenants/create") {
            contentType(ContentType.Application.Json)

            if (AppConfig.tenantInternalApiKey.isNotBlank()) {
                header("X-Internal-Api-Key", AppConfig.tenantInternalApiKey)
            }

            setBody(request)
        }

        val bodyText = response.bodyAsText()

        if (response.status.value !in 200..299) {
            throw IllegalStateException(
                "Tenant creation failed. Status: ${response.status.value}. Body: $bodyText"
            )
        }

        return json.decodeFromString<CreateTenantResponse>(bodyText)
    }
}

@Serializable
data class CreateTenantRequest(
    val schoolName: String,
    val tenantCode: String,
    val schoolType: String,
    val location: String,
    val contactEmail: String,
    val accountOwnerName: String,
    val primaryDomain: String,
    val academicYear: String,
    val features: List<String>,
    val academicCalendar: TenantAcademicCalendarSeed
)

@Serializable
data class CreateTenantResponse(
    val tenantId: Int,
    val schoolName: String,
    val tenantCode: String,
    val tenantSchema: String,
    val tenantSlug: String,
    val defaultDomain: String,
    val defaultLocalDomain: String,
    val fallbackLocalUrl: String,
    val status: String,
    val message: String,
    val principalLoginUserId: String,
    val principalPin: String,
    val adminLoginUserId: String,
    val adminPin: String
)
