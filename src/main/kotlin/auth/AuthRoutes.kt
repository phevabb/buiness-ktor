package auth



import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import kotlinx.serialization.Serializable
import org.mindrot.jbcrypt.BCrypt
import com.example.account.repo.AccountsRepository
import com.example.auth.JwtConfig

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class LoginUserResponse(
    val id: Int,
    val email: String,

    val schoolName: String,
    val fullName: String,
    val phoneNumber: String,
    val location: String,

    val tenantCode: String,
    val academicYear: String,
    val estimatedStudents: Int,
    val subscriptionAmountPerTermPesewas: Long,

    val isActive: Boolean,
    val isStaff: Boolean,
    val isEmailVerified: Boolean,
    val profilePictureUrl: String?,

    val tenantProvisioned: Boolean,
    val tenantProvisionError: String?,
    val tenantProvisionedAtEpochMillis: Long?,

    val tenantId: Int?,
    val tenantSchema: String?,
    val tenantSlug: String?,
    val defaultDomain: String?,
    val tenantStatus: String?,

    val principalLoginUserId: String?,
    val principalPin: String?
)
@Serializable
data class LoginResponse(
    val access: String,
    val user: LoginUserResponse
)

fun Route.authRoutes() {

    post("/login") {
        println("========== LOGIN REQUEST START ==========")

        val req = try {
            call.receive<LoginRequest>()
        } catch (e: Exception) {
            println("LOGIN DEBUG: Failed to parse request body")
            println("LOGIN DEBUG: Parse error = ${e.message}")

            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Invalid request body")
            )
            return@post
        }

        println("LOGIN DEBUG: Request body received")
        println("LOGIN DEBUG: Raw email = '${req.email}'")
        println("LOGIN DEBUG: Password received = ${req.password.isNotBlank()}")

        val email = req.email.trim().lowercase()
        val password = req.password.trim()

        println("LOGIN DEBUG: Normalized email = '$email'")
        println("LOGIN DEBUG: Password length = ${password.length}")

        if (email.isBlank() || password.isBlank()) {
            println("LOGIN DEBUG: Email or password is blank")
            println("========== LOGIN REQUEST END: BAD REQUEST ==========")

            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Email and password are required")
            )
            return@post
        }

        println("LOGIN DEBUG: Looking up account by email...")

        val account = try {
            AccountsRepository.findByEmail(email)
        } catch (e: Exception) {
            println("LOGIN DEBUG: Database error while finding account")
            println("LOGIN DEBUG: DB error = ${e.message}")
            e.printStackTrace()

            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to "Unable to process login at this time")
            )
            return@post
        }

        if (account == null) {
            println("LOGIN DEBUG: No account found for email = '$email'")
            println("========== LOGIN REQUEST END: ACCOUNT NOT FOUND ==========")

            call.respond(
                HttpStatusCode.Unauthorized,
                mapOf("error" to "Invalid email or password")
            )
            return@post
        }

        println("LOGIN DEBUG: Account found")
        println("LOGIN DEBUG: Account ID = ${account.id}")
        println("LOGIN DEBUG: Account email = ${account.email}")
        println("LOGIN DEBUG: School name = ${account.schoolName}")
        println("LOGIN DEBUG: Full name = ${account.fullName}")
        println("LOGIN DEBUG: Tenant code = ${account.tenantCode}")
        println("LOGIN DEBUG: Is active = ${account.isActive}")
        println("LOGIN DEBUG: Is email verified = ${account.isEmailVerified}")
        println("LOGIN DEBUG: Password hash exists = ${account.passwordHash.isNotBlank()}")
        println("LOGIN DEBUG: Password hash prefix = ${account.passwordHash.take(10)}...")

        println("LOGIN DEBUG: Checking password...")

        val passwordMatches = try {
            BCrypt.checkpw(password, account.passwordHash)
        } catch (e: Exception) {
            println("LOGIN DEBUG: Password check threw exception")
            println("LOGIN DEBUG: Password check error = ${e.message}")
            e.printStackTrace()
            false
        }

        println("LOGIN DEBUG: Password matches = $passwordMatches")

        if (!passwordMatches) {
            println("LOGIN DEBUG: Invalid password for email = '$email'")
            println("========== LOGIN REQUEST END: INVALID PASSWORD ==========")

            call.respond(
                HttpStatusCode.Unauthorized,
                mapOf("error" to "Invalid email or password")
            )
            return@post
        }

        if (!account.isEmailVerified) {
            println("LOGIN DEBUG: Account email is not verified")
            println("LOGIN DEBUG: Email = ${account.email}")
            println("========== LOGIN REQUEST END: EMAIL NOT VERIFIED ==========")

            call.respond(
                HttpStatusCode.Forbidden,
                mapOf("error" to "Please verify your email before logging in")
            )
            return@post
        }

        if (!account.isActive) {
            println("LOGIN DEBUG: Account is not active")
            println("LOGIN DEBUG: Email = ${account.email}")
            println("========== LOGIN REQUEST END: ACCOUNT INACTIVE ==========")

            call.respond(
                HttpStatusCode.Forbidden,
                mapOf("error" to "Your account is not active yet")
            )
            return@post
        }

        println("LOGIN DEBUG: Generating JWT token...")

        val token = try {
            JwtConfig.generateToken(
                accountId = account.id,
                email = account.email,
                tenantCode = account.tenantCode
            )
        } catch (e: Exception) {
            println("LOGIN DEBUG: Token generation failed")
            println("LOGIN DEBUG: Token error = ${e.message}")
            e.printStackTrace()

            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to "Unable to create login session")
            )
            return@post
        }

        println("LOGIN DEBUG: Token generated successfully")
        println("LOGIN DEBUG: Token length = ${token.length}")

        val response = LoginResponse(
            access = token,
            user = LoginUserResponse(
                id = account.id,
                email = account.email,
                schoolName = account.schoolName,
                fullName = account.fullName,

                location = account.location,
                academicYear = account.academicYear,
                estimatedStudents = account.estimatedStudents,
                subscriptionAmountPerTermPesewas = account.subscriptionAmountPerTermPesewas,

                tenantCode = account.tenantCode,

                isActive = account.isActive,
                isEmailVerified = account.isEmailVerified,
                isStaff = account.isStaff,

                profilePictureUrl = account.profilePictureUrl,

                tenantProvisioned = account.tenantProvisioned,
                tenantProvisionError = account.tenantProvisionError,
                tenantProvisionedAtEpochMillis = account.tenantProvisionedAtEpochMillis,

                tenantId = account.tenantId,
                tenantSchema = account.tenantSchema,
                tenantSlug = account.tenantSlug,
                defaultDomain = account.defaultDomain,
                tenantStatus = account.tenantStatus,

                principalLoginUserId = account.principalLoginUserId,
                principalPin = account.principalPin,
                phoneNumber = account.phoneNumber,

            )
        )

        println("LOGIN DEBUG: Sending login response")
        println("LOGIN DEBUG: Response user ID = ${response.user.id}")
        println("LOGIN DEBUG: Response tenant code = ${response.user.tenantCode}")
        println("========== LOGIN REQUEST END: SUCCESS ==========")

        call.respond(
            HttpStatusCode.OK,
            response
        )
    }

    authenticate("auth-jwt") {
        post("/logout") {
            call.respond(
                HttpStatusCode.OK,
                mapOf("message" to "Logged out successfully")
            )
        }
    }
}