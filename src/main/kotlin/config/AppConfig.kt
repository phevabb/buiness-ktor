package com.example.config



import io.github.cdimascio.dotenv.dotenv

//
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
//        get() = "mail.privateemail.com"
//
//    // Recommended for Gmail testing
//    val smtpPort: Int
//        get() = 465
//
//    val smtpUsername: String
//        get() = "support@phenaschool.com"
//
//    val smtpPassword: String
//        get() = "Uncleproton1."
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
    get() = "https://api.phenaschool.com"


val tenantApiBaseUrl: String
    get() = "https://tenant-api.phenaschool.com"



    val tenantInternalApiKey: String
        get() = "change-this-secret-key"
    // Gmail SMTP
    val smtpHost: String
        get() = "mail.privateemail.com"
    // Recommended for Gmail testing
    val smtpPort: Int
        get() = 465
    val smtpUsername: String
        get() = "support@phenaschool.com"
    val smtpPassword: String
        get() = "Uncleproton1."
    val smtpFromEmail: String
        get() = smtpUsername
    val smtpFromName: String
        get() = "Phena School Management System"
    val emailUseSsl: Boolean
        get() = true
    val emailDebug: Boolean
        get() = true
}
