package superadmin.dto.billing


import kotlinx.serialization.Serializable

@Serializable
data class TenantBillingProfileResponse(
    val summary: BillingDashboardSummary,
    val school: BillingTenantSchoolItem
)