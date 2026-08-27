package config

object AppConfig {

    private fun getRequiredEnv(name: String): String {
        return System.getenv(name)
            ?: error("Required environment variable '$name' is not set")
    }

    val adminPhone: String
        get() = getRequiredEnv("ADMINPHONE")

    val mnotifyEndpoint: String
        get() = getRequiredEnv("MNOTIFY_ENDPOINT")

    val adminSenderId: String
        get() = getRequiredEnv("MNOTIFY_ADMIN_SENDER_ID")

    val mnotifyApiKey: String
        get() = getRequiredEnv("MNOTIFY_API_KEY")





    val publicApiBaseUrl: String
        get() = getRequiredEnv("PUBLIC_API_BASE_URL")

    val tenantApiBaseUrl: String
        get() = getRequiredEnv("TENANT_API_BASE_URL")

    val businessFrontendUrl: String
        get() = getRequiredEnv("BUSINESS_FRONTEND_URL")

    val tenantInternalApiKey: String
        get() = getRequiredEnv("TENANT_INTERNAL_API_KEY")

    val smtpHost: String
        get() = getRequiredEnv("SMTP_HOST")

    val smtpPort: Int
        get() = getRequiredEnv("SMTP_PORT").toIntOrNull()
            ?: error("Environment variable 'SMTP_PORT' must be a valid integer")

    val smtpUsername: String
        get() = getRequiredEnv("SMTP_USERNAME")

    val smtpPassword: String
        get() = getRequiredEnv("SMTP_PASSWORD")

    val smtpFromEmail: String
        get() = getRequiredEnv("SMTP_FROM_EMAIL")

    val smtpFromName: String
        get() = getRequiredEnv("SMTP_FROM_NAME")

    val emailUseSsl: Boolean
        get() = getRequiredEnv("EMAIL_USE_SSL").toBooleanStrictOrNull()
            ?: error("Environment variable 'EMAIL_USE_SSL' must be true or false")

    val emailDebug: Boolean
        get() = getRequiredEnv("EMAIL_DEBUG").toBooleanStrictOrNull()
            ?: error("Environment variable 'EMAIL_DEBUG' must be true or false")

    val paystackSecretKey: String
        get() = getRequiredEnv("PAYSTACK_SECRET_KEY")
}