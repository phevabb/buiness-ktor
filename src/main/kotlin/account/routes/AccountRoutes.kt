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
import com.example.account.service.TenantProvisioningService
import com.example.config.AppConfig
import io.ktor.http.ContentType

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respondText

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
        } catch (e: Exception) {
            e.printStackTrace()

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
                call.respondText(
                    text = buildVerificationErrorPage(
                        title = "Verification link missing",
                        message = "The verification link is missing a valid token. Please request a new verification email."
                    ),
                    contentType = ContentType.Text.Html,
                    status = HttpStatusCode.BadRequest
                )
                return@get
            }

            val account = AccountsRepository.verifyEmail(token)

            try {
                val tenantResponse =
                    TenantProvisioningService.createTenantForAccount(account)


                val updatedAccount =
                    AccountsRepository.saveTenantProvisioningSuccess(
                        accountId = account.id,
                        tenantResponse = tenantResponse
                    )

                call.respondText(
                    text = buildVerificationSuccessPage(
                        schoolName = updatedAccount.schoolName,
                        tenantCode = updatedAccount.tenantCode,
                        tenantSchema = updatedAccount.tenantSchema ?: "",
                        defaultDomain = updatedAccount.defaultDomain ?: "",
                        principalLoginUserId = updatedAccount.principalLoginUserId ?: "",
                        principalPin = updatedAccount.principalPin ?: "",
//                        loginUrl = "http://localhost:5173/auth/login"
                        loginUrl = "https://kogschool.com/auth/login"
                    ),
                    contentType = ContentType.Text.Html,
                    status = HttpStatusCode.OK
                )
            } catch (tenantError: Exception) {
                tenantError.printStackTrace()

                AccountsRepository.saveTenantProvisioningFailure(
                    accountId = account.id,
                    errorMessage = tenantError.message ?: "Tenant creation failed."
                )

                call.respondText(
                    text = buildVerificationPartialSuccessPage(
                        schoolName = account.schoolName,
                        supportEmail = "support@phenasystems.com"
                    ),
                    contentType = ContentType.Text.Html,
                    status = HttpStatusCode.BadGateway
                )
            }
        } catch (e: IllegalArgumentException) {
            call.respondText(
                text = buildVerificationErrorPage(
                    title = "Verification failed",
                    message = e.message ?: "This verification link is invalid or has expired."
                ),
                contentType = ContentType.Text.Html,
                status = HttpStatusCode.BadRequest
            )
        } catch (e: Exception) {
            e.printStackTrace()

            call.respondText(
                text = buildVerificationErrorPage(
                    title = "Something went wrong",
                    message = "We could not verify your account at the moment. Please try again shortly."
                ),
                contentType = ContentType.Text.Html,
                status = HttpStatusCode.InternalServerError
            )
        }
    }


    // create tenant here

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
    val encodedToken = java.net.URLEncoder.encode(
        token,
        Charsets.UTF_8.name()
    )

    return "${AppConfig.publicApiBaseUrl.trimEnd('/')}/api/accounts/verify-email?token=$encodedToken"




}

private fun buildVerificationErrorPage(
    title: String,
    message: String
): String {
    return """
        <!doctype html>
        <html lang="en">
        <head>
            <meta charset="UTF-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0" />
            <title>$title | Phena</title>

            <style>
                * {
                    box-sizing: border-box;
                }

                body {
                    margin: 0;
                    min-height: 100vh;
                    display: grid;
                    place-items: center;
                    padding: 24px;
                    color: #ffffff;
                    background:
                        radial-gradient(circle at top left, rgba(239, 68, 68, 0.2), transparent 32%),
                        radial-gradient(circle at bottom right, rgba(22, 139, 118, 0.22), transparent 38%),
                        linear-gradient(135deg, #06131f 0%, #0b2137 48%, #071926 100%);
                    font-family: Inter, ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
                }

                .card {
                    width: min(680px, 100%);
                    padding: 44px;
                    border: 1px solid rgba(255, 255, 255, 0.14);
                    border-radius: 34px;
                    background: rgba(255, 255, 255, 0.075);
                    box-shadow:
                        0 40px 120px rgba(0, 0, 0, 0.45),
                        inset 0 1px 0 rgba(255, 255, 255, 0.12);
                    backdrop-filter: blur(26px);
                }

                .icon {
                    width: 64px;
                    height: 64px;
                    display: grid;
                    place-items: center;
                    border-radius: 20px;
                    color: #071926;
                    background: #fca5a5;
                    font-size: 28px;
                    font-weight: 950;
                }

                h1 {
                    margin: 26px 0 14px;
                    font-size: clamp(34px, 5vw, 54px);
                    line-height: 1;
                    letter-spacing: -0.05em;
                }

                p {
                    margin: 0;
                    color: rgba(237, 245, 255, 0.72);
                    font-size: 16px;
                    line-height: 1.8;
                }
            </style>
        </head>

        <body>
            <main class="card">
                <div class="icon">!</div>
                <h1>${escapeHtml(title)}</h1>
                <p>${escapeHtml(message)}</p>
            </main>
        </body>
        </html>
    """.trimIndent()
}


