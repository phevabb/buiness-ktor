package com.example.config

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*

fun Application.configureCors() {

    install(CORS) {

        // ✅ PRODUCTION FRONTEND
        allowHost("phenaschool.com", schemes = listOf("https"))
        allowHost("www.phenaschool.com", schemes = listOf("https"))

        // ✅ LOCAL DEV
        allowHost("localhost:5173", schemes = listOf("http"))
        allowHost("127.0.0.1:5173", schemes = listOf("http"))

        // ✅ METHODS
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Delete)

        // ✅ HEADERS
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.Accept)
        allowHeader("X-Tenant-Slug")
        allowHeader("X-Tenant-Code")

        // ✅ REQUIRED for cookies/auth
        allowCredentials = true

        maxAgeInSeconds = 3600
    }
}