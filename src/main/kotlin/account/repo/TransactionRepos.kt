package account.repo



import com.example.superadmin.table.AcademicYearsTable
import com.example.superadmin.table.SubscriptionInvoicesTable
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.SortOrder
import com.example.superadmin.table.AcademicTermsTable
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

@Serializable
data class AccountTransactionResponse(
    val id: Int,
    val accountId: Int,
    val tenantCode: String,

    val academicYearId: Int,
    val academicYearName: String?,

    val academicTermId: Int,
    val termName: String?,

    val studentCount: Int,
    val amountPerStudentCedis: Double,
    val totalAmountCedis: Double,
    val isPaid: Boolean,
    val paymentStatus: String,
    val paystackReference: String?,
    val dueDateEpochMillis: Long,
    val paidAtEpochMillis: Long?,
    val createdAtEpochMillis: Long
)


object AccountTransactionRepository {

    fun findAll(
        tenantCode: String?,
        status: String?,
        paid: Boolean?
    ): List<AccountTransactionResponse> = transaction {

        val academicYearMap = AcademicYearsTable
            .selectAll()
            .associate { row ->
                row[AcademicYearsTable.id].value to row[AcademicYearsTable.name]
            }

        val academicTermMap = AcademicTermsTable
            .selectAll()
            .associate { row ->
                row[AcademicTermsTable.id].value to row[AcademicTermsTable.termName]
            }

        SubscriptionInvoicesTable
            .selectAll()
            .orderBy(SubscriptionInvoicesTable.createdAtEpochMillis, SortOrder.DESC)
            .map { row ->
                val academicYearId = row[SubscriptionInvoicesTable.academicYearId]
                val academicTermId = row[SubscriptionInvoicesTable.academicTermId]

                AccountTransactionResponse(
                    id = row[SubscriptionInvoicesTable.id].value,
                    accountId = row[SubscriptionInvoicesTable.accountId],
                    tenantCode = row[SubscriptionInvoicesTable.tenantCode],

                    academicYearId = academicYearId,
                    academicYearName = academicYearMap[academicYearId],

                    academicTermId = academicTermId,
                    termName = academicTermMap[academicTermId],

                    studentCount = row[SubscriptionInvoicesTable.studentCount],
                    amountPerStudentCedis = row[SubscriptionInvoicesTable.amountPerStudentCedis].toDouble(),
                    totalAmountCedis = row[SubscriptionInvoicesTable.totalAmountCedis].toDouble(),
                    isPaid = row[SubscriptionInvoicesTable.isPaid],
                    paymentStatus = row[SubscriptionInvoicesTable.paymentStatus],
                    paystackReference = row[SubscriptionInvoicesTable.paystackReference],
                    dueDateEpochMillis = row[SubscriptionInvoicesTable.dueDateEpochMillis],
                    paidAtEpochMillis = row[SubscriptionInvoicesTable.paidAtEpochMillis],
                    createdAtEpochMillis = row[SubscriptionInvoicesTable.createdAtEpochMillis]
                )
            }
            .filter { invoice ->
                tenantCode.isNullOrBlank() ||
                        invoice.tenantCode.equals(tenantCode.trim(), ignoreCase = true)
            }
            .filter { invoice ->
                status.isNullOrBlank() ||
                        invoice.paymentStatus.equals(status.trim(), ignoreCase = true)
            }
            .filter { invoice ->
                paid == null || invoice.isPaid == paid
            }
    }

    fun findByTenantCode(
        tenantCode: String
    ): List<AccountTransactionResponse> {
        return findAll(
            tenantCode = tenantCode,
            status = null,
            paid = null
        )
    }
}