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
    val amountPerStudentCedis: String = "5.00"
)

@Serializable
data class CreateAcademicYearResponse(
    val message: String,
    val academicYearId: Int
)

@Serializable
data class CurrentBillingResponse(
    val accountId: Int,
    val tenantCode: String,

    val invoiceId: Int,

    val academicYearId: Int,
    val academicYearName: String,

    val academicTermId: Int,
    val termCode: String,
    val termName: String,
    val termNumber: Int,

    val studentCount: Int,
    val amountPerStudentCedis: String,
    val totalAmountCedis: String,

    val isPaid: Boolean,
    val paymentStatus: String,

    val canMakePayment: Boolean,
    val paymentButtonLabel: String,

    val dueDateEpochMillis: Long,
    val graceStartDateEpochMillis: Long,
    val graceEndDateEpochMillis: Long
)

@Serializable
data class SuperAdminInvoiceResponse(
    val invoiceId: Int,
    val accountId: Int,
    val tenantCode: String,
    val schoolName: String,
    val email: String,

    val academicYearId: Int,
    val academicYearName: String,
    val academicTermId: Int,
    val termName: String,

    val studentCount: Int,
    val amountPerStudentCedis: String,
    val totalAmountCedis: String,

    val isPaid: Boolean,
    val paymentStatus: String,
    val paystackReference: String?,

    val dueDateEpochMillis: Long,
    val paidAtEpochMillis: Long?,
    val createdAtEpochMillis: Long
)

@Serializable
data class SuperAdminTransactionResponse(
    val transactionId: Int,
    val invoiceId: Int,
    val accountId: Int,
    val tenantCode: String,

    val provider: String,
    val paystackReference: String,
    val amountCedis: String,
    val currency: String,
    val status: String,

    val authorizationUrl: String?,

    val createdAtEpochMillis: Long,
    val paidAtEpochMillis: Long?
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
data class SimpleMessageResponse(
    val message: String
)

 @Serializable
data class AcademicYearCalendarResponse(
    val id: Int,
    val name: String,
    val startDateEpochMillis: Long,
    val endDateEpochMillis: Long,
    val status: String,
    val createdAtEpochMillis: Long,
    val terms: List<AcademicTermCalendarResponse>
)

@Serializable
data class AcademicTermCalendarResponse(
    val id: Int,
    val academicYearId: Int,
    val termCode: String,
    val termName: String,
    val termNumber: Int,

    val reopeningDateEpochMillis: Long,
    val closingDateEpochMillis: Long,

    val vacationStartDateEpochMillis: Long?,
    val vacationEndDateEpochMillis: Long?,

    val graceStartDateEpochMillis: Long,
    val graceEndDateEpochMillis: Long,

    val paymentDeadlineEpochMillis: Long,

    val amountPerStudentCedis: String,

    val status: String,
    val createdAtEpochMillis: Long
)



@Serializable
data class UpdateAcademicYearResponse(
    val message: String,
    val academicYearId: Int
)

@Serializable
data class DeleteAcademicYearResponse(
    val message: String,
    val academicYearId: Int
)



@Serializable
data class InvoiceResponse(
    val invoiceId: Int,
    val accountId: Int,
    val tenantCode: String,

    val academicYearId: Int,
    val academicYearName: String,

    val academicTermId: Int,
    val termCode: String,
    val termName: String,
    val termNumber: Int,

    val studentCount: Int,
    val amountPerStudentCedis: String,
    val totalAmountCedis: String,

    val isPaid: Boolean,
    val paymentStatus: String,

    val paystackReference: String?,

    val dueDateEpochMillis: Long,
    val paidAtEpochMillis: Long?,
    val createdAtEpochMillis: Long
)







// testing only

@kotlinx.serialization.Serializable
data class CreateTestInvoiceRequest(
    val tenantCode: String,
    val studentCount: Int? = null
)

@kotlinx.serialization.Serializable
data class CreateTestInvoiceResponse(
    val message: String,
    val invoiceId: Int,
    val tenantCode: String,
    val studentCount: Int,
    val amountPerStudentCedis: String,
    val totalAmountCedis: String,
    val paymentStatus: String
)