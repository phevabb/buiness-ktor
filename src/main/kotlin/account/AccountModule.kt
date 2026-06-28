package account

import auth.authRoutes
import com.example.account.routes.accountRoutes
import com.example.account.routes.dashboardRoutes
import com.example.superadmin.client.TenantSuperAdminClient
import com.example.superadmin.routes.superAdminAccountRoutes
import com.example.superadmin.routes.superAdminTenantRoutes
import io.ktor.server.application.Application
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.accountModule(
    tenantSuperAdminClient: TenantSuperAdminClient
) {
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

            route("/super") {
                superAdminAccountRoutes()
            }

            route("/internal/super/tenant") {
                superAdminTenantRoutes(tenantSuperAdminClient)
            }
        }
    }
}