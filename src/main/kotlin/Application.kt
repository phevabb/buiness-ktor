package com.example

import account.accountModule
import com.example.account.plugins.configureSerialization
import com.example.config.AppConfig
import com.example.config.DatabaseFactory
import config.AppTables
import com.example.config.configureCors
import com.example.superadmin.client.PaystackClient
import com.example.superadmin.client.TenantSuperAdminClient
import com.example.superadmin.routes.billingRoutes
import com.example.superadmin.routes.superAdminBillingRoutes
import com.example.superadmin.services.PaymentService
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json

fun Application.module() {

    println("STEP 1")
    DatabaseFactory.init(*AppTables.all)

    println("STEP 2")
    configureSerialization()

    println("STEP 3")
    configureSecurity()



    println("STEP 5")



    val tenantHttpClient = HttpClient(CIO) {
        install(ClientContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    prettyPrint = false
                }
            )
        }
    }

    val tenantSuperAdminClient = TenantSuperAdminClient(
        httpClient = tenantHttpClient,
        tenantBaseUrl = AppConfig.tenantApiBaseUrl,
        internalApiKey = AppConfig.tenantInternalApiKey
    )

    println("STEP 4")
    accountModule(tenantSuperAdminClient)

    val paystackClient = PaystackClient(
        httpClient = tenantHttpClient,
        secretKey = AppConfig.paystackSecretKey
    )

    val paymentService = PaymentService(
        paystackClient = paystackClient,

        // local/testing:
        // BUSINESS_FRONTEND_URL=http://localhost:5173
        callbackBaseUrl = AppConfig.businessFrontendUrl
    )

    DatabaseFactory.init(*AppTables.all)
    configureSerialization()
    configureSecurity()

    configureCors()

    accountModule(tenantSuperAdminClient)

    routing {
        billingRoutes(paymentService)
        superAdminBillingRoutes()
    }

    println("========== [BUSINESS APP STARTED SUCCESSFULLY] ==========")
}