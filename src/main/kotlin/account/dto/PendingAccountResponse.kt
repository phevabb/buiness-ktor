package account.dto



import kotlinx.serialization.Serializable

@Serializable
data class PendingAccountResponse(
    val id: Int,
    val email: String,
    val schoolName: String,
    val fullName: String,
    val phoneNumber: String,
    val location: String,
    val academicYear: String,
    val estimatedStudents: Int,
    val profilePictureUrl: String?,
    val verificationExpiresAtEpochMillis: Long,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
    val verificationExpired: Boolean
)