package superadmin.client

import kotlinx.serialization.Serializable

@Serializable
data class InternalTenantBillingSummaryItem(
    val tenantId: Int,
    val schoolName: String,
    val tenantCode: String,
    val location: String?,
    val academicYear: String?,
    val status: String,
    val studentCount: Int
)

interface TenantInternalBillingClient {
    suspend fun getTenantBillingSummary(
        search: String? = null,
        status: String? = null
    ): List<InternalTenantBillingSummaryItem>
}