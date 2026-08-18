package sms.dto



import kotlinx.serialization.Serializable

@Serializable
data class MnotifySmsRequest(
    val recipient: List<String>,
    val sender: String,
    val message: String
)