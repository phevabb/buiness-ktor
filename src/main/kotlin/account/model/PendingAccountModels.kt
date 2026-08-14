package account.model



data class PendingRegistrationResult(
    val email: String,
    val schoolName: String,
    val verificationToken: String,
    val replacedExistingRegistration: Boolean
)

data class PendingAccountData(
    val id: Int,
    val email: String,
    val passwordHash: String,
    val schoolName: String,
    val fullName: String,
    val phoneNumber: String,
    val location: String,
    val academicYear: String,
    val estimatedStudents: Int,
    val profilePictureUrl: String?
)