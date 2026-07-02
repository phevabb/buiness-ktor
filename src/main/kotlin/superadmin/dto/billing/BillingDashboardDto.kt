package superadmin.dto.billing



import kotlinx.serialization.Serializable

@Serializable
data class BillingDashboardResponse(
    val summary: BillingDashboardSummary,
    val schools: List<BillingTenantSchoolItem>
)

@Serializable
data class BillingDashboardSummary(
    val totalSchools: Int,
    val activeSchools: Int,
    val totalStudents: Int,
    val billingRatePerStudent: Double,
    val estimatedBilling: Double,
    val currentAcademicYear: String,
    val currentTerm: String
)

@Serializable
data class BillingTenantSchoolItem(
    val tenantId: Int,
    val schoolName: String,
    val tenantCode: String,
    val region: String?,
    val studentCount: Int,
    val currentTerm: String,
    val lastBillingDate: String?,
    val status: String,
    val estimatedAmount: Double
)
