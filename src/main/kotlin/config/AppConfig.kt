package com.example.config



import io.github.cdimascio.dotenv.dotenv


//
//object AppConfig {
//
//    // Business Ktor URL
//    val publicApiBaseUrl: String
//        get() = "http://127.0.0.1:9000"
//
//    // Tenant Ktor URL
//    val tenantApiBaseUrl: String
//        get() = "http://127.0.0.1:9001"
//
//    val tenantInternalApiKey: String
//        get() = "change-this-secret-key"
//
//    // Gmail SMTP
//    val smtpHost: String
//        get() = "smtp.gmail.com"
//
//    // Recommended for Gmail testing
//    val smtpPort: Int
//        get() = 465
//
//    val smtpUsername: String
//        get() = "phevab1@gmail.com"
//
//    val smtpPassword: String
//        get() = "asxkumlgijtpucya"
//
//    val smtpFromEmail: String
//        get() = smtpUsername
//
//    val smtpFromName: String
//        get() = "Phena School Management System"
//
//
//    val emailUseSsl: Boolean
//        get() = true
//
//    val emailDebug: Boolean
//        get() = true
//}


object AppConfig {

    // Business Ktor URL
    val publicApiBaseUrl: String
        get() = "https://kogschool.com/business"
    // Tenant Ktor URL
    val tenantApiBaseUrl: String
        get() = "https://kogschool.com/enterprise"
    val tenantInternalApiKey: String
        get() = "change-this-secret-key"
    // Gmail SMTP
    val smtpHost: String
        get() = "smtp.gmail.com"
    // Recommended for Gmail testing
    val smtpPort: Int
        get() = 465
    val smtpUsername: String
        get() = "phevab1@gmail.com"
    val smtpPassword: String
        get() = "asxkumlgijtpucya"
    val smtpFromEmail: String
        get() = smtpUsername
    val smtpFromName: String
        get() = "Phena School Management System"
    val emailUseSsl: Boolean
        get() = true
    val emailDebug: Boolean
        get() = true
}
