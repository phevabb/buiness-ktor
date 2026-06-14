package com.example

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

import org.jetbrains.exposed.v1.core.dao.id.UIntIdTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.update

@Serializable
data class ExposedUser(
    val name: String,
    val age: Int
)

class ExposedUserService(private val database: Database) {

    object Users : UIntIdTable("users") {
        val name = varchar("name", length = 50)
        val age = integer("age")
    }

    suspend fun createSchema() {
        withContext(Dispatchers.IO) {
            suspendTransaction(db = database) {
                SchemaUtils.create(Users)
            }
        }
    }

    suspend fun create(user: ExposedUser): UInt =
        withContext(Dispatchers.IO) {
            suspendTransaction(db = database) {
                val newRecord = Users.insert {
                    it[name] = user.name
                    it[age] = user.age
                }
                newRecord[Users.id].value
            }
        }

    suspend fun read(id: UInt): ExposedUser? =
        withContext(Dispatchers.IO) {
            suspendTransaction(db = database) {
                Users
                    .selectAll()
                    .where { Users.id eq id }
                    .map { row ->
                        ExposedUser(
                            name = row[Users.name],
                            age = row[Users.age]
                        )
                    }
                    .singleOrNull()
            }
        }

    suspend fun update(id: UInt, user: ExposedUser) {
        withContext(Dispatchers.IO) {
            suspendTransaction(db = database) {
                Users.update({ Users.id eq id }) {
                    it[name] = user.name
                    it[age] = user.age
                }
            }
        }
    }

    suspend fun delete(id: UInt) {
        withContext(Dispatchers.IO) {
            suspendTransaction(db = database) {
                Users.deleteWhere { Users.id eq id }
            }
        }
    }
}