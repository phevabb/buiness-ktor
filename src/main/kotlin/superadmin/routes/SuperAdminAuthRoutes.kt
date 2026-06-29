package com.example.superadmin.routes

import com.example.superadmin.dto.CreateSuperAdminRequest
import com.example.superadmin.dto.LoginRequest
import com.example.superadmin.repos.SuperAdminRepository
import com.example.superadmin.security.JwtService
import com.example.superadmin.services.PasswordUtils

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import superadmin.dto.CreateSuperAdminResponse
import superadmin.dto.LoginResponse
import superadmin.dto.MessageResponse


fun Route.superAdminAuthRoutes(
    repo: SuperAdminRepository
) {

    route("/auth") {

        // ✅ LOGIN (public)
        post("/login") {
            val request = call.receive<LoginRequest>()

            val admin = repo.findByEmail(request.email)

            if (admin == null || !admin.isActive) {
                return@post call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("message" to "Invalid credentials")
                )
            }

            val valid = PasswordUtils.verify(
                request.password,
                admin.passwordHash
            )

            if (!valid) {
                return@post call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("message" to "Invalid credentials")
                )
            }

            val token = JwtService.generateToken(admin.id)

            call.respond(
                LoginResponse(
                    message = "Login successful",
                    token = token
                )
            )
        }

        // ✅ CREATE SUPER ADMIN (PROTECTED 🔥)


            post("/create") {

                val request = call.receive<CreateSuperAdminRequest>()

                // ✅ check if email already exists
                val existing = repo.findByEmail(request.email)
                if (existing != null) {
                    return@post call.respond(
                        HttpStatusCode.Conflict,
                        mapOf("message" to "Email already exists")
                    )
                }

                val hashedPassword = PasswordUtils.hash(request.password)

                val id = repo.createSuperAdmin(
                    email = request.email,
                    passwordHash = hashedPassword,
                    fullName = request.fullName
                )

                call.respond(
                    CreateSuperAdminResponse(
                        message = "SuperAdmin created successfully",
                        id = id
                    )
                )


            }


        // ✅ LOGOUT
        post("/logout") {
            call.respond(
                MessageResponse(message = "Logged out")
            )
        }
    }
}