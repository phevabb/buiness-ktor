package superadmin.plugins


import com.example.superadmin.routes.superAdminBillingRoutes
import config.AppConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import superadmin.client.KtorTenantInternalBillingClient
import superadmin.repos.SuperAdminBillingRepositoryImpl
import superadmin.services.SuperAdminBillingService

fun Application.configureRouting() {
    val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    prettyPrint = false
                }
            )
        }
    }

    val tenantInternalBillingClient = KtorTenantInternalBillingClient(
        httpClient = httpClient,

        // Production URL
        tenantApiBaseUrl = AppConfig.tenantApiBaseUrl,

        // Local/testing URL
        // tenantApiBaseUrl = "http://127.0.0.1:9001",

        internalApiKey = AppConfig.tenantInternalApiKey.trim()
    )

    val billingRepository = SuperAdminBillingRepositoryImpl()

    val billingService = SuperAdminBillingService(
        billingRepository = billingRepository,
        tenantInternalBillingClient = tenantInternalBillingClient
    )

    routing {
        superAdminBillingRoutes(
            billingService = billingService
        )
    }
}