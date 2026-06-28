package com.example.superadmin.routes



import com.example.superadmin.client.TenantSuperAdminClient
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable

fun Route.superAdminTenantRoutes(
    tenantSuperAdminClient: TenantSuperAdminClient
) {


        /**
         * Vue calls:
         * GET /api/superadmin/tenants
         *
         * ktor-business internally calls:
         * GET ktor-tenant/api/internal/superadmin/tenants
         */
        get {
            val search = call.request.queryParameters["search"]
            val status = call.request.queryParameters["status"]

            try {
                val tenants = tenantSuperAdminClient.getTenants(
                    search = search,
                    status = status
                )

                call.respond(HttpStatusCode.OK, tenants)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadGateway,
                    mapOf(
                        "message" to "Unable to fetch tenants from tenant service",
                        "error" to (e.message ?: "Unknown error")
                    )
                )
            }
        }

        /**
         * Vue calls:
         * PATCH /api/superadmin/tenants/{tenantCode}/status
         *
         * ktor-business internally calls:
         * PATCH ktor-tenant/api/internal/superadmin/tenants/{tenantCode}/status
         */
    patch("/{tenantCode}/status") {
        println("========== [BUSINESS] PATCH TENANT STATUS START ==========")

        val tenantCode = call.parameters["tenantCode"]

        println("[BUSINESS] Received tenantCode from path: $tenantCode")

        if (tenantCode.isNullOrBlank()) {
            println("[BUSINESS] ERROR: tenantCode is null or blank")

            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("message" to "Tenant code is required")
            )
            return@patch
        }

        val request = try {
            val body = call.receive<BusinessUpdateTenantStatusRequest>()
            println("[BUSINESS] Request body received successfully: status=${body.status}")
            body
        } catch (e: Exception) {
            println("[BUSINESS] ERROR: Failed to receive request body")
            println("[BUSINESS] Body parse error: ${e.message}")

            call.respond(
                HttpStatusCode.BadRequest,
                mapOf(
                    "message" to "Invalid request body",
                    "error" to (e.message ?: "Unknown error")
                )
            )
            return@patch
        }

        val allowedStatuses = setOf(
            "provisioning",
            "active",
            "inactive",
            "suspended",
            "failed"
        )

        println("[BUSINESS] Allowed statuses: $allowedStatuses")
        println("[BUSINESS] Requested status: ${request.status}")

        if (request.status !in allowedStatuses) {
            println("[BUSINESS] ERROR: Invalid tenant status: ${request.status}")

            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("message" to "Invalid tenant status")
            )
            return@patch
        }

        try {
            println("[BUSINESS] Calling tenant service to update status...")
            println("[BUSINESS] tenantCode=$tenantCode")
            println("[BUSINESS] status=${request.status}")

            val result = tenantSuperAdminClient.updateTenantStatus(
                tenantCode = tenantCode,
                status = request.status
            )

            println("[BUSINESS] Tenant service response received successfully")
            println("[BUSINESS] Result: $result")
            println("========== [BUSINESS] PATCH TENANT STATUS SUCCESS ==========")

            call.respond(HttpStatusCode.OK, result)

        } catch (e: Exception) {
            println("[BUSINESS] ERROR: Failed to update tenant status from tenant service")
            println("[BUSINESS] Exception type: ${e::class.qualifiedName}")
            println("[BUSINESS] Exception message: ${e.message}")
            e.printStackTrace()

            println("========== [BUSINESS] PATCH TENANT STATUS FAILED ==========")

            call.respond(
                HttpStatusCode.BadGateway,
                mapOf(
                    "message" to "Unable to update tenant status from tenant service",
                    "error" to (e.message ?: "Unknown error")
                )
            )
        }
    }

}

@Serializable
data class BusinessUpdateTenantStatusRequest(
    val status: String
)