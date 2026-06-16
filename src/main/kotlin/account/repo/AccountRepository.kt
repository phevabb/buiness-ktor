package com.example.account.repo




import com.example.account.dto.AccountResponse
import com.example.account.dto.CreateAccountRequest
import com.example.account.dto.UpdateAccountRequest
import com.example.account.mapper.toAccount
import com.example.account.mapper.toResponse
import com.example.account.security.AccountSecurity
import com.example.account.service.CreateTenantResponse
import com.example.account.table.AccountsTable
import org.jetbrains.exposed.v1.core.eq

import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.random.Random

object AccountsRepository {

    private const val PRICE_PER_STUDENT_PER_TERM_PESEWAS = 500L
    private const val EMAIL_TOKEN_EXPIRY_MILLIS = 24L * 60L * 60L * 1000L

    data class CreatedAccountResult(
        val account: AccountResponse,
        val emailVerificationToken: String
    )



    fun findByEmail(email: String): AccountLoginData? {
        return transaction {
            AccountsTable
                .selectAll()
                .where {
                    AccountsTable.email eq email.trim().lowercase()
                }
                .limit(1)
                .map { row ->
                    AccountLoginData(
                        id = row[AccountsTable.id].value,
                        email = row[AccountsTable.email],
                        passwordHash = row[AccountsTable.passwordHash],

                        schoolName = row[AccountsTable.schoolName],
                        fullName = row[AccountsTable.fullName],
                        phoneNumber = row[AccountsTable.phoneNumber],
                        location = row[AccountsTable.location],

                        tenantCode = row[AccountsTable.tenantCode],
                        academicYear = row[AccountsTable.academicYear],
                        estimatedStudents = row[AccountsTable.estimatedStudents],
                        subscriptionAmountPerTermPesewas = row[AccountsTable.subscriptionAmountPerTermPesewas],

                        isActive = row[AccountsTable.isActive],
                        isStaff = row[AccountsTable.isStaff],
                        isEmailVerified = row[AccountsTable.isEmailVerified],
                        profilePictureUrl = row[AccountsTable.profilePictureUrl],

                        tenantProvisioned = row[AccountsTable.tenantProvisioned],
                        tenantProvisionError = row[AccountsTable.tenantProvisionError],
                        tenantProvisionedAtEpochMillis = row[AccountsTable.tenantProvisionedAtEpochMillis],

                        tenantId = row[AccountsTable.tenantId],
                        tenantSchema = row[AccountsTable.tenantSchema],
                        tenantSlug = row[AccountsTable.tenantSlug],
                        defaultDomain = row[AccountsTable.defaultDomain],
                        tenantStatus = row[AccountsTable.tenantStatus],

                        principalLoginUserId = row[AccountsTable.principalLoginUserId],
                        principalPin = row[AccountsTable.principalPin]
                    )
                }
                .singleOrNull()
        }
    }


    fun findById(
        id: Int
    ): AccountResponse? = transaction {
        AccountsTable
            .selectAll()
            .where { AccountsTable.id eq id }
            .singleOrNull()
            ?.toAccount()
            ?.toResponse()
    }

