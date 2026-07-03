package superadmin.routes

import superadmin.repos.SuperAdminTransactionRepository

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

fun Route.superAdminTransactionRoutes() {
    get {
        val tenantCode = call.request.queryParameters["tenantCode"]
        val status = call.request.queryParameters["status"]
        val paid = call.request.queryParameters["paid"]?.toBooleanStrictOrNull()

        val transactions = SuperAdminTransactionRepository.findAll(
            tenantCode = tenantCode,
            status = status,
            paid = paid
        )

        call.respond(HttpStatusCode.OK, transactions)
    }

    get("/{tenantCode}") {
        val tenantCode = call.parameters["tenantCode"]

        if (tenantCode.isNullOrBlank()) {
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("message" to "tenantCode is required")
            )
            return@get
        }

        val transactions = SuperAdminTransactionRepository.findByTenantCode(
            tenantCode = tenantCode
        )

        call.respond(HttpStatusCode.OK, transactions)
    }
}