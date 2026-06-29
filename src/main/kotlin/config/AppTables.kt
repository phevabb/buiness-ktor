package config


import com.example.account.table.AccountsTable
import com.example.auth.table.EmailVerificationTokensTable
import com.example.auth.table.PasswordResetTokensTable
import com.example.auth.table.RefreshTokensTable
import com.example.superadmin.table.AcademicTermsTable
import com.example.superadmin.table.AcademicYearsTable
import com.example.superadmin.table.PaymentTransactionsTable
import com.example.superadmin.table.SubscriptionInvoicesTable
import com.example.superadmin.table.SuperAdminsTable
import org.jetbrains.exposed.v1.core.Table

object AppTables {
    val all: Array<Table> = arrayOf(
        AccountsTable,
        RefreshTokensTable,
        EmailVerificationTokensTable,
        PasswordResetTokensTable,
        SuperAdminsTable,

        AcademicYearsTable,
        AcademicTermsTable,
        SubscriptionInvoicesTable,
        PaymentTransactionsTable,

        )
}