    fun create(
        req: CreateAccountRequest
    ): CreatedAccountResult = transaction {

        validateCreateRequest(req)

        val normalizedEmail = req.email.trim().lowercase()

        val emailExists = AccountsTable
            .selectAll()
            .where { AccountsTable.email eq normalizedEmail }
            .singleOrNull() != null

        if (emailExists) {
            throw IllegalArgumentException("An account with this email already exists.")
        }

        val now = System.currentTimeMillis()

        val tenantCode = generateUniqueTenantCode(req.schoolName)
        val passwordHash = AccountSecurity.hashPassword(req.password)

        val emailVerificationToken = AccountSecurity.generateSecureToken()
        val emailVerificationTokenHash =
            AccountSecurity.sha256(emailVerificationToken)

        val subscriptionAmountPerTermPesewas =
            calculateSubscriptionAmountPerTermPesewas(req.estimatedStudents)

        val id = AccountsTable.insertAndGetId {
            it[email] = normalizedEmail
            it[AccountsTable.passwordHash] = passwordHash

            it[schoolName] = req.schoolName.trim()
            it[fullName] = req.fullName.trim()
            it[phoneNumber] = req.phoneNumber.trim()

            it[location] = req.location.trim()

            it[AccountsTable.tenantCode] = tenantCode
            it[academicYear] = req.academicYear.trim()

            it[estimatedStudents] = req.estimatedStudents
            it[AccountsTable.subscriptionAmountPerTermPesewas] =
                subscriptionAmountPerTermPesewas

            it[isActive] = false
            it[isStaff] = false
            it[isEmailVerified] = false

            it[profilePictureUrl] =
                req.profilePictureUrl?.trim()?.ifBlank { null }

            it[AccountsTable.emailVerificationTokenHash] =
                emailVerificationTokenHash

            it[AccountsTable.emailVerificationExpiresAtEpochMillis] =
                now + EMAIL_TOKEN_EXPIRY_MILLIS

            it[createdAtEpochMillis] = now
        }.value

        val createdAccount = AccountsTable
            .selectAll()
            .where { AccountsTable.id eq id }
            .singleOrNull()
            ?.toAccount()
            ?.toResponse()
            ?: error("Account created but could not be found.")

        CreatedAccountResult(
            account = createdAccount,
            emailVerificationToken = emailVerificationToken
        )
    }

    fun verifyEmail(
        token: String
    ): AccountResponse = transaction {

        if (token.trim().isBlank()) {
            throw IllegalArgumentException("Verification token is required.")
        }

        val tokenHash = AccountSecurity.sha256(token.trim())
        val now = System.currentTimeMillis()

        val row = AccountsTable
            .selectAll()
            .where { AccountsTable.emailVerificationTokenHash eq tokenHash }
            .singleOrNull()
            ?: throw IllegalArgumentException("Invalid verification token.")

        val expiresAt =
            row[AccountsTable.emailVerificationExpiresAtEpochMillis]

        if (expiresAt == null || expiresAt < now) {
            throw IllegalArgumentException("Verification token has expired.")
        }

        val accountId = row[AccountsTable.id].value

        AccountsTable.update({ AccountsTable.id eq accountId }) {
            it[isEmailVerified] = true
            it[isActive] = true
            it[emailVerificationTokenHash] = null
            it[emailVerificationExpiresAtEpochMillis] = null
        }

        AccountsTable
            .selectAll()
            .where { AccountsTable.id eq accountId }
            .singleOrNull()
            ?.toAccount()
            ?.toResponse()
            ?: error("Account not found after email verification.")
    }

    fun resendVerificationEmail(
        email: String
    ): CreatedAccountResult = transaction {

        val normalizedEmail = email.trim().lowercase()

        if (normalizedEmail.isBlank()) {
            throw IllegalArgumentException("Email is required.")
        }

        val row = AccountsTable
            .selectAll()
            .where { AccountsTable.email eq normalizedEmail }
            .singleOrNull()
            ?: throw IllegalArgumentException("No account found with this email.")

        if (row[AccountsTable.isEmailVerified]) {
            throw IllegalArgumentException("Email is already verified.")
        }

        val accountId = row[AccountsTable.id].value

        val emailVerificationToken = AccountSecurity.generateSecureToken()
        val emailVerificationTokenHash =
            AccountSecurity.sha256(emailVerificationToken)

        AccountsTable.update({ AccountsTable.id eq accountId }) {
            it[AccountsTable.emailVerificationTokenHash] =
                emailVerificationTokenHash

            it[AccountsTable.emailVerificationExpiresAtEpochMillis] =
                System.currentTimeMillis() + EMAIL_TOKEN_EXPIRY_MILLIS
        }

        val account = AccountsTable
            .selectAll()
            .where { AccountsTable.id eq accountId }
            .singleOrNull()
            ?.toAccount()
            ?.toResponse()
            ?: error("Account not found after verification token update.")

        CreatedAccountResult(
            account = account,
            emailVerificationToken = emailVerificationToken
        )
    }

