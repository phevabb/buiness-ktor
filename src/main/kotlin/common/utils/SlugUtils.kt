package com.example.common.utils



import java.util.UUID

object SlugUtils {
    fun generateTenantCode(schoolName: String): String {
        val base = schoolName
            .lowercase()
            .replace(Regex("[^a-z0-9]"), "")

        return "$base-${UUID.randomUUID().toString().replace("-", "").take(4)}"
    }
}