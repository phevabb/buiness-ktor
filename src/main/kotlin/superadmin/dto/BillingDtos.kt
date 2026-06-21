package com.example.superadmin.dto



import kotlinx.serialization.Serializable

@Serializable
data class CreateAcademicYearRequest(
    val name: String,
    val startDateEpochMillis: Long,
    val endDateEpochMillis: Long,
    val terms: List<CreateAcademicTermRequest>
)

@Serializable
data class CreateAcademicTermRequest(
    val termCode: String,
    val termName: String,
    val termNumber: Int,
    val reopeningDateEpochMillis: Long,
    val closingDateEpochMillis: Long,
    val vacationStartDateEpochMillis: Long? = null,
    val vacationEndDateEpochMillis: Long? = null,
    val graceStartDateEpochMillis: Long,
    val graceEndDateEpochMillis: Long,
    val paymentDeadlineEpochMillis: Long,

    // Store cedis, for example: "5.00"
    val amountPerStudentCedis: String = "5.00"
)

@Serializable
data class CurrentBillingResponse(
    val accountId: Int,
    val tenantCode: String,
    val academicYearId: Int,
    val academicTermId: Int,
    val termName: String,
    val studentCount: Int,
    val amountPerStudentCedis: String,
    val totalAmountCedis: String,
    val isPaid: Boolean,
    val paymentStatus: String,
    val canMakePayment: Boolean,
    val dueDateEpochMillis: Long
)

@Serializable
data class InitializePaymentResponse(
    val invoiceId: Int,
    val reference: String,
    val authorizationUrl: String,
    val accessCode: String
)

@Serializable
data class VerifyPaymentResponse(
    val success: Boolean,
    val message: String,
    val invoiceId: Int?,
    val reference: String
)





@Serializable
data class CreateAcademicYearResponse(
    val message: String,
    val academicYearId: Int
)