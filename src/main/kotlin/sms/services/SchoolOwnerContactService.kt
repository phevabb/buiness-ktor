package sms.services



import com.example.account.table.AccountsTable
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction


object SchoolOwnerContactService {

    fun findOwnerPhoneByTenantCode(
        tenantCode: String
    ): String? {

        val normalizedTenantCode =
            normalizeTenantCode(
                tenantCode
            )

        return transaction {

            AccountsTable
                .selectAll()
                .firstOrNull { row ->

                    normalizeTenantCode(
                        row[AccountsTable.tenantCode]
                    ) == normalizedTenantCode
                }
                ?.let { row ->

                    /*
                     * Replace AccountsTable.phoneNumber with your actual contact column.
                     * Examples:
                     * AccountsTable.phone
                     * AccountsTable.contact
                     * AccountsTable.ownerContact
                     */
                    row[AccountsTable.phoneNumber]
                }
                ?.trim()
                ?.takeIf {
                    it.isNotBlank()
                }
        }
    }

    private fun normalizeTenantCode(
        tenantCode: String
    ): String {

        return tenantCode
            .trim()
            .lowercase()
            .replace(
                Regex("[^a-z0-9_]"),
                ""
            )
    }
}