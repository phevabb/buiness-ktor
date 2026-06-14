package com.example.account.service



interface PasswordHasher {
    fun hash(rawPassword: String): String
    fun verify(rawPassword: String, hashedPassword: String): Boolean
}