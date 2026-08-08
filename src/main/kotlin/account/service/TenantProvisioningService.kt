package com.example.account.service

import account.dto.UpdateSchoolBrandingRequest
import account.dto.UpdateTenantPinsRequest
import account.dto.UpdateTenantRequest
import com.example.account.dto.AccountResponse

import com.example.superadmin.dto.TenantAcademicCalendarSeed
import com.example.superadmin.repos.BillingRepository
import config.AppConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.String

// this is more of a repo
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

    suspend fun updateSchoolBranding(
        tenantCode: String,
        schoolName: String,
        schoolLogoUrl: String?,
        schoolMotto: String?,
        location: String?
    ): Boolean {

        val request = UpdateSchoolBrandingRequest(
            tenantCode = tenantCode,
            schoolName = schoolName,
            schoolLogoUrl = schoolLogoUrl,
            schoolMotto = schoolMotto,
            location = location
        )

        val response = client.put(
            "${AppConfig.tenantApiBaseUrl}/internal/tenants/update-school-branding"
        ) {

            contentType(ContentType.Application.Json)

            header(
                "X-Internal-Api-Key",
                AppConfig.tenantInternalApiKey
            )

            setBody(request)
        }

        return response.status.value in 200..299
    }

    suspend fun updateTenantPins(
        tenantCode: String,
        adminPin: String?,
        principalPin: String?
    ): Boolean {

        val request = UpdateTenantPinsRequest(
            tenantCode = tenantCode,
            adminPin = adminPin,
            principalPin = principalPin
        )

        val response = client.put(
            "${AppConfig.tenantApiBaseUrl}/internal/tenants/update-pins"
        ) {

            contentType(ContentType.Application.Json)

            header(
                "X-Internal-Api-Key",
                AppConfig.tenantInternalApiKey
            )

            setBody(request)
        }

        return response.status.value in 200..299
    }





    suspend fun updateTenantForAccount(
        account: AccountResponse
    ) {

        val request = UpdateTenantRequest(
            tenantCode = account.tenantCode,

            schoolName = account.schoolName,
            schoolLogoUrl = account.schoolLogoUrl,
            schoolMotto = account.schoolMotto,

            schoolLocation = account.location,

            fullName = account.fullName,

            phoneNumber = account.phoneNumber,

            adminPin = account.adminPin,

            principalPin = account.principalPin
        )

        val response = client.put(
            "${AppConfig.tenantApiBaseUrl}/internal/tenants/update"
        ) {

            contentType(ContentType.Application.Json)

            header(
                "X-Internal-Api-Key",
                AppConfig.tenantInternalApiKey
            )

            setBody(request)
        }

        if (!response.status.isSuccess()) {
            error("Tenant update failed")
        }
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
