package sms.routes


import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import sms.dto.RequestSenderIdRequest
import sms.dto.SenderIdMessageResponse
import superadmin.dto.sms.RejectSenderIdRequest
import superadmin.services.TenantSmsService

fun Route.smsRoutes() {

    route("/sms") {
// FOR SUPERADMIN
        get("/sender-id/all") {

            try {

                val response =
                    TenantSmsService.getAllSenderIds()

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
                            ?: "Unable to retrieve sender ID requests."
                    )
                )
            }
        }

        put("/sender-id/{id}/approve") {

            try {

                val id =
                    call.parameters["id"]
                        ?.toIntOrNull()
                        ?: throw IllegalArgumentException(
                            "Invalid sender ID request id."
                        )

                val response =
                    TenantSmsService.approveSenderId(
                        id = id
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
                        message = e.message
                            ?: "Invalid approval request."
                    )
                )

            } catch (e: Exception) {

                e.printStackTrace()

                call.respond(
                    HttpStatusCode.InternalServerError,
                    SenderIdMessageResponse(
                        success = false,
                        message = e.message
                            ?: "Unable to approve sender ID."
                    )
                )
            }
        }

        put("/sender-id/{id}/reject") {

            try {

                val id =
                    call.parameters["id"]
                        ?.toIntOrNull()
                        ?: throw IllegalArgumentException(
                            "Invalid sender ID request id."
                        )

                val request =
                    call.receive<RejectSenderIdRequest>()

                val response =
                    TenantSmsService.rejectSenderId(
                        id = id,
                        request = request
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
                        message = e.message
                            ?: "Invalid rejection request."
                    )
                )

            } catch (e: Exception) {

                e.printStackTrace()

                call.respond(
                    HttpStatusCode.InternalServerError,
                    SenderIdMessageResponse(
                        success = false,
                        message = e.message
                            ?: "Unable to reject sender ID."
                    )
                )
            }
        }

        delete("/sender-id/{id}") {

            try {

                val id =
                    call.parameters["id"]
                        ?.toIntOrNull()
                        ?: throw IllegalArgumentException(
                            "Invalid sender ID request id."
                        )

                TenantSmsService.deleteSenderId(
                    id = id
                )

                call.respond(
                    HttpStatusCode.OK,
                    SenderIdMessageResponse(
                        success = true,
                        message = "Sender ID request deleted successfully."
                    )
                )

            } catch (e: IllegalArgumentException) {

                call.respond(
                    HttpStatusCode.BadRequest,
                    SenderIdMessageResponse(
                        success = false,
                        message = e.message
                            ?: "Invalid delete request."
                    )
                )

            } catch (e: Exception) {

                e.printStackTrace()

                call.respond(
                    HttpStatusCode.InternalServerError,
                    SenderIdMessageResponse(
                        success = false,
                        message = e.message
                            ?: "Unable to delete sender ID request."
                    )
                )
            }
        }







        // BELOW ARE FOR CLIENTS / SCHOOLS
        delete("/sender-id/{id}") {

            try {

                val id =
                    call.parameters["id"]
                        ?.toIntOrNull()
                        ?: throw IllegalArgumentException(
                            "Invalid sender ID request id."
                        )

                val tenantCode =
                    call.request.headers["X-Tenant-Code"]
                        ?: throw IllegalArgumentException(
                            "Tenant code is required."
                        )

                TenantSmsService.deleteSenderId(
                    id = id,
                    tenantCode = tenantCode
                )

                call.respond(
                    HttpStatusCode.OK,
                    SenderIdMessageResponse(
                        success = true,
                        message = "Sender ID request deleted successfully."
                    )
                )

            } catch (e: IllegalArgumentException) {

                call.respond(
                    HttpStatusCode.BadRequest,
                    SenderIdMessageResponse(
                        success = false,
                        message = e.message
                            ?: "Invalid delete request."
                    )
                )

            } catch (e: Exception) {

                e.printStackTrace()

                call.respond(
                    HttpStatusCode.BadRequest,
                    SenderIdMessageResponse(
                        success = false,
                        message = e.message
                            ?: "Unable to delete sender ID request."
                    )
                )
            }
        }

        post("/sender-id/request") {

            try {

                val request =
                    call.receive<RequestSenderIdRequest>()

                if (request.tenantCode.isBlank()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        SenderIdMessageResponse(
                            success = false,
                            message = "Tenant code is required."
                        )
                    )

                    return@post
                }

                if (request.senderId.isBlank()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        SenderIdMessageResponse(
                            success = false,
                            message = "Sender ID is required."
                        )
                    )

                    return@post
                }

                val response =
                    TenantSmsService.requestSenderId(
                        request
                    )

                call.respond(
                    HttpStatusCode.Created,
                    response
                )

            } catch (e: Exception) {

                e.printStackTrace()

                val message =
                    e.message
                        ?: "Unable to request sender ID."

                val status =
                    if (
                        message.contains(
                            "already exists",
                            ignoreCase = true
                        )
                    ) {
                        HttpStatusCode.Conflict
                    } else {
                        HttpStatusCode.BadRequest
                    }

                call.respond(
                    status,
                    SenderIdMessageResponse(
                        success = false,
                        message = message
                    )
                )
            }}

        get("/sender-id/latest/{tenantCode}") {

            try {

                val tenantCode =
                    call.parameters["tenantCode"]
                        ?: throw IllegalArgumentException(
                            "Tenant code is required."
                        )

                val response =
                    TenantSmsService.getLatestSenderId(
                        tenantCode
                    )

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
                            ?: "Unable to retrieve sender ID."
                    )
                )
            }
        }
    }
}