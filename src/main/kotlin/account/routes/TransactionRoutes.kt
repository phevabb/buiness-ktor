package account.routes





import account.repo.AccountTransactionRepository
import io.ktor.http.HttpStatusCode

import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

fun Route.accountTransactionRoutes() {



    get("/{tenantCode}") {
        val tenantCode = call.parameters["tenantCode"]

        if (tenantCode.isNullOrBlank()) {
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("message" to "tenantCode is required")
            )
            return@get
        }

        val transactions = AccountTransactionRepository.findByTenantCode(
            tenantCode = tenantCode
        )

        call.respond(HttpStatusCode.OK, transactions)
    }
}