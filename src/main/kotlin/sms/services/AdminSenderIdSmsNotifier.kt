package sms.services



import config.AppConfig
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

object AdminSenderIdSmsNotifier {

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


    val adminPhone = AppConfig.adminPhone


    val endpoint = AppConfig.mnotifyEndpoint
    val adminSenderId = AppConfig.adminSenderId
    val apiKey = AppConfig.mnotifyApiKey




    fun notifySenderIdRequestAsync(
        schoolName: String,
        tenantCode: String,
        requestedSenderId: String
    ) {

        if (apiKey.isBlank()) {

            println(
                "Admin sender ID SMS skipped: mNotify API key is not configured."
            )

            return
        }

        val cleanedSchoolName =
            schoolName.ifBlank {
                "Unknown School"
            }

        val message =
            "$cleanedSchoolName has requested a sender ID: $requestedSenderId. Review and approve. Tenant code: $tenantCode"

        scope.launch {

            try {

                val payload =
                    MnotifySmsRequest(
                        recipient = listOf(
                            adminPhone
                        ),
                        sender = adminSenderId,
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
                    "Admin sender ID request SMS status=${response.status} body=$bodyText"
                )

            } catch (e: Exception) {

                println(
                    "Failed to send admin sender ID request SMS: ${e.message}"
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