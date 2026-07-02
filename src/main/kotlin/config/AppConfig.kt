package config

object AppConfig {

    private fun getEnv(name: String, default: String): String {
        return System.getenv(name) ?: default
    }

    val publicApiBaseUrl: String
        get() = getEnv("PUBLIC_API_BASE_URL", "http://127.0.0.1:9000")

    val tenantApiBaseUrl: String
        get() = getEnv("TENANT_API_BASE_URL", "http://127.0.0.1:9001")

    val businessFrontendUrl: String
        get() = getEnv("BUSINESS_FRONTEND_URL", "http://localhost:5173")

    val tenantInternalApiKey: String
        get() = getEnv("TENANT_INTERNAL_API_KEY", "change-this-secret-key")

    val smtpHost: String
        get() = getEnv("SMTP_HOST", "mail.privateemail.com")

    val smtpPort: Int
        get() = getEnv("SMTP_PORT", "465").toInt()

    val smtpUsername: String
        get() = getEnv("SMTP_USERNAME", "support@phenaschool.com")

    val smtpPassword: String
        get() = getEnv("SMTP_PASSWORD", "")

    val smtpFromEmail: String
        get() = getEnv("SMTP_FROM_EMAIL", smtpUsername)

    val smtpFromName: String
        get() = getEnv("SMTP_FROM_NAME", "Phena School Management System")

    val emailUseSsl: Boolean
        get() = getEnv("EMAIL_USE_SSL", "true").toBoolean()

    val emailDebug: Boolean
        get() = getEnv("EMAIL_DEBUG", "true").toBoolean()

    val paystackSecretKey: String
        get() = getEnv("PAYSTACK_SECRET_KEY", "sk_test_7b250f25faa65af86b48c4d5ff006db68c275799")
}