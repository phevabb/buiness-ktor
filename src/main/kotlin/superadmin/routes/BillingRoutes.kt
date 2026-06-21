package com.example.superadmin.routes



import com.example.superadmin.dto.CreateAcademicYearRequest
import com.example.superadmin.dto.CreateAcademicYearResponse
import com.example.superadmin.repos.BillingRepository
import com.example.superadmin.services.PaymentService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.billingRoutes(
    paymentService: PaymentService
) {
    route("/api/billing") {

        /**
         * SuperAdmin creates academic year + 3 terms.
         */
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
         * School dashboard checks current term billing.
         *
         * Example:
         * GET /api/billing/current?accountId=1
         */
        get("/current") {
            val accountId = call.request.queryParameters["accountId"]?.toIntOrNull()

            if (accountId == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "accountId is required"))
                return@get
            }

            val billing = BillingRepository.getCurrentBillingForAccount(accountId)

            if (billing == null) {
                call.respond(HttpStatusCode.NotFound, mapOf("message" to "No active billing term found"))
                return@get
            }

            call.respond(HttpStatusCode.OK, billing)
        }

        /**
         * Initialize Paystack payment for an invoice.
         *
         * POST /api/billing/invoices/{invoiceId}/paystack/initialize
         */
        post("/invoices/{invoiceId}/paystack/initialize") {
            val invoiceId = call.parameters["invoiceId"]?.toIntOrNull()

            if (invoiceId == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Invalid invoice ID"))
                return@post
            }

            try {
                val response = paymentService.initializePayment(invoiceId)
                call.respond(HttpStatusCode.OK, response)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("message" to (e.message ?: "Unable to initialize payment"))
                )
            }
        }

        /**
         * Verify Paystack payment.
         *
         * GET /api/billing/paystack/verify/{reference}
         */
        get("/paystack/verify/{reference}") {
            val reference = call.parameters["reference"]

            if (reference.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Reference is required"))
                return@get
            }

            val response = paymentService.verifyPayment(reference)
            call.respond(HttpStatusCode.OK, response)
        }
    }
}