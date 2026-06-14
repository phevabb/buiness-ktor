package com.example.account.email



object AccountEmailService {

    fun sendVerificationEmail(
        to: String,
        schoolName: String,
        verificationUrl: String
    ) {
        /*
         * Keep this simple for now.
         * Later, connect this to SMTP, Resend, SendGrid, Brevo, or Mailgun.
         *
         * For development, this prints the verification link.
         */
        println("Sending verification email to: $to")
        println("School: $schoolName")
        println("Verification URL: $verificationUrl")
    }
}