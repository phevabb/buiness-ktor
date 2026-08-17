package superadmin.routes.sms



import io.ktor.http.HttpStatusCode

import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import sms.dto.SenderIdMessageResponse
import superadmin.services.TenantSmsService

fun Route.smsWalletAdminRoutes() {

    route("/sms/admin") {

        get("/wallets") {

            try {

                val response =
                    TenantSmsService.getAllSmsWallets()

                call.respond(
                    HttpStatusCode.OK,
                    response
                )

            } catch (e: Exception) {

                e.printStackTrace()

                call.respond(
                    HttpStatusCode.InternalServerError,
                    SenderIdMessageResponse(
                        success = false,
                        message = e.message
                            ?: "Unable to retrieve SMS wallets."
                    )
                )
            }
        }

        get("/wallets/{tenantCode}") {

            try {

                val tenantCode =
                    call.parameters["tenantCode"]
                        ?: throw IllegalArgumentException(
                            "Tenant code is required."
                        )

                val response =
                    TenantSmsService.getSmsWalletByTenantCode(
                        tenantCode = tenantCode
                    )

                call.respond(
                    HttpStatusCode.OK,
                    response
                )

            } catch (e: IllegalArgumentException) {

                call.respond(
                    HttpStatusCode.BadRequest,
                    SenderIdMessageResponse(
                        success = false,
                        message = e.message ?: "Invalid wallet request."
                    )
                )

            } catch (e: Exception) {

                e.printStackTrace()

                call.respond(
                    HttpStatusCode.InternalServerError,
                    SenderIdMessageResponse(
                        success = false,
                        message = e.message
                            ?: "Unable to retrieve SMS wallet."
                    )
                )
            }
        }



        get("/wallet-transactions") {

            try {

                val response =
                    TenantSmsService.getAllSmsWalletTransactions()

                call.respond(
                    HttpStatusCode.OK,
                    response
                )

            } catch (e: Exception) {

                e.printStackTrace()

                call.respond(
                    HttpStatusCode.InternalServerError,
                    SenderIdMessageResponse(
                        success = false,
                        message = e.message
                            ?: "Unable to retrieve SMS wallet transactions."
                    )
                )
            }
        }

        get("/wallet-transactions/{tenantCode}") {

            try {

                val tenantCode =
                    call.parameters["tenantCode"]
                        ?: throw IllegalArgumentException(
                            "Tenant code is required."
                        )

                val response =
                    TenantSmsService.getSmsWalletTransactionsByTenantCode(
                        tenantCode = tenantCode
                    )

                call.respond(
                    HttpStatusCode.OK,
                    response
                )

            } catch (e: IllegalArgumentException) {

                call.respond(
                    HttpStatusCode.BadRequest,
                    SenderIdMessageResponse(
                        success = false,
                        message = e.message ?: "Invalid transaction request."
                    )
                )

            } catch (e: Exception) {

                e.printStackTrace()

                call.respond(
                    HttpStatusCode.InternalServerError,
                    SenderIdMessageResponse(
                        success = false,
                        message = e.message
                            ?: "Unable to retrieve SMS wallet transactions."
                    )
                )
            }
        }
    }
}