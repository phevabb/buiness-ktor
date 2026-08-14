package account.repo



import account.dto.PendingAccountResponse
import account.model.PendingAccountData
import account.model.PendingRegistrationResult
import account.table.PendingAccountsTable
import com.example.account.dto.CreateAccountRequest
import com.example.account.security.AccountSecurity
import org.jetbrains.exposed.v1.core.SortOrder

import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.less

import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update

object PendingAccountsRepository {

    private const val EMAIL_TOKEN_EXPIRY_MILLIS: Long =
        24L * 60L * 60L * 1000L

    /**
     * Creates a new pending registration.
     *
     * If the email is already pending, the existing pending record
     * is updated and given a new verification token.
     *
     * The old token automatically becomes invalid because its hash
     * is replaced.
     */


    fun findAllPendingAccounts(): List<PendingAccountResponse> {

        val currentTime =
            System.currentTimeMillis()

        return transaction {

            PendingAccountsTable
                .selectAll()
                .orderBy(
                    PendingAccountsTable.createdAtEpochMillis,
                    SortOrder.DESC
                )
                .map { row ->

                    val expiresAt =
                        row[
                            PendingAccountsTable
                                .emailVerificationExpiresAtEpochMillis
                        ]

                    PendingAccountResponse(
                        id =
                            row[PendingAccountsTable.id].value,

                        email =
                            row[PendingAccountsTable.email],

                        schoolName =
                            row[PendingAccountsTable.schoolName],

                        fullName =
                            row[PendingAccountsTable.fullName],

                        phoneNumber =
                            row[PendingAccountsTable.phoneNumber],

                        location =
                            row[PendingAccountsTable.location],

                        academicYear =
                            row[PendingAccountsTable.academicYear],

                        estimatedStudents =
                            row[PendingAccountsTable.estimatedStudents],

                        profilePictureUrl =
                            row[PendingAccountsTable.profilePictureUrl],

                        verificationExpiresAtEpochMillis =
                            expiresAt,

                        createdAtEpochMillis =
                            row[
                                PendingAccountsTable.createdAtEpochMillis
                            ],

                        updatedAtEpochMillis =
                            row[
                                PendingAccountsTable.updatedAtEpochMillis
                            ],

                        verificationExpired =
                            expiresAt < currentTime
                    )
                }
        }
    }



    fun createOrReplacePending(
        req: CreateAccountRequest
    ): PendingRegistrationResult {

        return transaction {

            val normalizedEmail =
                req.email
                    .trim()
                    .lowercase()

            val passwordHash =
                AccountSecurity.hashPassword(
                    req.password
                )

            val verificationToken =
                AccountSecurity.generateSecureToken()

            val verificationTokenHash =
                AccountSecurity.sha256(
                    verificationToken
                )

            val now =
                System.currentTimeMillis()

            val expiresAt =
                now + EMAIL_TOKEN_EXPIRY_MILLIS

            val existingRow =
                PendingAccountsTable
                    .selectAll()
                    .where {
                        PendingAccountsTable.email eq normalizedEmail
                    }
                    .limit(1)
                    .singleOrNull()

            if (existingRow == null) {

                PendingAccountsTable.insert {

                    it[email] =
                        normalizedEmail

                    it[PendingAccountsTable.passwordHash] =
                        passwordHash

                    it[schoolName] =
                        req.schoolName.trim()

                    it[fullName] =
                        req.fullName.trim()

                    it[phoneNumber] =
                        req.phoneNumber.trim()

                    it[location] =
                        req.location.trim()

                    it[academicYear] =
                        req.academicYear.trim()

                    it[estimatedStudents] =
                        req.estimatedStudents

                    it[profilePictureUrl] =
                        req.profilePictureUrl
                            ?.trim()
                            ?.ifBlank { null }

                    it[emailVerificationTokenHash] =
                        verificationTokenHash

                    it[emailVerificationExpiresAtEpochMillis] =
                        expiresAt

                    it[createdAtEpochMillis] =
                        now

                    it[updatedAtEpochMillis] =
                        now
                }

            } else {

                PendingAccountsTable.update(
                    {
                        PendingAccountsTable.email eq normalizedEmail
                    }
                ) {

                    it[PendingAccountsTable.passwordHash] =
                        passwordHash

                    it[schoolName] =
                        req.schoolName.trim()

                    it[fullName] =
                        req.fullName.trim()

                    it[phoneNumber] =
                        req.phoneNumber.trim()

                    it[location] =
                        req.location.trim()

                    it[academicYear] =
                        req.academicYear.trim()

                    it[estimatedStudents] =
                        req.estimatedStudents

                    it[profilePictureUrl] =
                        req.profilePictureUrl
                            ?.trim()
                            ?.ifBlank { null }

                    it[emailVerificationTokenHash] =
                        verificationTokenHash

                    it[emailVerificationExpiresAtEpochMillis] =
                        expiresAt

                    it[updatedAtEpochMillis] =
                        now
                }
            }

            PendingRegistrationResult(
                email = normalizedEmail,
                schoolName = req.schoolName.trim(),
                verificationToken = verificationToken,
                replacedExistingRegistration =
                    existingRow != null
            )
        }
    }

