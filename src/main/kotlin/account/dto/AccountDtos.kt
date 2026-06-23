package com.example.account.dto



import kotlinx.serialization.Serializable
@Serializable
data class ForgotPasswordRequest(
    val email: String
)

@Serializable
data class ResetPasswordRequest(
    val token: String,
    val newPassword: String,
    val confirmPassword: String
)

@Serializable
data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String,
    val confirmPassword: String
)

@Serializable
data class SimpleAuthResponse(
    val message: String
)













@Serializable
data class CreateAccountRequest(
    val email: String,
    val password: String,
    val passwordConfirm: String,
    val schoolName: String,
    val fullName: String,
    val phoneNumber: String,
    val estimatedStudents: Int,
    val academicYear: String,
    val location: String = "",
    val profilePictureUrl: String? = null
)

@Serializable
data class UpdateAccountRequest(
    val schoolName: String? = null,
    val fullName: String? = null,
    val phoneNumber: String? = null,
    val estimatedStudents: Int? = null,
    val academicYear: String? = null,
    val location: String? = null,
    val profilePictureUrl: String? = null
)

@Serializable
data class AccountResponse(
    val id: Int,
    val email: String,
    val schoolName: String,
    val fullName: String,
    val phoneNumber: String,
    val location: String,
    val tenantCode: String,
    val academicYear: String,
    val estimatedStudents: Int,
    val subscriptionAmountPerTermPesewas: Long,
    val subscriptionAmountPerTermGhs: Double,
    val isActive: Boolean,
    val isStaff: Boolean,
    val isEmailVerified: Boolean,
    val profilePictureUrl: String?,
    val createdAtEpochMillis: Long,

    val tenantProvisioned: Boolean,
    val tenantProvisionError: String?,
    val tenantProvisionedAtEpochMillis: Long?,

    val tenantId: Int?,
    val tenantSchema: String?,
    val tenantSlug: String?,
    val defaultDomain: String?,
    val defaultLocalDomain: String?,
    val fallbackLocalUrl: String?,

    val tenantStatus: String?,

    val principalLoginUserId: String?,
    val principalPin: String?
)



@Serializable
data class RegisterAccountResponse(
    val account: AccountResponse,
    val message: String
)



@Serializable
data class MessageResponse(
    val message: String
)
@Serializable
data class ResendVerificationRequest(
    val email: String
)