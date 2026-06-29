package com.example.superadmin.repos


import com.example.superadmin.table.SuperAdminsTable

import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.selectAll

data class SuperAdmin(
    val id: Int,
    val email: String,
    val passwordHash: String,
    val isActive: Boolean
)

class SuperAdminRepository {

    fun findByEmail(email: String): SuperAdmin? = transaction {
        SuperAdminsTable
            .selectAll()
            .where{ SuperAdminsTable.email eq email }
            .map {
                SuperAdmin(
                    id = it[SuperAdminsTable.id].value,
                    email = it[SuperAdminsTable.email],
                    passwordHash = it[SuperAdminsTable.passwordHash],
                    isActive = it[SuperAdminsTable.isActive]
                )
            }
            .singleOrNull()
    }

    fun createSuperAdmin(
        email: String,
        passwordHash: String,
        fullName: String
    ): Int = transaction {

        val id = SuperAdminsTable.insertAndGetId {
            it[SuperAdminsTable.email] = email
            it[SuperAdminsTable.passwordHash] = passwordHash
            it[SuperAdminsTable.fullName] = fullName
            it[SuperAdminsTable.createdAtEpochMillis] = System.currentTimeMillis()
            it[SuperAdminsTable.isActive] = true
        }

        id.value
    }

}
