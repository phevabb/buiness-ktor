package superadmin.repos

import com.example.superadmin.table.AcademicTermsTable
import com.example.superadmin.table.AcademicYearsTable
import com.example.superadmin.table.SubscriptionInvoicesTable
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.core.lessEq
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.experimental.newSuspendedTransaction
import java.time.Instant
import java.time.ZoneId

class SuperAdminBillingRepositoryImpl : SuperAdminBillingRepository {

    override suspend fun getCurrentBillingPeriod(
        dateEpochMillis: Long
    ): CurrentBillingPeriodRaw {
        return newSuspendedTransaction(Dispatchers.IO) {
            val currentTerm = AcademicTermsTable
                .selectAll()
                .where {
                    (AcademicTermsTable.reopeningDateEpochMillis lessEq dateEpochMillis) and
                            (AcademicTermsTable.closingDateEpochMillis greaterEq dateEpochMillis)
                }
                .singleOrNull()
                ?: throw IllegalStateException("No active academic term found for current date.")

            val academicYearId = currentTerm[AcademicTermsTable.academicYearId]

            val academicYear = AcademicYearsTable
                .selectAll()
                .where {
                    AcademicYearsTable.id eq academicYearId
                }
                .singleOrNull()
                ?: throw IllegalStateException("Academic year not found for current term.")

            CurrentBillingPeriodRaw(
                academicYearId = academicYear[AcademicYearsTable.id].value,
                academicYearName = academicYear[AcademicYearsTable.name],
                academicTermId = currentTerm[AcademicTermsTable.id].value,
                termCode = currentTerm[AcademicTermsTable.termCode],
                termName = currentTerm[AcademicTermsTable.termName],
                termNumber = currentTerm[AcademicTermsTable.termNumber],
                amountPerStudentCedis = currentTerm[AcademicTermsTable.amountPerStudentCedis]
            )
        }
    }

    override suspend fun getLastBillingDatesByTenantCode(): Map<String, String?> {
        return newSuspendedTransaction(Dispatchers.IO) {
            val invoices = SubscriptionInvoicesTable
                .selectAll()
                .map { row ->
                    val tenantCode = row[SubscriptionInvoicesTable.tenantCode]
                    val createdAtEpochMillis = row[SubscriptionInvoicesTable.createdAtEpochMillis]

                    tenantCode to createdAtEpochMillis
                }

            invoices
                .groupBy(
                    keySelector = { it.first },
                    valueTransform = { it.second }
                )
                .mapValues { entry ->
                    val latestEpochMillis = entry.value.maxOrNull()

                    latestEpochMillis?.let {
                        Instant.ofEpochMilli(it)
                            .atZone(ZoneId.of("UTC"))
                            .toLocalDate()
                            .toString()
                    }
                }
        }
    }
}