package com.example.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

object DatabaseFactory {

    private lateinit var database: Database

    fun init(vararg tables: Table) {
        if (::database.isInitialized) return

//        val hikariConfig = HikariConfig().apply {
//            jdbcUrl = "jdbc:postgresql://localhost:5432/ktor_business"
//            username = "postgres"
//            password = "postgres"
//            driverClassName = "org.postgresql.Driver"
//            maximumPoolSize = 10
//            minimumIdle = 2
//            isAutoCommit = false
//            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
//            validate()
//        } // for local


        val hikariConfig = HikariConfig().apply {
            jdbcUrl = "jdbc:postgresql://ep-delicate-salad-ab2s96d9-pooler.eu-west-2.aws.neon.tech:5432/neondb?sslmode=require"
            username = "neondb_owner"
            password = "npg_wZR98SylkFiQ"
            driverClassName = "org.postgresql.Driver"
            maximumPoolSize = 10
            minimumIdle = 2
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        } // NEEEOOON


        val dataSource = HikariDataSource(hikariConfig)
        database = Database.connect(dataSource)

        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(*tables)
        }
    }

    fun db(): Database = database
}