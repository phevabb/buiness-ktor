package com.example.account.service



import config.AppConfig
import jakarta.mail.Authenticator
import jakarta.mail.Message
import jakarta.mail.PasswordAuthentication
import jakarta.mail.Session
import jakarta.mail.Transport
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import java.util.Properties

object AccountEmailService {

    fun sendVerificationEmail(
        to: String,
        schoolName: String,
        verificationUrl: String
    ) {
        val properties = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.host", AppConfig.smtpHost)
            put("mail.smtp.port", AppConfig.smtpPort.toString())
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
    }

    private fun buildVerificationEmailHtml(
        schoolName: String,
        verificationUrl: String
    ): String {
        return """
            <!doctype html>
            <html>
            <body style="margin:0;padding:0;background:#f4f7fb;font-family:Arial,sans-serif;">
                <div style="max-width:640px;margin:0 auto;padding:32px;">
                    <div style="background:#071926;color:#ffffff;border-radius:22px;padding:32px;">
                        <h1 style="margin:0 0 12px;font-size:28px;">
                            Verify your Phena account
                        </h1>

                        <p style="color:#d7e3f4;font-size:16px;line-height:1.7;">
                            Your school <strong>$schoolName</strong> has started registration on Phena.
                        </p>

                        <p style="color:#d7e3f4;font-size:16px;line-height:1.7;">
                            Click the button below to verify your email address.
                        </p>

                        <p style="margin:32px 0;">
                            <a href="$verificationUrl"
                               style="background:#f5d58c;color:#071926;padding:14px 22px;border-radius:14px;text-decoration:none;font-weight:800;">
                                Verify Email
                            </a>
                        </p>

                        <p style="color:#aab8c8;font-size:13px;">
                            If the button does not work, copy this link:
                        </p>

                        <p style="word-break:break-all;color:#f5d58c;font-size:13px;">
                            $verificationUrl
                        </p>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
    }
}