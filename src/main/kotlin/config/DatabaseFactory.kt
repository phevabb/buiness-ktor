package com.example.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

object DatabaseFactory {

    private var database: Database? = null

    private var dataSource: HikariDataSource? = null

    private fun getEnv(
        name: String,
        default: String
    ): String {

        return System.getenv(name)
            ?: System.getProperty(name)
            ?: default
    }

    private fun getIntEnv(
        name: String,
        default: Int
    ): Int {

        return getEnv(
            name = name,
            default = default.toString()
        ).toIntOrNull()
            ?: default
    }

    private fun getLongEnv(
        name: String,
        default: Long
    ): Long {

        return getEnv(
            name = name,
            default = default.toString()
        ).toLongOrNull()
            ?: default
    }

    private fun getBooleanEnv(
        name: String,
        default: Boolean
    ): Boolean {

        return getEnv(
            name = name,
            default = default.toString()
        )
            .trim()
            .equals(
                other = "true",
                ignoreCase = true
            )
    }

    @Synchronized
    fun init(
        tables: List<Table>
    ) {

        if (
            database != null ||
            dataSource != null
        ) {

            println(
                "⚠️ [BUSINESS DATABASE] DatabaseFactory.init() was called more than once. Duplicate initialization skipped."
            )

            return
        }

        println(
            "======================================================"
        )

        println(
            "🗄️ [BUSINESS DATABASE] Initializing database"
        )

        println(
            "======================================================"
        )

        val databaseUrl =
            getEnv(
                name = "DB_URL",
                default = "jdbc:postgresql://localhost:5432/ktor_business?ApplicationName=ktor-business-local"
            )

        val databaseUser =
            getEnv(
                name = "DB_USER",
                default = "postgres"
            )

        val databasePassword =
            getEnv(
                name = "DB_PASSWORD",
                default = "postgres"
            )

        val maximumPoolSize =
            getIntEnv(
                name = "DB_MAX_POOL_SIZE",
                default = 5
            ).coerceIn(
                minimumValue = 1,
                maximumValue = 20
            )

        val minimumIdleConnections =
            getIntEnv(
                name = "DB_MIN_IDLE",
                default = 0
            ).coerceIn(
                minimumValue = 0,
                maximumValue = maximumPoolSize
            )

        val idleTimeoutMillis =
            getLongEnv(
                name = "DB_IDLE_TIMEOUT_MS",
                default = 60000L
            ).coerceAtLeast(
                minimumValue = 10000L
            )

        val connectionTimeoutMillis =
            getLongEnv(
                name = "DB_CONNECTION_TIMEOUT_MS",
                default = 30000L
            ).coerceAtLeast(
                minimumValue = 250L
            )

        val validationTimeoutMillis =
            getLongEnv(
                name = "DB_VALIDATION_TIMEOUT_MS",
                default = 5000L
            ).coerceAtLeast(
                minimumValue = 250L
            )

        val maxLifetimeMillis =
            getLongEnv(
                name = "DB_MAX_LIFETIME_MS",
                default = 1800000L
            )

        val leakDetectionThresholdMillis =
            getLongEnv(
                name = "DB_LEAK_DETECTION_MS",
                default = 60000L
            )

        val hikariConfig =
            HikariConfig().apply {

                jdbcUrl =
                    databaseUrl

                username =
                    databaseUser

                password =
                    databasePassword

                driverClassName =
                    "org.postgresql.Driver"

                poolName =
                    "ktor-business-hikari-pool"

                this.maximumPoolSize =
                    maximumPoolSize

                minimumIdle =
                    minimumIdleConnections

                idleTimeout =
                    idleTimeoutMillis

                connectionTimeout =
                    connectionTimeoutMillis

                validationTimeout =
                    validationTimeoutMillis

                maxLifetime =
                    maxLifetimeMillis

                keepaliveTime =
                    0L

                connectionTestQuery =
                    null

                isAutoCommit =
                    false

                transactionIsolation =
                    "TRANSACTION_READ_COMMITTED"

                initializationFailTimeout =
                    10000L

                leakDetectionThreshold =
                    leakDetectionThresholdMillis
            }

        hikariConfig.validate()

        val createdDataSource =
            HikariDataSource(
                hikariConfig
            )

        try {

            val connectedDatabase =
                Database.connect(
                    datasource = createdDataSource
                )

            dataSource =
                createdDataSource

            database =
                connectedDatabase

            println(
                "✅ [BUSINESS DATABASE] Database connection initialized"
            )

            println(
                "✅ [BUSINESS DATABASE] Pool name = ${hikariConfig.poolName}"
            )

            println(
                "✅ [BUSINESS DATABASE] maximumPoolSize = ${hikariConfig.maximumPoolSize}"
            )

            println(
                "✅ [BUSINESS DATABASE] minimumIdle = ${hikariConfig.minimumIdle}"
            )

            println(
                "✅ [BUSINESS DATABASE] idleTimeout = ${hikariConfig.idleTimeout}"
            )

            println(
                "✅ [BUSINESS DATABASE] keepaliveTime = ${hikariConfig.keepaliveTime}"
            )

            println(
                "✅ [BUSINESS DATABASE] transactionIsolation = ${hikariConfig.transactionIsolation}"
            )

            println(
                "✅ [BUSINESS DATABASE] tables supplied = ${tables.size}"
            )

            runSchemaMigrationIfEnabled(
                connectedDatabase = connectedDatabase,
                tables = tables
            )

            println(
                "✅ [BUSINESS DATABASE] Initialization completed"
            )

        } catch (e: Exception) {

            println(
                "❌ [BUSINESS DATABASE] Initialization failed: ${e.message ?: "Unknown database initialization error"}"
            )

            e.printStackTrace()

            try {

                createdDataSource.close()

            } catch (closeError: Exception) {

                println(
                    "⚠️ [BUSINESS DATABASE] Failed to close partially initialized pool: ${closeError.message}"
                )

                closeError.printStackTrace()
            }

            dataSource =
                null

            database =
                null

            throw e
        }
    }

