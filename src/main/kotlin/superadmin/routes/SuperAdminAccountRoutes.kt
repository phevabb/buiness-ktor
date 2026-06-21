package com.example.superadmin.routes

import com.example.account.repo.AccountsRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

fun Route.superAdminAccountRoutes() {
        /**
         * GET /api/superadmin/accounts
         *
         * Optional query params:
         * ?search=adisco
         * ?status=active
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
         * GET /api/superadmin/accounts/{id}
         */
        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()

            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid account ID")
                return@get
            }

            val account = AccountsRepository.findById(id)

            if (account == null) {
                call.respond(HttpStatusCode.NotFound, "Account not found")
                return@get
            }

            call.respond(HttpStatusCode.OK, account)
        }


    }
