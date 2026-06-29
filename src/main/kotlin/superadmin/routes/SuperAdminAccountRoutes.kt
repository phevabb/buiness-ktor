package com.example.superadmin.routes

import com.example.account.repo.AccountsRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import kotlinx.serialization.Serializable

import io.ktor.server.auth.authenticate   // ✅ add

fun Route.superAdminAccountRoutes() {

    authenticate("super-admin-jwt") {   // ✅ 🔥 THIS IS THE PROTECTION LAYER

        /**
         * GET /api/super
         */
        get {
            val search = call.request.queryParameters["search"]
            val status = call.request.queryParameters["status"]

            val accounts = AccountsRepository.findAllForSuperAdmin(
                search = search,
                status = status
            )

            call.respond(HttpStatusCode.OK, accounts)
        }

        /**
         * GET /api/super/{id}
         */
        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()

            if (id == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("message" to "Invalid account ID")
                )
                return@get
            }

            val account = AccountsRepository.findById(id)

            if (account == null) {
                call.respond(
                    HttpStatusCode.NotFound,
                    mapOf("message" to "Account not found")
                )
                return@get
            }

            call.respond(HttpStatusCode.OK, account)
        }

        /**
         * PATCH /accounts/{id}/active
         */
        patch("/accounts/{id}/active") {
            val id = call.parameters["id"]?.toIntOrNull()

            if (id == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("message" to "Invalid account ID")
                )
                return@patch
            }

            val request = call.receive<UpdateAccountActiveRequest>()

            val updated = AccountsRepository.updateAccountActive(
                accountId = id,
                isActive = request.isActive
            )

            if (!updated) {
                call.respond(
                    HttpStatusCode.NotFound,
                    mapOf("message" to "Account not found")
                )
                return@patch
            }

            call.respond(
                HttpStatusCode.OK,
                UpdateAccountResponse(
                    success = true,
                    message = "Account active status updated successfully"
                )
            )
        }

        /**
         * PATCH /accounts/{id}/status
         */
        patch("/accounts/{id}/status") {

            val id = call.parameters["id"]?.toIntOrNull()

            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid account ID"))
                return@patch
            }

            val request = call.receive<UpdateAccountStatusRequest>()
            val nextStatus = request.status.trim().lowercase()

            val allowedStatuses = setOf(
                "active", "inactive", "suspended", "pending", "failed"
            )

            if (nextStatus !in allowedStatuses) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("message" to "Invalid account status: $nextStatus")
                )
                return@patch
            }

            val updated = AccountsRepository.updateAccountStatus(
                accountId = id,
                status = nextStatus
            )

            if (!updated) {
                call.respond(HttpStatusCode.NotFound, mapOf("message" to "Account not found"))
                return@patch
            }

            call.respond(
                HttpStatusCode.OK,
                UpdateAccountResponse(
                    success = true,
                    message = "Account status updated successfully"
                )
            )
        }
    }
}

@Serializable
data class UpdateAccountActiveRequest(
    val isActive: Boolean
)

@Serializable
data class UpdateAccountStatusRequest(
    val status: String
)

@Serializable
data class UpdateAccountResponse(
    val success: Boolean,
    val message: String
)