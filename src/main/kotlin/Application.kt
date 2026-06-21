package com.example


import account.accountModule
import com.example.config.DatabaseFactory
import config.AppTables

import com.example.config.configureCors

import io.ktor.server.application.Application

import com.example.superadmin.client.TenantSuperAdminClient

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.serialization.kotlinx.json.json
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
    tenantBaseUrl = "http://127.0.0.1:9001",
    internalApiKey = "change-this-to-a-long-random-secret"

//    tenantBaseUrl = System.getenv("KTOR_TENANT_BASE_URL") ?: "http://127.0.0.1:9001",
//    internalApiKey = System.getenv("INTERNAL_API_KEY") ?: "change-this-to-a-long-random-secret"
)

fun Application.module() {
    DatabaseFactory.init(*AppTables.all)

//    configureAuth()
   configureSecurity()
    configureSerialization()
    configureCors()
    accountModule()



}















        // Other routes...

