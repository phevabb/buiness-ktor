package com.example


import account.accountModule
import com.example.config.DatabaseFactory
import config.AppTables

import com.example.config.configureCors

import io.ktor.server.application.Application
import plugins.configureAuth

fun Application.module() {
    DatabaseFactory.init(*AppTables.all)

    configureAuth()
    configureSecurity()
    configureSerialization()
    configureCors()
    accountModule()

}