    fun update(
        id: Int,
        req: UpdateAccountRequest
    ): AccountResponse? = transaction {

        val existingRow = AccountsTable
            .selectAll()
            .where { AccountsTable.id eq id }
            .singleOrNull()
            ?: return@transaction null

        req.phoneNumber?.let {
            if (it.trim().isBlank()) {
                throw IllegalArgumentException("Phone number cannot be empty.")
            }

            if (!isValidPhoneNumber(it)) {
                throw IllegalArgumentException("Phone number is invalid.")
            }
        }

        req.estimatedStudents?.let {
            if (it < 1) {
                throw IllegalArgumentException("Estimated students must be greater than zero.")
            }
        }

        req.academicYear?.let {
            if (!isValidAcademicYear(it)) {
                throw IllegalArgumentException("Academic year must be valid, for example 2025/2026.")
            }
        }

        val nextEstimatedStudents =
            req.estimatedStudents ?: existingRow[AccountsTable.estimatedStudents]

        AccountsTable.update({ AccountsTable.id eq id }) {
            req.schoolName
                ?.trim()
                ?.takeIf { value -> value.isNotBlank() }
                ?.let { value ->
                    it[schoolName] = value
                }

            req.fullName
                ?.trim()
                ?.takeIf { value -> value.isNotBlank() }
                ?.let { value ->
                    it[fullName] = value
                }

            req.phoneNumber
                ?.trim()
                ?.takeIf { value -> value.isNotBlank() }
                ?.let { value ->
                    it[phoneNumber] = value
                }

            req.location
                ?.trim()
                ?.let { value ->
                    it[location] = value
                }

            req.academicYear
                ?.trim()
                ?.takeIf { value -> value.isNotBlank() }
                ?.let { value ->
                    it[academicYear] = value
                }

            req.profilePictureUrl
                ?.trim()
                ?.let { value ->
                    it[profilePictureUrl] = value.ifBlank { null }
                }

            if (req.estimatedStudents != null) {
                it[estimatedStudents] = req.estimatedStudents
                it[subscriptionAmountPerTermPesewas] =
                    calculateSubscriptionAmountPerTermPesewas(
                        nextEstimatedStudents
                    )
            }
        }

        AccountsTable
            .selectAll()
            .where { AccountsTable.id eq id }
            .singleOrNull()
            ?.toAccount()
            ?.toResponse()
    }



    fun saveTenantProvisioningSuccess(
        accountId: Int,
        tenantResponse: CreateTenantResponse
    ): AccountResponse = transaction {

        AccountsTable.update({ AccountsTable.id eq accountId }) {
            it[tenantProvisioned] = true
            it[tenantProvisionError] = null
            it[tenantProvisionedAtEpochMillis] = System.currentTimeMillis()

            it[tenantId] = tenantResponse.tenantId
            it[tenantSchema] = tenantResponse.tenantSchema
            it[tenantSlug] = tenantResponse.tenantSlug
            it[defaultDomain] = tenantResponse.defaultDomain
            it[tenantStatus] = tenantResponse.status

            it[principalLoginUserId] = tenantResponse.principalLoginUserId
            it[principalPin] = tenantResponse.principalPin
        }

        AccountsTable
            .selectAll()
            .where { AccountsTable.id eq accountId }
            .singleOrNull()
            ?.toAccount()
            ?.toResponse()
            ?: error("Account not found after saving tenant provisioning response.")
    }

    fun saveTenantProvisioningFailure(
        accountId: Int,
        errorMessage: String
    ): AccountResponse = transaction {

        AccountsTable.update({ AccountsTable.id eq accountId }) {
            it[tenantProvisioned] = false
            it[tenantProvisionError] = errorMessage.take(5000)
            it[tenantProvisionedAtEpochMillis] = null

            it[tenantStatus] = "failed"
        }

        AccountsTable
            .selectAll()
            .where { AccountsTable.id eq accountId }
            .singleOrNull()
            ?.toAccount()
            ?.toResponse()
            ?: error("Account not found after saving tenant provisioning failure.")
    }