    /**
     * Finds and validates a pending registration using
     * the raw token received from the email URL.
     */
    fun findByVerificationToken(
        token: String
    ): PendingAccountData {

        if (token.isBlank()) {

            throw IllegalArgumentException(
                "Verification token is required."
            )
        }

        val tokenHash =
            AccountSecurity.sha256(
                token.trim()
            )

        return transaction {

            val row =
                PendingAccountsTable
                    .selectAll()
                    .where {
                        PendingAccountsTable.emailVerificationTokenHash eq tokenHash
                    }
                    .limit(1)
                    .singleOrNull()
                    ?: throw IllegalArgumentException(
                        "This verification link is invalid or has already been used."
                    )

            val expiresAt =
                row[
                    PendingAccountsTable.emailVerificationExpiresAtEpochMillis
                ]

            if (
                expiresAt <
                System.currentTimeMillis()
            ) {

                throw IllegalArgumentException(
                    "Verification token has expired. Please register again to receive a new verification link."
                )
            }

            PendingAccountData(
                id =
                    row[PendingAccountsTable.id].value,

                email =
                    row[PendingAccountsTable.email],

                passwordHash =
                    row[PendingAccountsTable.passwordHash],

                schoolName =
                    row[PendingAccountsTable.schoolName],

                fullName =
                    row[PendingAccountsTable.fullName],

                phoneNumber =
                    row[PendingAccountsTable.phoneNumber],

                location =
                    row[PendingAccountsTable.location],

                academicYear =
                    row[PendingAccountsTable.academicYear],

                estimatedStudents =
                    row[PendingAccountsTable.estimatedStudents],

                profilePictureUrl =
                    row[PendingAccountsTable.profilePictureUrl]
            )
        }
    }

    fun emailExists(
        email: String
    ): Boolean {

        val normalizedEmail =
            email
                .trim()
                .lowercase()

        return transaction {

            PendingAccountsTable
                .selectAll()
                .where {
                    PendingAccountsTable.email eq normalizedEmail
                }
                .limit(1)
                .singleOrNull() != null
        }
    }

    fun deleteById(
        pendingAccountId: Int
    ): Boolean {

        return transaction {

            PendingAccountsTable.deleteWhere {
                PendingAccountsTable.id eq pendingAccountId
            } > 0
        }
    }

    fun deleteByEmail(
        email: String
    ): Boolean {

        val normalizedEmail =
            email
                .trim()
                .lowercase()

        return transaction {

            PendingAccountsTable.deleteWhere {
                PendingAccountsTable.email eq normalizedEmail
            } > 0
        }
    }

    /**
     * Optional cleanup method.
     * This can be executed periodically to remove expired records.
     */
    fun deleteExpiredPendingRegistrations(): Int {

        val now =
            System.currentTimeMillis()

        return transaction {

            PendingAccountsTable.deleteWhere {
                PendingAccountsTable
                    .emailVerificationExpiresAtEpochMillis less now
            }
        }
    }
}