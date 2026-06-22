package com.example.superadmin.dto


import kotlinx.serialization.Serializable

@Serializable
data class TenantAcademicCalendarSeed(
    val academicYearId: Int,
    val academicYearName: String,
    val startDateEpochMillis: Long,
    val endDateEpochMillis: Long,
    val terms: List<TenantAcademicTermSeed>
)

@Serializable
data class TenantAcademicTermSeed(
    val academicTermId: Int,
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
    val amountPerStudentCedis: String
)