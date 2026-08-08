package account.dto
import kotlinx.serialization.Serializable

@Serializable
data class SchoolProfileResponse(
    val schoolName: String,
    val schoolLogoUrl: String?,
    val schoolMotto: String?,
    val location: String
)