package com.example.account.routes



import com.example.account.repo.AccountsRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.header
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kotlinx.serialization.Serializable

@Serializable
data class WorkspaceDashboardResponse(
    val schoolName: String,
    val schoolType: String,
    val schoolId: String,
    val tenantCode: String,
    val location: String,
    val contactEmail: String,

    val planName: String,
    val billingCycle: String,
    val billingStatus: String,
    val pricePerStudent: Int,
    val activeStudents: Int,
    val staffCount: Int,
    val classCount: Int,
    val nextInvoiceDate: String,

    val domain: String,
    val customDomain: String,
    val defaultLocalDomain: String?,
    val fallbackLocalUrl: String?,

    val sslStatus: String,
    val environment: String,

    val smsCreditsLeft: Int,
    val smsCreditsUsed: Int,
    val smsCreditsTotal: Int,

    val academicYear: String,
    val principalPin: String?,
    val principalLoginUserId: String?,

    val adminLoginUserId:String?,
    val adminPin: String?,
    )

fun Route.dashboardRoutes() {
    authenticate("auth-jwt") {
        get("/workspace") {
            val tenantCode = call.request.header("X-Tenant-Code")?.trim()

            if (tenantCode.isNullOrBlank()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Tenant code is required")
                )
                return@get
            }

            val account = AccountsRepository.findByTenantCode(tenantCode)
            println("account is print $account")

            if (account == null) {
                call.respond(
                    HttpStatusCode.NotFound,
                    mapOf("error" to "Workspace not found")
                )
                return@get
            }

            val pricePerStudentCedis =
                (account.subscriptionAmountPerTermPesewas / 100).toInt()

            val domain = account.defaultDomain ?: ""
            val defaultLocalDomain = account.defaultLocalDomain ?: ""
            val fallbackLocalUrl = account.fallbackLocalUrl ?: ""
            val adminLoginUserId = account.adminLoginUserId ?: ""
            val adminPin = account.adminPin?: ""

            call.respond(
                HttpStatusCode.OK,
                WorkspaceDashboardResponse(
                    schoolName = account.schoolName,
                    defaultLocalDomain = account.defaultLocalDomain,
                    fallbackLocalUrl = account.fallbackLocalUrl,

                    schoolType = "School",
                    schoolId = account.tenantCode,
                    tenantCode = account.tenantCode,
                    location = account.location,
                    contactEmail = account.email,

                    planName = "Standard Plan",
                    billingCycle = "Billed Per Term",
                    billingStatus = account.tenantStatus ?: "Active",
                    pricePerStudent = pricePerStudentCedis,
                    activeStudents = account.estimatedStudents,
                    staffCount = 0,
                    classCount = 0,
                    nextInvoiceDate = "",

                    domain = domain,
                    customDomain = domain,
                    sslStatus = "Active",
                    environment = "Production",

                    smsCreditsLeft = 0,
                    smsCreditsUsed = 0,
                    smsCreditsTotal = 0,

                    academicYear = account.academicYear,
                    principalLoginUserId = account.principalLoginUserId,
                    principalPin = account.principalPin,

                    adminLoginUserId = adminLoginUserId,
                    adminPin = adminPin


                )
            )
        }
    }
}