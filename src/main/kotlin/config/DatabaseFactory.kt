package com.example.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

object DatabaseFactory {

    private lateinit var database: Database

    private fun getEnv(name: String, default: String): String {
        return System.getenv(name)
            ?: System.getProperty(name)
            ?: default
    }

    fun init(vararg tables: Table) {
        if (::database.isInitialized) return

        val hikariConfig = HikariConfig().apply {
            jdbcUrl = getEnv(
                "DB_URL",
                "jdbc:postgresql://localhost:5432/ktor_business"
            )

            username = getEnv("DB_USER", "postgres")
            password = getEnv("DB_PASSWORD", "postgres")

            driverClassName = "org.postgresql.Driver"
            maximumPoolSize = 10
            minimumIdle = 2
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }

        val dataSource = HikariDataSource(hikariConfig)
        database = Database.connect(dataSource)

        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(*tables)
        }
    }

    fun db(): Database = database
}