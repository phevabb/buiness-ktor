package com.example

import account.accountModule
import account.plugins.configureSerialization
import com.example.config.DatabaseFactory
import com.example.config.configureCors
import com.example.superadmin.client.PaystackClient
import com.example.superadmin.client.TenantSuperAdminClient
import com.example.superadmin.routes.billingRoutes
import com.example.superadmin.routes.superAdminBillingRoutes
import com.example.superadmin.services.PaymentService
import config.AppConfig
import config.AppTables
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import seo.seoRoutes
import superadmin.client.KtorTenantInternalBillingClient
import superadmin.repos.SuperAdminBillingRepositoryImpl
import superadmin.services.SuperAdminBillingService

fun Application.module() {

    println("STEP 1 - DATABASE INIT")

    DatabaseFactory.init(
        tables = AppTables.all.toList()
    )

    monitor.subscribe(
        ApplicationStopped
    ) {

        println("STEP STOP - CLOSING BUSINESS DATABASE")

        DatabaseFactory.close()
    }

    println("STEP 2 - SERIALIZATION")

    configureSerialization()

    println("STEP 3 - SECURITY")

    configureSecurity()

    println("STEP 4 - CORS")

    configureCors()

    println("STEP 5 - HTTP CLIENT SETUP")

    val tenantHttpClient =
        HttpClient(CIO) {

            install(ClientContentNegotiation) {

                json(
                    Json {

                        ignoreUnknownKeys =
                            true

                        isLenient =
                            true

                        prettyPrint =
                            false
                    }
                )
            }
        }

    monitor.subscribe(
        ApplicationStopped
    ) {

        println("STEP STOP - CLOSING BUSINESS HTTP CLIENT")

        tenantHttpClient.close()
    }

    println("STEP 6 - TENANT SUPER ADMIN CLIENT")

    val tenantSuperAdminClient =
        TenantSuperAdminClient(
            httpClient =
                tenantHttpClient,

            tenantBaseUrl =
                AppConfig.tenantApiBaseUrl,

            internalApiKey =
                AppConfig.tenantInternalApiKey
        )

    println("STEP 7 - PAYSTACK CLIENT")

    val paystackClient =
        PaystackClient(
            httpClient =
                tenantHttpClient,

            secretKey =
                AppConfig.paystackSecretKey
        )

    println("STEP 8 - PAYMENT SERVICE")

    val paymentService =
        PaymentService(
            paystackClient =
                paystackClient,

            callbackBaseUrl =
                AppConfig.businessFrontendUrl
        )

    println("STEP 9 - ACCOUNT MODULE")

    accountModule(
        tenantSuperAdminClient
    )

    println("STEP 10 - SUPER ADMIN BILLING SERVICES")

    val superAdminBillingRepository =
        SuperAdminBillingRepositoryImpl()

    val tenantInternalBillingClient =
        KtorTenantInternalBillingClient(
            httpClient =
                tenantHttpClient,

            tenantApiBaseUrl =
                AppConfig.tenantApiBaseUrl,

            internalApiKey =
                AppConfig.tenantInternalApiKey.trim()
        )

    val superAdminBillingService =
        SuperAdminBillingService(
            billingRepository =
                superAdminBillingRepository,

            tenantInternalBillingClient =
                tenantInternalBillingClient
        )

    println("STEP 11 - DEFAULT ROUTING CONFIG")

    configureRouting()

    println("STEP 12 - CUSTOM ROUTES")

    routing {

        seoRoutes()

        billingRoutes(
            paymentService
        )

        superAdminBillingRoutes(
            superAdminBillingService
        )
    }

    println("========== [BUSINESS APP STARTED SUCCESSFULLY] ==========")
}