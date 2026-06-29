package com.example.superadmin.routes

import com.example.superadmin.client.TenantSuperAdminClient
import io.ktor.http.HttpStatusCode

import io.ktor.server.auth.authenticate     // ✅ ADD THIS
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

fun Route.superAdminTenantRoutes(
    tenantSuperAdminClient: TenantSuperAdminClient
) {

    authenticate("super-admin-jwt") {  // ✅ 🔥 PROTECTION STARTS HERE

        /**
         * GET /api/internal/super/tenant
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
         * PATCH /api/internal/super/tenant/{tenantCode}/status
         */
        patch("/{tenantCode}/status") {

            println("========== [BUSINESS] PATCH TENANT STATUS START ==========")

            val tenantCode = call.parameters["tenantCode"]

            if (tenantCode.isNullOrBlank()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("message" to "Tenant code is required")
                )
                return@patch
            }

            val request = try {
                call.receive<BusinessUpdateTenantStatusRequest>()
            } catch (e: Exception) {
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

            if (request.status !in allowedStatuses) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("message" to "Invalid tenant status")
                )
                return@patch
            }

            try {
                val result = tenantSuperAdminClient.updateTenantStatus(
                    tenantCode = tenantCode,
                    status = request.status
                )

                call.respond(HttpStatusCode.OK, result)

            } catch (e: Exception) {
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
}

@Serializable
data class BusinessUpdateTenantStatusRequest(
    val status: String
)
