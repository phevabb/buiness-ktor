package superadmin.services

import superadmin.client.TenantInternalBillingClient
import superadmin.dto.billing.BillingDashboardResponse
import superadmin.dto.billing.BillingDashboardSummary
import superadmin.dto.billing.BillingTenantSchoolItem
import superadmin.repos.SuperAdminBillingRepository

class SuperAdminBillingService(
    private val billingRepository: SuperAdminBillingRepository,
    private val tenantInternalBillingClient: TenantInternalBillingClient
) {
    suspend fun getBillingDashboard(): BillingDashboardResponse {
        val currentBillingPeriod = billingRepository.getCurrentBillingPeriod()

        val billingRatePerStudent =
            currentBillingPeriod.amountPerStudentCedis.toDouble()

        val currentAcademicYear =
            currentBillingPeriod.academicYearName

        val currentTerm =
            currentBillingPeriod.termName

        /**
         * This comes from ktor-tenant:
         * GET /api/internal/superadmin/tenants/billing-summary
         */
        val tenants = tenantInternalBillingClient.getTenantBillingSummary()

        val lastBillingDateByTenantCode =
            billingRepository.getLastBillingDatesByTenantCode()

        val schools = tenants.map { tenant ->
            BillingTenantSchoolItem(
                tenantId = tenant.tenantId,
                schoolName = tenant.schoolName,
                tenantCode = tenant.tenantCode,
                region = tenant.location,
                studentCount = tenant.studentCount,
                currentTerm = "$currentAcademicYear - $currentTerm",
                lastBillingDate = lastBillingDateByTenantCode[tenant.tenantCode],
                status = tenant.status.uppercase(),
                estimatedAmount = tenant.studentCount * billingRatePerStudent
            )
        }

        val totalSchools = schools.size

        val activeSchools = schools.count {
            it.status.equals("ACTIVE", ignoreCase = true)
        }

        val totalStudents = schools.sumOf {
            it.studentCount
        }

        val estimatedBilling = schools.sumOf {
            it.estimatedAmount
        }

        return BillingDashboardResponse(
            summary = BillingDashboardSummary(
                totalSchools = totalSchools,
                activeSchools = activeSchools,
                totalStudents = totalStudents,
                billingRatePerStudent = billingRatePerStudent,
                estimatedBilling = estimatedBilling,
                currentAcademicYear = currentAcademicYear,
                currentTerm = currentTerm
            ),
            schools = schools
        )
    }
}