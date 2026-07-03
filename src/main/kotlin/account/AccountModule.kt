package account

import account.routes.accountTransactionRoutes
import auth.authRoutes
import com.example.account.routes.accountRoutes
import com.example.account.routes.dashboardRoutes
import com.example.superadmin.client.TenantSuperAdminClient
import com.example.superadmin.repos.SuperAdminRepository
import com.example.superadmin.routes.superAdminAccountRoutes
import com.example.superadmin.routes.superAdminAuthRoutes
import com.example.superadmin.routes.superAdminTenantRoutes
import io.ktor.server.application.Application
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import superadmin.routes.superAdminTransactionRoutes

fun Application.accountModule(
    tenantSuperAdminClient: TenantSuperAdminClient
) {
    val superAdminRepo = SuperAdminRepository()

    routing {
        route("/api") {

            /**
             * Public/client account routes
             *
             * Base:
             * /api/accounts
             */
            route("/accounts") {
                accountRoutes()
            }

            /**
             * General auth routes
             *
             * Base:
             * /api/auth
             */
            route("/auth") {
                authRoutes()
            }

            /**
             * Dashboard routes
             *
             * Base:
             * /api/dashboard
             */
            route("/dashboard") {
                dashboardRoutes()
            }

            /**
             * Client account transaction routes
             *
             * Final URL:
             * GET /api/transactions/{tenantCode}
             *
             * Example:
             * GET /api/transactions/PHEVAB-847425
             */
            route("/transactions") {
                accountTransactionRoutes()
            }

            /**
             * SuperAdmin auth and account routes
             *
             * Base:
             * /api/super
             */
            route("/super") {
                superAdminAuthRoutes(superAdminRepo)
                superAdminAccountRoutes()
            }

            /**
             * SuperAdmin transaction routes
             *
             * Final URL:
             * GET /api/superadmin/account-transactions
             */
            route("/superadmin/account-transactions") {
                superAdminTransactionRoutes()
            }

            /**
             * SuperAdmin tenant routes through ktor-business -> ktor-tenant
             *
             * Base:
             * /api/internal/super/tenant
             */
            route("/internal/super/tenant") {
                superAdminTenantRoutes(tenantSuperAdminClient)
            }
        }
    }
}