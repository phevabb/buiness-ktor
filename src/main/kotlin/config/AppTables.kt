package config


import com.example.account.table.AccountsTable
import com.example.auth.table.EmailVerificationTokensTable
import com.example.auth.table.PasswordResetTokensTable
import com.example.auth.table.RefreshTokensTable
import com.example.superadmin.table.AcademicTermsTable
import com.example.superadmin.table.AcademicYearsTable
import com.example.superadmin.table.PaymentTransactionsTable
import com.example.superadmin.table.SubscriptionInvoicesTable
import org.jetbrains.exposed.v1.core.Table

object AppTables {
    val all: Array<Table> = arrayOf(
        AccountsTable,
        RefreshTokensTable,
        EmailVerificationTokensTable,
        PasswordResetTokensTable,

        AcademicYearsTable,
        AcademicTermsTable,
        SubscriptionInvoicesTable,
        PaymentTransactionsTable,

        )
}