package com.example.superadmin.dto


import kotlinx.serialization.Serializable

@Serializable
data class CreateSuperAdminRequest(
    val email: String,
    val password: String,
    val fullName: String
)