package com.example


import account.accountModule
import com.example.config.DatabaseFactory
import config.AppTables

import com.example.config.configureCors
import com.example.superadmin.client.PaystackClient

import io.ktor.server.application.Application

import com.example.superadmin.client.TenantSuperAdminClient
import com.example.superadmin.routes.billingRoutes
import com.example.superadmin.routes.superAdminBillingRoutes
import com.example.superadmin.services.PaymentService

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json



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
//  tenantBaseUrl = "https://tenant-api.phenaschool.com",           // production
    tenantBaseUrl = "http://127.0.0.1:9001",    // local
    internalApiKey = "change-this-to-a-long-random-secret"


)

val paystackClient = PaystackClient(
    httpClient = tenantHttpClient,
//    secretKey = System.getenv("PAYSTACK_SECRET_KEY") ?: "sk_test_your_key_here"




    secretKey = System.getenv("PAYSTACK_SECRET_KEY") ?: "sk_test_7b250f25faa65af86b48c4d5ff006db68c275799"

)

val paymentService = PaymentService(
    paystackClient = paystackClient,
    callbackBaseUrl = System.getenv("BUSINESS_FRONTEND_URL")
    ?: "http://localhost:3000" // fallback for local ONLY (never change this)


)

fun Application.module() {
    DatabaseFactory.init(*AppTables.all)

//    configureAuth()
   configureSecurity()
    configureSerialization()
    configureCors()
    accountModule()


    routing {
        billingRoutes(paymentService)
        superAdminBillingRoutes()
    }




}















        // Other routes...

