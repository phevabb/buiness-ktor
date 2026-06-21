package com.example.superadmin.routes



import com.example.superadmin.dto.SimpleMessageResponse
import com.example.superadmin.repos.BillingRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.superAdminBillingRoutes() {
    route("/api/superadmin/billing") {


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