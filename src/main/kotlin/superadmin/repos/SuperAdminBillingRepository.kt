package superadmin.repos

import java.math.BigDecimal

data class CurrentBillingPeriodRaw(
    val academicYearId: Int,
    val academicYearName: String,
    val academicTermId: Int,
    val termCode: String,
    val termName: String,
    val termNumber: Int,
    val amountPerStudentCedis: BigDecimal
)

interface SuperAdminBillingRepository {
    suspend fun getCurrentBillingPeriod(
        dateEpochMillis: Long = System.currentTimeMillis()
    ): CurrentBillingPeriodRaw

    suspend fun getLastBillingDatesByTenantCode(): Map<String, String?>
}