package com.example.account.service


import org.mindrot.jbcrypt.BCrypt

class BcryptPasswordHasher : PasswordHasher {
    override fun hash(rawPassword: String): String =
        BCrypt.hashpw(rawPassword, BCrypt.gensalt())

    override fun verify(rawPassword: String, hashedPassword: String): Boolean =
        BCrypt.checkpw(rawPassword, hashedPassword)
}