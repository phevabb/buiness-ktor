package sms.services



import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation

import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import sms.dto.MnotifySmsRequest

object SenderIdApprovalSmsNotifier {

    private val client =
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                        explicitNulls = false
                    }
                )
            }
        }

    private val scope =
        CoroutineScope(
            SupervisorJob() + Dispatchers.IO
        )

    private const val endpoint =
        "https://api.mnotify.com/api/sms/quick"

    private const val senderId =
        "phenasystem"

    private val apiKey: String
        get() =
            "CelTN4i2JFPI2ZpknqYl0azod"

    fun notifyApprovalAsync(
        phone: String,
        schoolName: String,
        approvedSenderId: String
    ) {

        val cleanedPhone =
            phone.trim()

        if (cleanedPhone.isBlank()) {

            println(
                "Sender ID approval SMS skipped: owner phone number is blank."
            )

            return
        }

        if (apiKey.isBlank()) {

            println(
                "Sender ID approval SMS skipped: mNotify API key is blank."
            )

            return
        }

        val cleanedSchoolName =
            schoolName.ifBlank {
                "Your school"
            }

        val message =
            "$cleanedSchoolName, your SMS sender ID '$approvedSenderId' has been approved. You can now send SMS from Phena SMS Center."

        scope.launch {

            try {

                val payload =
                    MnotifySmsRequest(
                        recipient = listOf(
                            cleanedPhone
                        ),
                        sender = senderId,
                        message = message
                    )

                val response =
                    client.post(
                        "$endpoint?key=$apiKey"
                    ) {
                        contentType(
                            ContentType.Application.Json
                        )

                        setBody(
                            payload
                        )
                    }

                val bodyText =
                    response.bodyAsText()

                println(
                    "Sender ID approval SMS status=${response.status} body=$bodyText"
                )

            } catch (e: Exception) {

                println(
                    "Failed to send sender ID approval SMS: ${e.message}"
                )

                e.printStackTrace()
            }
        }
    }

    fun close() {

        runCatching {
            client.close()
        }

        scope.cancel()
    }
}