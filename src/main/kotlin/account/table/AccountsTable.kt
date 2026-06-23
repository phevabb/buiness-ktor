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
    val profilePictureUrl = varchar("profile_picture_url", 500).nullable()
    val emailVerificationTokenHash = varchar("email_verification_token_hash", 255).nullable()
    val emailVerificationExpiresAtEpochMillis = long("email_verification_expires_at_epoch_millis").nullable()
    val createdAtEpochMillis = long("created_at_epoch_millis")
    val tenantProvisioned = bool("tenant_provisioned").default(false)
    val tenantProvisionError = text("tenant_provision_error").nullable()
    val tenantProvisionedAtEpochMillis = long("tenant_provisioned_at_epoch_millis").nullable()
    val tenantId = integer("tenant_id").nullable()
    val tenantSchema = varchar("tenant_schema", 150).nullable()
    val tenantSlug = varchar("tenant_slug", 150).nullable()
    val defaultDomain = varchar("default_domain", 255).nullable()
    val defaultLocalDomain = varchar("default_local_domain", 255).nullable()
    val fallbackLocalUrl = varchar("fallback_local_url", 255).nullable()
    val tenantStatus = varchar("tenant_status", 50).nullable()
    val principalLoginUserId = varchar("principal_login_user_id", 100).nullable()
    val principalPin = varchar("principal_pin", 50).nullable()


    // Billing / trial tracking
    val trialAcademicYearId = integer("trial_academic_year_id").nullable()
    val trialTermId = integer("trial_term_id").nullable()
    val trialStartedAtEpochMillis = long("trial_started_at_epoch_millis").nullable()
    val trialEndedAtEpochMillis = long("trial_ended_at_epoch_millis").nullable()
    val billingStatus = varchar("billing_status", 50).default("trial")
    val lastPaidAcademicYearId = integer("last_paid_academic_year_id").nullable()
    val lastPaidTermId = integer("last_paid_term_id").nullable()
    val lastPaymentAtEpochMillis = long("last_payment_at_epoch_millis").nullable()

    // password stuff
    val passwordResetTokenHash = varchar("password_reset_token_hash", 255).nullable()
    val passwordResetExpiresAtEpochMillis = long("password_reset_expires_at_epoch_millis").nullable()
}
// billing stats
//trial
//active
//pending_payment
//overdue
//suspended