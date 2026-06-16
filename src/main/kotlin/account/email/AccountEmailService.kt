package com.example.account.email




import com.example.config.AppConfig
import jakarta.mail.Authenticator
import jakarta.mail.Message
import jakarta.mail.PasswordAuthentication
import jakarta.mail.Session
import jakarta.mail.Transport
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties

object AccountEmailService {

    suspend fun sendVerificationEmail(
        to: String,
        schoolName: String,
        verificationUrl: String
    ) = withContext(Dispatchers.IO) {

        println("Preparing verification email for: $to")
        println("SMTP host: ${AppConfig.smtpHost}")
        println("SMTP port: ${AppConfig.smtpPort}")
        println("SMTP SSL: ${AppConfig.emailUseSsl}")
        println("Verification URL: $verificationUrl")

        val properties = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.host", AppConfig.smtpHost)
            put("mail.smtp.port", AppConfig.smtpPort.toString())

            put("mail.smtp.connectiontimeout", "15000")
            put("mail.smtp.timeout", "15000")
            put("mail.smtp.writetimeout", "15000")

            if (AppConfig.emailUseSsl) {
                put("mail.smtp.ssl.enable", "true")
                put("mail.smtp.ssl.trust", AppConfig.smtpHost)
                put("mail.smtp.starttls.enable", "false")
            } else {
                put("mail.smtp.starttls.enable", "true")
                put("mail.smtp.starttls.required", "true")
                put("mail.smtp.ssl.enable", "false")
            }
        }

        val session = Session.getInstance(
            properties,
            object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(
                        AppConfig.smtpUsername,
                        AppConfig.smtpPassword
                    )
                }
            }
        )

        session.debug = AppConfig.emailDebug

        val html = buildVerificationEmailHtml(
            schoolName = schoolName,
            verificationUrl = verificationUrl
        )

        val message = MimeMessage(session).apply {
            setFrom(
                InternetAddress(
                    AppConfig.smtpFromEmail,
                    AppConfig.smtpFromName
                )
            )

            setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(to)
            )

            subject = "Verify your Phena school account"
            setContent(html, "text/html; charset=utf-8")
        }

        Transport.send(message)

        println("Verification email sent successfully to: $to")
    }

    private fun buildVerificationEmailHtml(
        schoolName: String,
        verificationUrl: String
    ): String {
        val safeSchoolName = escapeHtml(schoolName)
        val safeVerificationUrl = escapeHtml(verificationUrl)

        return """

<!doctype html>

<html>
<body style="margin:0;padding:0;background:#eef6ff;font-family:'Segoe UI',Arial,sans-serif;">

```
<div style="max-width:680px;margin:0 auto;padding:40px 20px;">

    <!-- Card -->
    <div style="
        background:linear-gradient(180deg,#ffffff 0%,#f8fbff 100%);
        border-radius:28px;
        overflow:hidden;
        box-shadow:0 20px 50px rgba(29,78,216,0.12);
        border:1px solid #dbeafe;
    ">

        <!-- Header -->
        <div style="
            background:linear-gradient(135deg,#2563eb 0%,#60a5fa 100%);
            padding:40px;
            text-align:center;
        ">

            <div style="
                width:70px;
                height:70px;
                margin:0 auto;
                border-radius:20px;
                background:rgba(255,255,255,0.18);
                color:#ffffff;
                font-size:34px;
                font-weight:800;
                line-height:70px;
                backdrop-filter:blur(10px);
            ">
                P
            </div>

            <h1 style="
                margin:24px 0 0;
                color:#ffffff;
                font-size:32px;
                font-weight:700;
                line-height:1.2;
            ">
                Verify Your Account
            </h1>

            <p style="
                margin:12px auto 0;
                color:rgba(255,255,255,0.9);
                font-size:16px;
                max-width:500px;
                line-height:1.7;
            ">
                Welcome to Phena School Management System
            </p>

        </div>

        <!-- Body -->
        <div style="padding:42px;">

            <p style="
                margin:0 0 18px;
                color:#334155;
                font-size:16px;
                line-height:1.8;
            ">
                Your school <strong style="color:#1e40af;">$safeSchoolName</strong>
                has successfully started registration on the Phena platform.
            </p>

            <p style="
                margin:0;
                color:#64748b;
                font-size:16px;
                line-height:1.8;
            ">
                Please verify your email address to activate your school account and gain full access to the platform.
            </p>

            <!-- CTA -->
            <div style="text-align:center;margin:40px 0;">
                <a href="$safeVerificationUrl"
                   style="
                       display:inline-block;
                       background:linear-gradient(135deg,#2563eb,#3b82f6);
                       color:#ffffff;
                       text-decoration:none;
                       padding:16px 38px;
                       border-radius:14px;
                       font-size:16px;
                       font-weight:600;
                       box-shadow:0 10px 25px rgba(37,99,235,0.25);
                   ">
                    Verify Email Address
                </a>
            </div>

            <!-- Link -->
            <div style="
                background:#eff6ff;
                border:1px solid #bfdbfe;
                border-radius:16px;
                padding:18px;
            ">
                <p style="
                    margin:0 0 10px;
                    color:#475569;
                    font-size:13px;
                    font-weight:600;
                ">
                    Button not working?
                </p>

                <p style="
                    margin:0;
                    word-break:break-all;
                    color:#2563eb;
                    font-size:13px;
                    line-height:1.6;
                ">
                    $safeVerificationUrl
                </p>
            </div>

            <!-- Footer -->
            <div style="
                margin-top:30px;
                padding-top:24px;
                border-top:1px solid #e2e8f0;
            ">
                <p style="
                    margin:0;
                    color:#94a3b8;
                    font-size:13px;
                    line-height:1.7;
                ">
                    This verification link expires in 24 hours for security purposes.
                </p>
            </div>

        </div>

    </div>

    <p style="
        text-align:center;
        margin-top:24px;
        color:#94a3b8;
        font-size:12px;
    ">
        © 2026 Phena School Management System
    </p>

</div>


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
}