private fun buildVerificationPartialSuccessPage(
    schoolName: String,
    supportEmail: String
): String {
    return """
        <!doctype html>
        <html lang="en">
        <head>
            <meta charset="UTF-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0" />
            <title>Email Verified | Phena</title>

            <style>
                * {
                    box-sizing: border-box;
                }

                body {
                    margin: 0;
                    min-height: 100vh;
                    display: grid;
                    place-items: center;
                    padding: 24px;
                    color: #ffffff;
                    background:
                        radial-gradient(circle at top left, rgba(220, 174, 84, 0.24), transparent 32%),
                        radial-gradient(circle at bottom right, rgba(239, 68, 68, 0.16), transparent 38%),
                        linear-gradient(135deg, #06131f 0%, #0b2137 48%, #071926 100%);
                    font-family: Inter, ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
                }

                .card {
                    width: min(720px, 100%);
                    padding: 44px;
                    border: 1px solid rgba(255, 255, 255, 0.14);
                    border-radius: 34px;
                    background: rgba(255, 255, 255, 0.075);
                    box-shadow:
                        0 40px 120px rgba(0, 0, 0, 0.45),
                        inset 0 1px 0 rgba(255, 255, 255, 0.12);
                    backdrop-filter: blur(26px);
                }

                .icon {
                    width: 64px;
                    height: 64px;
                    display: grid;
                    place-items: center;
                    border-radius: 20px;
                    color: #071926;
                    background: linear-gradient(135deg, #f5d58c, #c99635);
                    font-size: 28px;
                    font-weight: 950;
                }

                h1 {
                    margin: 26px 0 14px;
                    font-size: clamp(34px, 5vw, 54px);
                    line-height: 1;
                    letter-spacing: -0.05em;
                }

                p {
                    margin: 0 0 18px;
                    color: rgba(237, 245, 255, 0.72);
                    font-size: 16px;
                    line-height: 1.8;
                }

                a {
                    color: #f5d58c;
                    font-weight: 900;
                }
            </style>
        </head>

        <body>
            <main class="card">
                <div class="icon">✓</div>

                <h1>Email verified successfully.</h1>

                <p>
                    Your email for <strong>${escapeHtml(schoolName)}</strong> has been verified. Proceed to login.
                </p>


            </main>
        </body>
        </html>
    """.trimIndent()
}


