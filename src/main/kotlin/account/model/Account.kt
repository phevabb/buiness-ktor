package com.example.account.model

data class Account(
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
    val tenantStatus: String?,

    val principalLoginUserId: String?,
    val principalPin: String?
)