    private fun validateCreateRequest(
        req: CreateAccountRequest
    ) {
        val errors = mutableListOf<String>()

        if (req.schoolName.trim().isBlank()) {
            errors += "School name is required."
        }

        if (req.fullName.trim().isBlank()) {
            errors += "Full name is required."
        }

        if (req.email.trim().isBlank()) {
            errors += "Email is required."
        } else if (!isValidEmail(req.email)) {
            errors += "Email address is invalid."
        }

        if (req.phoneNumber.trim().isBlank()) {
            errors += "Phone number is required."
        } else if (!isValidPhoneNumber(req.phoneNumber)) {
            errors += "Phone number is invalid."
        }

        if (req.estimatedStudents < 1) {
            errors += "Estimated students must be greater than zero."
        }

        if (req.academicYear.trim().isBlank()) {
            errors += "Academic year is required."
        } else if (!isValidAcademicYear(req.academicYear)) {
            errors += "Academic year must be valid, for example 2025/2026."
        }

        if (req.password.isBlank()) {
            errors += "Password is required."
        } else if (!isStrongPassword(req.password)) {
            errors += "Password must contain at least 8 characters, uppercase, lowercase, number, and special character."
        }

        if (req.passwordConfirm.isBlank()) {
            errors += "Password confirmation is required."
        } else if (req.password != req.passwordConfirm) {
            errors += "Password and confirmation password do not match."
        }

        if (errors.isNotEmpty()) {
            throw IllegalArgumentException(errors.joinToString(" "))
        }
    }

    private fun calculateSubscriptionAmountPerTermPesewas(
        estimatedStudents: Int
    ): Long {
        return estimatedStudents * PRICE_PER_STUDENT_PER_TERM_PESEWAS
    }

    private fun isValidEmail(
        email: String
    ): Boolean {
        return Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
            .matches(email.trim())
    }

    private fun isValidPhoneNumber(
        phoneNumber: String
    ): Boolean {
        return Regex("^\\+?[0-9\\s-]{7,20}$")
            .matches(phoneNumber.trim())
    }

    private fun isStrongPassword(
        password: String
    ): Boolean {
        val hasMinLength = password.length >= 8
        val hasUppercase = password.any { it.isUpperCase() }
        val hasLowercase = password.any { it.isLowerCase() }
        val hasNumber = password.any { it.isDigit() }
        val hasSpecial = password.any { !it.isLetterOrDigit() }

        return hasMinLength &&
                hasUppercase &&
                hasLowercase &&
                hasNumber &&
                hasSpecial
    }

    private fun isValidAcademicYear(
        academicYear: String
    ): Boolean {
        val match = Regex("^(\\d{4})\\s*/\\s*(\\d{4})$")
            .matchEntire(academicYear.trim())
            ?: return false

        val startYear = match.groupValues[1].toInt()
        val endYear = match.groupValues[2].toInt()

        return endYear == startYear + 1
    }

    private fun generateUniqueTenantCode(
        schoolName: String
    ): String {
        var tenantCode: String
        var exists: Boolean

        do {
            tenantCode = generateTenantCode(schoolName)

            exists = AccountsTable
                .selectAll()
                .where { AccountsTable.tenantCode eq tenantCode }
                .singleOrNull() != null
        } while (exists)

        return tenantCode
    }

    private fun generateTenantCode(
        schoolName: String
    ): String {
        val prefix = schoolName
            .uppercase()
            .replace(Regex("[^A-Z0-9]"), "")
            .take(6)
            .ifBlank { "PHENA" }

        val suffix = Random.nextInt(100000, 999999)

        return "$prefix-$suffix"
    }


    data class AccountLoginData(
        val id: Int,
        val email: String,
        val passwordHash: String,

        val schoolName: String,
        val fullName: String,
        val phoneNumber: String,
        val location: String,

        val tenantCode: String,
        val academicYear: String,
        val estimatedStudents: Int,
        val subscriptionAmountPerTermPesewas: Long,

        val isActive: Boolean,
        val isStaff: Boolean,
        val isEmailVerified: Boolean,
        val profilePictureUrl: String?,

        val tenantProvisioned: Boolean,
        val tenantProvisionError: String?,
        val tenantProvisionedAtEpochMillis: Long?,

        val tenantId: Int?,
        val tenantSchema: String?,
        val tenantSlug: String?,
        val defaultDomain: String?,
        val tenantStatus: String?,

        val principalLoginUserId: String?,
        val principalPin: String?
    )
}



