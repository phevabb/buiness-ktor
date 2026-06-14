package config


import com.example.account.table.AccountsTable
import com.example.auth.table.EmailVerificationTokensTable
import com.example.auth.table.PasswordResetTokensTable
import com.example.auth.table.RefreshTokensTable
import org.jetbrains.exposed.v1.core.Table

object AppTables {
    val all: Array<Table> = arrayOf(
        AccountsTable,
        RefreshTokensTable,
        EmailVerificationTokensTable,
        PasswordResetTokensTable
    )
}