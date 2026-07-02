package com.example.superadmin.routes

import com.example.superadmin.dto.CreateTestInvoiceRequest
import com.example.superadmin.dto.SimpleMessageResponse
import com.example.superadmin.repos.BillingRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import superadmin.dto.billing.TenantBillingProfileResponse
import superadmin.services.SuperAdminBillingService

fun Route.superAdminBillingRoutes(
    billingService: SuperAdminBillingService
) {
    route("/api/superadmin/billing") {



        authenticate("super-admin-jwt") {


        /**
         * GET /api/superadmin/billing/tenant/{tenantCode}
         *
         * Returns one tenant billing profile using the dashboard data.
         */
        get("/tenant/{tenantCode}") {
            val tenantCode = call.parameters["tenantCode"]

            if (tenantCode.isNullOrBlank()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    SimpleMessageResponse("tenantCode is required")
                )
                return@get
            }

            val dashboard = billingService.getBillingDashboard()

            val school = dashboard.schools.firstOrNull {
                it.tenantCode.equals(tenantCode, ignoreCase = true)
            }

            if (school == null) {
                call.respond(
                    HttpStatusCode.NotFound,
                    SimpleMessageResponse("Tenant not found for tenantCode: $tenantCode")
                )
                return@get
            }

            call.respond(
                HttpStatusCode.OK,
                TenantBillingProfileResponse(
                    summary = dashboard.summary,
                    school = school
                )
            )
        }

        /**
         * GET /api/superadmin/billing/tenant/{tenantCode}/invoices
         */
        get("/tenant/{tenantCode}/invoices") {
            val tenantCode = call.parameters["tenantCode"]

            if (tenantCode.isNullOrBlank()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    SimpleMessageResponse("tenantCode is required")
                )
                return@get
            }

            val canonicalTenantCode = BillingRepository.resolveCanonicalTenantCode(
                tenantCode = tenantCode
            ) ?: tenantCode

            val invoices = BillingRepository.findInvoices(
                tenantCode = canonicalTenantCode
            )

            call.respond(HttpStatusCode.OK, invoices)
        }


        /**
         * POST /api/superadmin/billing/manual-invoice
         *
         * Body:
         * {
         *   "tenantCode": "cactus273855",
         *   "studentCount": 73
         * }
         */
        post("/manual-invoice") {
            val request = call.receive<CreateTestInvoiceRequest>()

            if (request.tenantCode.isBlank()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    SimpleMessageResponse("tenantCode is required")
                )
                return@post
            }

            if (request.studentCount != null && request.studentCount <= 0) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    SimpleMessageResponse("studentCount must be greater than zero")
                )
                return@post
            }

            try {
                val response = BillingRepository.createTestPendingInvoiceForTenant(
                    tenantCode = request.tenantCode,
                    studentCountOverride = request.studentCount
                )

                call.respond(HttpStatusCode.Created, response)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    SimpleMessageResponse(e.message ?: "Unable to generate manual invoice")
                )
            }
        }





        /**
         * POST /api/superadmin/billing/manual-invoice
         *
         * Body:
         * {
         *   "tenantCode": "phevab327146",
         *   "studentCount": 119
         * }
         *
         * Generates/updates invoice for the current billing term.
         */
        post("/manual-invoice") {
            val request = call.receive<CreateTestInvoiceRequest>()

            if (request.tenantCode.isBlank()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    SimpleMessageResponse("tenantCode is required")
                )
                return@post
            }

            if (request.studentCount != null && request.studentCount <= 0) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    SimpleMessageResponse("studentCount must be greater than zero")
                )
                return@post
            }

            try {
                val response = BillingRepository.createTestPendingInvoiceForTenant(
                    tenantCode = request.tenantCode,
                    studentCountOverride = request.studentCount
                )

                call.respond(HttpStatusCode.Created, response)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    SimpleMessageResponse(e.message ?: "Unable to generate manual invoice")
                )
            }
        }

            /**
             * GET /api/superadmin/billing/dashboard
             *
             * Returns:
             * - total schools
             * - active schools
             * - total students
             * - estimated billing
             * - tenant school billing list
             */
            get("/dashboard") {
                val response = billingService.getBillingDashboard()
                call.respond(HttpStatusCode.OK, response)
            }

            /**
             * POST /api/superadmin/billing/test-invoice
             */
            post("/test-invoice") {
                val request = call.receive<CreateTestInvoiceRequest>()

                if (request.tenantCode.isBlank()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        SimpleMessageResponse("tenantCode is required")
                    )
                    return@post
                }

                try {
                    val response = BillingRepository.createTestPendingInvoiceForTenant(
                        tenantCode = request.tenantCode,
                        studentCountOverride = request.studentCount
                    )

                    call.respond(HttpStatusCode.Created, response)
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        SimpleMessageResponse(e.message ?: "Unable to create test invoice")
                    )
                }
            }

            /**
             * GET /api/superadmin/billing/academic-years
             */
            get("/academic-years") {
                val search = call.request.queryParameters["search"]

                val calendars = BillingRepository.listAcademicYearCalendars(
                    search = search
                )

                call.respond(HttpStatusCode.OK, calendars)
            }

            /**
             * GET /api/superadmin/billing/invoices
             * GET /api/superadmin/billing/invoices?status=pending
             * GET /api/superadmin/billing/invoices?search=calvary
             */
            get("/invoices") {
                val status = call.request.queryParameters["status"]
                val search = call.request.queryParameters["search"]

                val invoices = BillingRepository.listInvoices(
                    status = status,
                    search = search
                )

                call.respond(HttpStatusCode.OK, invoices)
            }

            /**
             * GET /api/superadmin/billing/transactions
             */
            get("/transactions") {
                val status = call.request.queryParameters["status"]
                val search = call.request.queryParameters["search"]

                val transactions = BillingRepository.listTransactions(
                    status = status,
                    search = search
                )

                call.respond(HttpStatusCode.OK, transactions)
            }

            /**
             * Manual trigger for testing overdue logic.
             *
             * POST /api/superadmin/billing/run-overdue-check
             * POST /api/superadmin/billing/run-overdue-check?dateEpochMillis=1800000000000
             */
            post("/run-overdue-check") {
                val dateEpochMillis = call.request.queryParameters["dateEpochMillis"]?.toLongOrNull()
                    ?: System.currentTimeMillis()

                val count = BillingRepository.markOverdueInvoices(dateEpochMillis)

                call.respond(
                    HttpStatusCode.OK,
                    SimpleMessageResponse("Overdue check completed. Updated invoices: $count")
                )
            }
        }
    }
}