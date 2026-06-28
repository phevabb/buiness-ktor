package com.example.config

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.cors.routing.CORS

fun Application.configureCors() {
    install(CORS) {

        // =========================
        // PRODUCTION FRONTEND
        // =========================
        allowHost("phenaschool.com", schemes = listOf("https"))
        allowHost("www.phenaschool.com", schemes = listOf("https"))

        // =========================
        // LOCAL / TESTING
        // =========================
        allowHost("localhost:5173", schemes = listOf("http"))
        allowHost("127.0.0.1:5173", schemes = listOf("http"))

        // If testing with old local frontend port:
        allowHost("localhost:3000", schemes = listOf("http"))
        allowHost("127.0.0.1:3000", schemes = listOf("http"))

        // =========================
        // METHODS
        // =========================
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Delete)

        // =========================
        // HEADERS
        // =========================
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.Accept)

        // Your custom headers
        allowHeader("X-Tenant-Slug")
        allowHeader("X-Tenant-Code")
        allowHeader("X-Internal-Api-Key")

        // If frontend sends cookies or Authorization
        allowCredentials = true

        maxAgeInSeconds = 3600
    }
}