    private fun runSchemaMigrationIfEnabled(
        connectedDatabase: Database,
        tables: List<Table>
    ) {

        val runSchemaMigration =
            getBooleanEnv(
                name = "RUN_SCHEMA_MIGRATION",
                default = false
            )

        if (!runSchemaMigration) {

            println(
                "ℹ️ [BUSINESS DATABASE] Automatic schema migration is disabled"
            )

            return
        }

        if (tables.isEmpty()) {

            println(
                "ℹ️ [BUSINESS DATABASE] No tables were supplied for schema migration"
            )

            return
        }

        println(
            "➡️ [BUSINESS DATABASE] Running schema migration for ${tables.size} tables"
        )

        transaction(
            db = connectedDatabase
        ) {

            tables.forEach { table ->

                SchemaUtils.create(
                    table
                )

                SchemaUtils
                    .addMissingColumnsStatements(
                        table
                    )
                    .forEach { statement ->

                        exec(
                            statement
                        )
                    }
            }
        }

        println(
            "✅ [BUSINESS DATABASE] Schema migration completed"
        )
    }

    fun db(): Database {

        return database
            ?: error(
                "DatabaseFactory has not been initialized. Call DatabaseFactory.init(...) during application startup."
            )
    }

    fun isInitialized(): Boolean {

        return database != null &&
                dataSource != null &&
                dataSource?.isClosed == false
    }

    fun poolStatus(): DatabasePoolStatus {

        val currentDataSource =
            dataSource

        if (
            currentDataSource == null ||
            currentDataSource.isClosed
        ) {

            return DatabasePoolStatus(
                initialized = false,
                poolName = null,
                activeConnections = 0,
                idleConnections = 0,
                totalConnections = 0,
                threadsAwaitingConnection = 0
            )
        }

        val poolBean =
            currentDataSource.hikariPoolMXBean

        return DatabasePoolStatus(
            initialized = true,
            poolName = currentDataSource.poolName,
            activeConnections = poolBean.activeConnections,
            idleConnections = poolBean.idleConnections,
            totalConnections = poolBean.totalConnections,
            threadsAwaitingConnection = poolBean.threadsAwaitingConnection
        )
    }

    @Synchronized
    fun close() {

        println(
            "➡️ [BUSINESS DATABASE] Closing database connection pool"
        )

        val currentDataSource =
            dataSource

        if (currentDataSource == null) {

            println(
                "ℹ️ [BUSINESS DATABASE] Database connection pool is already closed or was never initialized"
            )

            database =
                null

            return
        }

        try {

            if (!currentDataSource.isClosed) {

                currentDataSource.close()
            }

            println(
                "✅ [BUSINESS DATABASE] Database connection pool closed"
            )

        } catch (e: Exception) {

            println(
                "❌ [BUSINESS DATABASE] Failed to close database pool: ${e.message ?: "Unknown pool shutdown error"}"
            )

            e.printStackTrace()

        } finally {

            dataSource =
                null

            database =
                null
        }
    }
}

data class DatabasePoolStatus(
    val initialized: Boolean,
    val poolName: String?,
    val activeConnections: Int,
    val idleConnections: Int,
    val totalConnections: Int,
    val threadsAwaitingConnection: Int
)