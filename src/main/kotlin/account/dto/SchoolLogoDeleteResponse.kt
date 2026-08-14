package account.dto

import kotlinx.serialization.Serializable

@Serializable
data class SchoolLogoDeleteResponse(

    val deleted: Boolean,

    val deletedFromCloudinary: Boolean,

    val oldPublicId: String?,

    val message: String
)