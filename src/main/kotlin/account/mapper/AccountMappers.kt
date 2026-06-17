package com.example.account.mapper

import com.example.account.dto.AccountResponse
import com.example.account.model.Account
import com.example.account.table.AccountsTable
import org.jetbrains.exposed.v1.core.ResultRow

fun ResultRow.toAccount(): Account =
    Account(
        id = this[AccountsTable.id].value,
        email = this[AccountsTable.email],
        schoolName = this[AccountsTable.schoolName],
        fullName = this[AccountsTable.fullName],
        phoneNumber = this[AccountsTable.phoneNumber],
        location = this[AccountsTable.location],
        tenantCode = this[AccountsTable.tenantCode],
        academicYear = this[AccountsTable.academicYear],
        estimatedStudents = this[AccountsTable.estimatedStudents],
        subscriptionAmountPerTermPesewas = this[AccountsTable.subscriptionAmountPerTermPesewas],
        isActive = this[AccountsTable.isActive],
        isStaff = this[AccountsTable.isStaff],
        isEmailVerified = this[AccountsTable.isEmailVerified],
        profilePictureUrl = this[AccountsTable.profilePictureUrl],
        createdAtEpochMillis = this[AccountsTable.createdAtEpochMillis],

        tenantProvisioned = this[AccountsTable.tenantProvisioned],
        tenantProvisionError = this[AccountsTable.tenantProvisionError],
        tenantProvisionedAtEpochMillis = this[AccountsTable.tenantProvisionedAtEpochMillis],

        tenantId = this[AccountsTable.tenantId],
        tenantSchema = this[AccountsTable.tenantSchema],
        tenantSlug = this[AccountsTable.tenantSlug],
        defaultDomain = this[AccountsTable.defaultDomain],
        tenantStatus = this[AccountsTable.tenantStatus],

        principalLoginUserId = this[AccountsTable.principalLoginUserId],
        principalPin = this[AccountsTable.principalPin],
        defaultLocalDomain = this[AccountsTable.defaultDomain],
        fallbackLocalUrl = this[AccountsTable.fallbackLocalUrl],
    )

fun Account.toResponse(): AccountResponse =
    AccountResponse(
        id = id,
        email = email,
        schoolName = schoolName,
        fullName = fullName,
        phoneNumber = phoneNumber,
        location = location,
        tenantCode = tenantCode,
        academicYear = academicYear,
        estimatedStudents = estimatedStudents,
        subscriptionAmountPerTermPesewas = subscriptionAmountPerTermPesewas,
        subscriptionAmountPerTermGhs = subscriptionAmountPerTermPesewas / 100.0,
        isActive = isActive,
        isStaff = isStaff,
        isEmailVerified = isEmailVerified,
        profilePictureUrl = profilePictureUrl,
        createdAtEpochMillis = createdAtEpochMillis,

        tenantProvisioned = tenantProvisioned,
        tenantProvisionError = tenantProvisionError,
        tenantProvisionedAtEpochMillis = tenantProvisionedAtEpochMillis,

        tenantId = tenantId,
        tenantSchema = tenantSchema,
        tenantSlug = tenantSlug,
        defaultDomain = defaultDomain,
        tenantStatus = tenantStatus,

        principalLoginUserId = principalLoginUserId,
        principalPin = principalPin,
        defaultLocalDomain = defaultLocalDomain,
        fallbackLocalUrl = fallbackLocalUrl,
    )