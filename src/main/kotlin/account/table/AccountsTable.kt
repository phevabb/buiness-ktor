package com.example.account.table




import org.jetbrains.exposed.v1.core.dao.id.IntIdTable

object AccountsTable : IntIdTable("accounts") {
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)

    val schoolName = varchar("school_name", 255)
    val fullName = varchar("full_name", 255)
    val phoneNumber = varchar("phone_number", 30)

    val location = varchar("location", 255).default("")

    val tenantCode = varchar("tenant_code", 100).uniqueIndex()
    val academicYear = varchar("academic_year", 20)

    val estimatedStudents = integer("estimated_students")
    val subscriptionAmountPerTermPesewas = long("subscription_amount_per_term_pesewas")

    val isActive = bool("is_active").default(false)
    val isStaff = bool("is_staff").default(false)
    val isEmailVerified = bool("is_email_verified").default(false)

    /*
     * School logo URL.
     * The frontend can upload the logo somewhere first, then send the final URL here.
     */
    val profilePictureUrl = varchar("profile_picture_url", 500).nullable()

    val emailVerificationTokenHash = varchar("email_verification_token_hash", 255).nullable()
    val emailVerificationExpiresAtEpochMillis = long("email_verification_expires_at_epoch_millis").nullable()

    val createdAtEpochMillis = long("created_at_epoch_millis")
}