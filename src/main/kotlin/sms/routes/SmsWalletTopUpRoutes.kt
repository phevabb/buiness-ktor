package sms.routes



import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import sms.dto.CreateWalletTopUpRequest
import sms.dto.CreateWalletTopUpResponse
import sms.dto.VerifyWalletTopUpRequest
import sms.dto.VerifyWalletTopUpResponse
import sms.services.SmsWalletTopUpService


fun Route.smsWalletTopUpRoutes() {

    route("/sms/wallet/top-up") {

        post("/create") {

            try {

                val request =
                    call.receive<CreateWalletTopUpRequest>()

                val response =
                    SmsWalletTopUpService.createTopUp(
                        request = request
                    )

                call.respond(
                    HttpStatusCode.Created,
                    response
                )

            } catch (e: IllegalArgumentException) {

                call.respond(
                    HttpStatusCode.BadRequest,
                    CreateWalletTopUpResponse(
                        success = false,
                        message = e.message ?: "Invalid wallet top-up request.",
                        reference = ""
                    )
                )

            } catch (e: Exception) {

                e.printStackTrace()

                call.respond(
                    HttpStatusCode.InternalServerError,
                    CreateWalletTopUpResponse(
                        success = false,
                        message = e.message ?: "Unable to create wallet top-up.",
                        reference = ""
                    )
                )
            }
        }

        post("/verify") {

            try {

                val request =
                    call.receive<VerifyWalletTopUpRequest>()

                val response =
                    SmsWalletTopUpService.verifyTopUp(
                        request = request
                    )

                call.respond(
                    HttpStatusCode.OK,
                    response
                )

            } catch (e: IllegalArgumentException) {

                call.respond(
                    HttpStatusCode.BadRequest,
                    VerifyWalletTopUpResponse(
                        success = false,
                        message = e.message ?: "Invalid verification request."
                    )
                )

            } catch (e: Exception) {

                e.printStackTrace()

                call.respond(
                    HttpStatusCode.InternalServerError,
                    VerifyWalletTopUpResponse(
                        success = false,
                        message = e.message ?: "Unable to verify wallet top-up."
                    )
                )
            }
        }
    }
}