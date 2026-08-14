package account.dto

import kotlinx.serialization.Serializable




@Serializable
data class SchoolLogoUploadResponse(
    val schoolLogoUrl: String,
    val schoolLogoPublicId: String
)
