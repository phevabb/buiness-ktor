package com.example.config

object AppConfig {

    fun getEnv(name: String, default: String = ""): String {
        return System.getenv(name) ?: default
    }

    val publicApiBaseUrl = getEnv("PUBLIC_API_BASE_URL", "http://127.0.0.1:9000")
    val tenantApiBaseUrl = getEnv("TENANT_API_BASE_URL", "http://127.0.0.1:9001")
    val tenantInternalApiKey = getEnv("TENANT_INTERNAL_API_KEY")

    val smtpHost = getEnv("SMTP_HOST")
    val smtpPort = getEnv("SMTP_PORT", "465").toInt()

    val smtpUsername = getEnv("SMTP_USERNAME")
    val smtpPassword = getEnv("SMTP_PASSWORD")

    val smtpFromEmail = smtpUsername
    val smtpFromName = getEnv("SMTP_FROM_NAME")

    val emailUseSsl = getEnv("EMAIL_USE_SSL", "true").toBoolean()
    val emailDebug = getEnv("EMAIL_DEBUG", "true").toBoolean()

    val paystackSecretKey = getEnv("PAYSTACK_SECRET_KEY")
}