package sms.routes

import io.ktor.http.HttpStatusCode

import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import sms.dto.PurchaseSmsCreditsRequest
import sms.dto.PurchaseSmsCreditsResponse
import superadmin.services.TenantSmsService

fun Route.smsWalletPurchaseRoutes() {

    route("/sms/wallet") {

        post("/purchase-sms") {

            try {

                val request =
                    call.receive<PurchaseSmsCreditsRequest>()

                val response =
                    TenantSmsService.purchaseSmsCredits(
                        request = request
                    )

                call.respond(
                    HttpStatusCode.OK,
                    response
                )

            } catch (e: IllegalArgumentException) {

                call.respond(
                    HttpStatusCode.BadRequest,
                    PurchaseSmsCreditsResponse(
                        success = false,
                        message = e.message
                            ?: "Invalid SMS purchase request."
                    )
                )

            } catch (e: Exception) {

                e.printStackTrace()

                call.respond(
                    HttpStatusCode.BadRequest,
                    PurchaseSmsCreditsResponse(
                        success = false,
                        message = e.message
                            ?: "Unable to purchase SMS credits."
                    )
                )
            }
        }
    }
}