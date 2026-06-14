package com.example

import io.ktor.server.application.Application

fun Application.configureSecurity() {
    // Security-related configuration goes here later.
    // Do NOT install Authentication or JWT here.
    // JWT is configured in plugins/AuthConfig.kt via configureAuth().
}