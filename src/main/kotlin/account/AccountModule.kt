package account

import auth.authRoutes
import com.example.account.routes.accountRoutes
import com.example.account.routes.dashboardRoutes
import com.example.superadmin.client.TenantSuperAdminClient
import com.example.superadmin.repos.SuperAdminRepository
import com.example.superadmin.routes.superAdminAccountRoutes
import com.example.superadmin.routes.superAdminTenantRoutes
import com.example.superadmin.routes.superAdminAuthRoutes   // ✅ add this
import io.ktor.server.application.Application
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
               // ✅ add this

fun Application.accountModule(
    tenantSuperAdminClient: TenantSuperAdminClient
) {

    val superAdminRepo = SuperAdminRepository()   // ✅ create repo once

    routing {
        route("/api") {

            route("/accounts") {
                accountRoutes()
            }

            route("/auth") {
                authRoutes()
            }

            route("/dashboard") {
                dashboardRoutes()
            }

            // ✅ SUPER ADMIN AREA
            route("/super") {

                superAdminAuthRoutes(superAdminRepo)   // ✅ LOGIN/LOGOUT here

                superAdminAccountRoutes()              // ✅ protected later
            }

            route("/internal/super/tenant") {
                superAdminTenantRoutes(tenantSuperAdminClient)
            }
        }
    }
}