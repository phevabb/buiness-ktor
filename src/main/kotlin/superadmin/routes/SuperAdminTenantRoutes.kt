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
            val tenantCode = call.parameters["tenantCode"]

            if (tenantCode.isNullOrBlank()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("message" to "Tenant code is required")
                )
                return@patch
            }

            val request = call.receive<BusinessUpdateTenantStatusRequest>()

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

@Serializable
data class BusinessUpdateTenantStatusRequest(
    val status: String
)