private fun buildVerificationSuccessPage(
    schoolName: String,
    tenantCode: String,
    tenantSchema: String,
    defaultDomain: String,
    principalLoginUserId: String,
    principalPin: String,
    loginUrl: String
): String {
    return """
        <!doctype html>
        <html lang="en">
        <head>
            <meta charset="UTF-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0" />
            <title>Email Verified | Phena</title>

            <style>
                * {
                    box-sizing: border-box;
                }

                body {
                    margin: 0;
                    min-height: 100vh;
                    display: grid;
                    place-items: center;
                    padding: 24px;
                    color: #ffffff;
                    background:
                        radial-gradient(circle at top left, rgba(220, 174, 84, 0.24), transparent 32%),
                        radial-gradient(circle at bottom right, rgba(22, 139, 118, 0.28), transparent 38%),
                        linear-gradient(135deg, #06131f 0%, #0b2137 48%, #071926 100%);
                    font-family: Inter, ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
                }

                .card {
                    width: min(760px, 100%);
                    overflow: hidden;
                    border: 1px solid rgba(255, 255, 255, 0.14);
                    border-radius: 34px;
                    background: rgba(255, 255, 255, 0.075);
                    box-shadow:
                        0 40px 120px rgba(0, 0, 0, 0.45),
                        inset 0 1px 0 rgba(255, 255, 255, 0.12);
                    backdrop-filter: blur(26px);
                }

                .top {
                    padding: 44px;
                    background:
                        linear-gradient(150deg, rgba(255, 255, 255, 0.12), rgba(255, 255, 255, 0.035)),
                        radial-gradient(circle at top, rgba(212, 169, 87, 0.26), transparent 42%);
                }

                .logo {
                    width: 64px;
                    height: 64px;
                    display: grid;
                    place-items: center;
                    border-radius: 20px;
                    color: #071926;
                    background: linear-gradient(135deg, #f5d58c, #c99635);
                    font-size: 28px;
                    font-weight: 900;
                    box-shadow: 0 20px 50px rgba(212, 169, 87, 0.28);
                }

                .badge {
                    display: inline-flex;
                    align-items: center;
                    gap: 10px;
                    margin-top: 30px;
                    padding: 10px 14px;
                    border: 1px solid rgba(134, 239, 172, 0.2);
                    border-radius: 999px;
                    color: #86efac;
                    background: rgba(34, 197, 94, 0.1);
                    font-size: 13px;
                    font-weight: 900;
                    letter-spacing: 0.08em;
                    text-transform: uppercase;
                }

                .check {
                    width: 22px;
                    height: 22px;
                    display: grid;
                    place-items: center;
                    border-radius: 999px;
                    color: #071926;
                    background: #86efac;
                    font-weight: 950;
                }

                h1 {
                    margin: 24px 0 14px;
                    max-width: 620px;
                    font-size: clamp(38px, 6vw, 64px);
                    line-height: 1;
                    letter-spacing: -0.06em;
                }

                .subtitle {
                    max-width: 620px;
                    margin: 0;
                    color: rgba(237, 245, 255, 0.76);
                    font-size: 17px;
                    line-height: 1.8;
                }

                .body {
                    padding: 34px 44px 44px;
                    background: rgba(4, 18, 30, 0.36);
                }

                .summary {
                    display: grid;
                    gap: 14px;
                    margin-bottom: 28px;
                }

                .summary-item {
                    display: flex;
                    align-items: center;
                    justify-content: space-between;
                    gap: 20px;
                    padding: 18px;
                    border: 1px solid rgba(255, 255, 255, 0.11);
                    border-radius: 18px;
                    background: rgba(255, 255, 255, 0.055);
                }

                .summary-item span {
                    color: rgba(237, 245, 255, 0.58);
                    font-size: 13px;
                    font-weight: 800;
                    text-transform: uppercase;
                    letter-spacing: 0.12em;
                }

                .summary-item strong {
                    color: #ffffff;
                    text-align: right;
                    font-size: 15px;
                }

                .actions {
                    display: flex;
                    align-items: center;
                    gap: 14px;
                    flex-wrap: wrap;
                }

                .primary-btn {
                    min-height: 56px;
                    display: inline-flex;
                    align-items: center;
                    justify-content: center;
                    padding: 0 24px;
                    border-radius: 18px;
                    color: #071926;
                    background: linear-gradient(135deg, #f5d58c, #c99635);
                    box-shadow: 0 22px 55px rgba(201, 150, 53, 0.25);
                    font-size: 15px;
                    font-weight: 950;
                    text-decoration: none;
                }

                .note {
                    margin: 24px 0 0;
                    color: rgba(237, 245, 255, 0.48);
                    font-size: 13px;
                    line-height: 1.6;
                }

                @media (max-width: 620px) {
                    .top,
                    .body {
                        padding: 28px;
                    }

                    .card {
                        border-radius: 24px;
                    }

                    .summary-item {
                        align-items: flex-start;
                        flex-direction: column;
                    }

                    .summary-item strong {
                        text-align: left;
                    }

                    .primary-btn {
                        width: 100%;
                    }
                }
            </style>
        </head>

        <body>
            <main class="card">
                <section class="top">
                    <div class="logo">P</div>

                    <div class="badge">
                        <span class="check">✓</span>
                        Email Verified
                    </div>

                    <h1>Your school workspace is ready.</h1>

                    <p class="subtitle">
                        Congratulations. Your email has been verified successfully, and your dedicated
                        Phena tenant has been created for <strong>${escapeHtml(schoolName)}</strong>.
                    </p>
                </section>

                <section class="body">
                    <div class="summary">
                        <div class="summary-item">
                            <span>School</span>
                            <strong>${escapeHtml(schoolName)}</strong>
                        </div>

                        <div class="summary-item">
                            <span>Tenant Code</span>
                            <strong>${escapeHtml(tenantCode)}</strong>
                        </div>

                        <div class="summary-item">
                            <span>Status</span>
                            <strong>Active and ready</strong>
                            
                        </div>
                    </div>

                    <div class="actions">
                        <a class="primary-btn" href="${escapeHtml(loginUrl)}">
                            Continue to Login
                        </a>
                    </div>

                    <p class="note">
                        You can now sign in and continue setting up your school dashboard, academic year,
                        users, classes, fees, and student records.
                    </p>
                </section>
            </main>
        </body>
        </html>
    """.trimIndent()
}


private fun escapeHtml(value: String): String {
    return value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#x27;")
}