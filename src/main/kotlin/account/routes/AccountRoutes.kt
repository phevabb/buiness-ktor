package com.example.account.routes





import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*




import com.example.account.dto.CreateAccountRequest
import com.example.account.dto.MessageResponse
import com.example.account.dto.RegisterAccountResponse
import com.example.account.dto.ResendVerificationRequest

import com.example.account.dto.UpdateAccountRequest
import com.example.account.email.AccountEmailService
import com.example.account.repo.AccountsRepository

import io.ktor.http.HttpStatusCode

import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import java.net.URLEncoder

fun Route.accountRoutes() {

    post("/register") {
        try {
            val req = call.receive<CreateAccountRequest>()

            val result = AccountsRepository.create(req)

            val verificationUrl = buildVerificationUrl(
                token = result.emailVerificationToken
            )

            AccountEmailService.sendVerificationEmail(
                to = result.account.email,
                schoolName = result.account.schoolName,
                verificationUrl = verificationUrl
            )

            call.respond(
                HttpStatusCode.Created,
                RegisterAccountResponse(
                    account = result.account,
                    message = "Account created. Please verify your email to activate the school account."
                )
            )
        } catch (e: IllegalArgumentException) {
            call.respond(
                HttpStatusCode.BadRequest,
                MessageResponse(e.message ?: "Invalid registration request.")
            )
        } catch (e: IllegalStateException) {
            call.respond(
                HttpStatusCode.Conflict,
                MessageResponse(e.message ?: "Account already exists.")
            )
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.InternalServerError,
                MessageResponse("Something went wrong. Please try again.")
            )
        }
    }

    get("/verify-email") {
        try {
            val token = call.request.queryParameters["token"]

            if (token.isNullOrBlank()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    MessageResponse("Verification token is required.")
                )
                return@get
            }

            val account = AccountsRepository.verifyEmail(token)

            call.respond(
                HttpStatusCode.OK,
                RegisterAccountResponse(
                    account = account,
                    message = "Email verified successfully. Your school account is now active."
                )
            )
        } catch (e: IllegalArgumentException) {
            call.respond(
                HttpStatusCode.BadRequest,
                MessageResponse(e.message ?: "Invalid verification token.")
            )
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.InternalServerError,
                MessageResponse("Something went wrong. Please try again.")
            )
        }
    }

    post("/resend-verification") {
        try {
            val req = call.receive<ResendVerificationRequest>()

            val result = AccountsRepository.resendVerificationEmail(req.email)

            val verificationUrl = buildVerificationUrl(
                token = result.emailVerificationToken
            )

            AccountEmailService.sendVerificationEmail(
                to = result.account.email,
                schoolName = result.account.schoolName,
                verificationUrl = verificationUrl
            )

            call.respond(
                HttpStatusCode.OK,
                MessageResponse("Verification email has been resent.")
            )
        } catch (e: IllegalArgumentException) {
            call.respond(
                HttpStatusCode.BadRequest,
                MessageResponse(e.message ?: "Invalid request.")
            )
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.InternalServerError,
                MessageResponse("Something went wrong. Please try again.")
            )
        }
    }

    get("/{id}") {
        val id = call.parameters["id"]?.toIntOrNull()

        if (id == null) {
            call.respond(HttpStatusCode.BadRequest, "Invalid account ID")
            return@get
        }

        val account = AccountsRepository.findById(id)

        if (account == null) {
            call.respond(HttpStatusCode.NotFound, "Account not found")
            return@get
        }

        call.respond(HttpStatusCode.OK, account)
    }

    put("/{id}") {
        try {
            val id = call.parameters["id"]?.toIntOrNull()

            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid account ID")
                return@put
            }

            val req = call.receive<UpdateAccountRequest>()

            val updated = AccountsRepository.update(
                id = id,
                req = req
            )

            if (updated == null) {
                call.respond(HttpStatusCode.NotFound, "Account not found")
                return@put
            }

            call.respond(HttpStatusCode.OK, updated)
        } catch (e: IllegalArgumentException) {
            call.respond(
                HttpStatusCode.BadRequest,
                MessageResponse(e.message ?: "Invalid update request.")
            )
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.InternalServerError,
                MessageResponse("Something went wrong. Please try again.")
            )
        }
    }
}

private fun buildVerificationUrl(token: String): String {
    val publicBaseUrl =
        System.getenv("PUBLIC_API_BASE_URL") ?: "http://localhost:8080"

    val encodedToken = URLEncoder.encode(
        token,
        Charsets.UTF_8.name()
    )

    return "${publicBaseUrl.trimEnd('/')}/accounts/verify-email?token=$encodedToken"
}