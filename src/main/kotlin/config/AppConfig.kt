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
//        get() = "http://127.0.0.1:8080"
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
//    /*
//     * IMPORTANT:
//     * Use a NEW Google App Password here.
//     * Do not use your normal Gmail password.
//     * Do not include spaces.
//     *
//     * Example app password from Google may display like:
//     * abcd efgh ijkl mnop
//     *
//     * Put it as:
//     * abcdefghijklmnop
//     */
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

    /*
     * IMPORTANT:
     * Use a NEW Google App Password here.
     * Do not use your normal Gmail password.
     * Do not include spaces.
     */
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
