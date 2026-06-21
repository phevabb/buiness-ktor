package com.example.superadmin.routes



import com.example.superadmin.dto.CreateAcademicYearRequest
import com.example.superadmin.dto.CreateAcademicYearResponse
import com.example.superadmin.dto.DeleteAcademicYearResponse
import com.example.superadmin.dto.SimpleMessageResponse
import com.example.superadmin.dto.UpdateAcademicYearResponse
import com.example.superadmin.repos.BillingRepository
import com.example.superadmin.services.PaymentService

import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route



import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.delete

import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

fun Route.billingRoutes(
    paymentService: PaymentService
) {
    route("/api/billing") {

        post("/academic-years") {
            val request = call.receive<CreateAcademicYearRequest>()

            val academicYearId = BillingRepository.createAcademicYearWithTerms(request)

            call.respond(
                HttpStatusCode.Created,
                CreateAcademicYearResponse(
                    message = "Academic year created successfully",
                    academicYearId = academicYearId
                )
            )
        }

        /**
         * Get current billing for an account.
         *
         * Normal:
         * GET /api/billing/current?accountId=1
         *
         * Testing future term:
         * GET /api/billing/current?accountId=1&dateEpochMillis=1789000000000
         */
        get("/current") {
            val accountId = call.request.queryParameters["accountId"]?.toIntOrNull()

            if (accountId == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    SimpleMessageResponse("accountId is required")
                )
                return@get
            }

            val dateEpochMillis = call.request.queryParameters["dateEpochMillis"]?.toLongOrNull()

            val billing = BillingRepository.getCurrentBillingForAccount(
                accountId = accountId,
                dateEpochMillis = dateEpochMillis
            )

            if (billing == null) {
                call.respond(
                    HttpStatusCode.NotFound,
                    SimpleMessageResponse("No active billing term found for this date")
                )
                return@get
            }

            call.respond(HttpStatusCode.OK, billing)
        }

        /**
         * Initialize Paystack payment.
         */
        post("/invoices/{invoiceId}/paystack/initialize") {
            val invoiceId = call.parameters["invoiceId"]?.toIntOrNull()

            if (invoiceId == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    SimpleMessageResponse("Invalid invoice ID")
                )
                return@post
            }

            try {
                val response = paymentService.initializePayment(invoiceId)
                call.respond(HttpStatusCode.OK, response)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    SimpleMessageResponse(e.message ?: "Unable to initialize payment")
                )
            }
        }

        /**
         * Verify Paystack payment.
         */
        get("/paystack/verify/{reference}") {
            val reference = call.parameters["reference"]

            if (reference.isNullOrBlank()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    SimpleMessageResponse("Reference is required")
                )
                return@get
            }

            val response = paymentService.verifyPayment(reference)
            call.respond(HttpStatusCode.OK, response)
        }




        patch("/academic-years/{academicYearId}") {
            val academicYearId = call.parameters["academicYearId"]?.toIntOrNull()

            if (academicYearId == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    SimpleMessageResponse("Invalid academic year ID")
                )
                return@patch
            }

            val request = call.receive<CreateAcademicYearRequest>()

            try {
                val updated = BillingRepository.updateAcademicYearCalendar(
                    academicYearId = academicYearId,
                    request = request
                )

                if (!updated) {
                    call.respond(
                        HttpStatusCode.NotFound,
                        SimpleMessageResponse("Academic year not found")
                    )
                    return@patch
                }

                call.respond(
                    HttpStatusCode.OK,
                    UpdateAcademicYearResponse(
                        message = "Academic year updated successfully",
                        academicYearId = academicYearId
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    SimpleMessageResponse(e.message ?: "Unable to update academic year")
                )
            }
        }

        delete("/academic-years/{academicYearId}") {
            val academicYearId = call.parameters["academicYearId"]?.toIntOrNull()

            if (academicYearId == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    SimpleMessageResponse("Invalid academic year ID")
                )
                return@delete
            }

            try {
                val deleted = BillingRepository.deleteAcademicYearCalendar(
                    academicYearId = academicYearId
                )

                if (!deleted) {
                    call.respond(
                        HttpStatusCode.NotFound,
                        SimpleMessageResponse("Academic year not found")
                    )
                    return@delete
                }

                call.respond(
                    HttpStatusCode.OK,
                    DeleteAcademicYearResponse(
                        message = "Academic year deleted successfully",
                        academicYearId = academicYearId
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    SimpleMessageResponse(e.message ?: "Unable to delete academic year")
                )
            }
        }


    }
}