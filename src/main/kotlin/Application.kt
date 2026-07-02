package com.example

import account.accountModule
import account.plugins.configureSerialization


import com.example.config.DatabaseFactory
import config.AppTables
import com.example.config.configureCors
import com.example.superadmin.client.PaystackClient
import com.example.superadmin.client.TenantSuperAdminClient
import com.example.superadmin.routes.billingRoutes
import com.example.superadmin.routes.superAdminBillingRoutes

import com.example.superadmin.services.PaymentService
import config.AppConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import superadmin.client.KtorTenantInternalBillingClient
import superadmin.repos.SuperAdminBillingRepositoryImpl

import superadmin.services.SuperAdminBillingService

fun Application.module() {

    println("STEP 1")
    DatabaseFactory.init(*AppTables.all)

    println("STEP 2")
    configureSerialization()
    configureRouting()

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
//    configureSerialization()
//    configureSecurity()

    configureCors()

    accountModule(tenantSuperAdminClient)
    val superAdminBillingRepository = SuperAdminBillingRepositoryImpl()

    val tenantInternalBillingClient = KtorTenantInternalBillingClient(
        httpClient = tenantHttpClient,

        // Production / env URL
        tenantApiBaseUrl = AppConfig.tenantApiBaseUrl,

        // Local/testing URL
        // tenantApiBaseUrl = "http://127.0.0.1:9001",

        internalApiKey = AppConfig.tenantInternalApiKey.trim()
    )

    val superAdminBillingService = SuperAdminBillingService(
        billingRepository = superAdminBillingRepository,
        tenantInternalBillingClient = tenantInternalBillingClient
    )
    routing {
        billingRoutes(paymentService)
//        superAdminBillingRoutes()
        superAdminBillingRoutes(superAdminBillingService)

    }

    println("========== [BUSINESS APP STARTED SUCCESSFULLY] ==========")
}