package account.table

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable



object PendingAccountsTable : IntIdTable("pending_accounts") {

    val email =
        varchar(
            name = "email",
            length = 255
        ).uniqueIndex()

    /*
     * Store only the hashed password.
     * Never store the plain password.
     */
    val passwordHash =
        varchar(
            name = "password_hash",
            length = 255
        )

    val schoolName =
        varchar(
            name = "school_name",
            length = 255
        )

    val fullName =
        varchar(
            name = "full_name",
            length = 255
        )

    val phoneNumber =
        varchar(
            name = "phone_number",
            length = 50
        )

    val location =
        varchar(
            name = "location",
            length = 255
        )

    val academicYear =
        varchar(
            name = "academic_year",
            length = 50
        )

    val estimatedStudents =     integer(
        name = "estimated_students"
    )

    val profilePictureUrl =
    varchar(
     name = "profile_picture_url",
            length = 500
    ).nullable()

    val emailVerificationTokenHash =
        varchar(
            name = "email_verification_token_hash",
            length = 255
        ).uniqueIndex()

    val emailVerificationExpiresAtEpochMillis =
        long(
            name = "email_verification_expires_at_epoch_millis"
        )

    val createdAtEpochMillis =
        long(
            name = "created_at_epoch_millis"
        )

    val updatedAtEpochMillis =
        long(
            name = "updated_at_epoch_millis"
        )
}