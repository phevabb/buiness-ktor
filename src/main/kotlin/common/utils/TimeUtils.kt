package com.example.common.utils



import java.time.Year

object TimeUtils {
    fun currentAcademicYear(): String {
        val now = Year.now().value
        return "$now/${now + 1}"
    }
}