package account




import auth.authRoutes
import com.example.account.routes.accountRoutes
import com.example.account.routes.dashboardRoutes

import com.example.account.service.BcryptPasswordHasher
import io.ktor.server.application.Application
import io.ktor.server.routing.routing

import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.accountModule() {
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


        }
    }
}