package org.discoursey.android.ui.components

import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {

    private val isoFormats = listOf(
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") },
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") },
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US),
        SimpleDateFormat("yyyy-MM-dd", Locale.US)
    )

    fun formatDiscourseTime(rawDate: String?): String {
        if (rawDate.isNullOrBlank()) return ""

        var parsedDate: Date? = null
        for (format in isoFormats) {
            try {
                parsedDate = format.parse(rawDate)
                if (parsedDate != null) break
            } catch (_: Exception) { }
        }

        if (parsedDate == null) {
            // Already a relative or custom string
            return rawDate
        }

        val now = System.currentTimeMillis()
        val diffMillis = now - parsedDate.time

        // Future or less than 1 min
        if (diffMillis < 60_000L) {
            return "just now"
        }

        val diffMinutes = diffMillis / (60_000L)
        val diffHours = diffMillis / (3600_000L)

        return when {
            // Past 1 hour: 1 min ago, 38 min ago, etc.
            diffMinutes < 60 -> "$diffMinutes min ago"
            // From 1 hour to 23 hours ago: 1 hour ago, 5 hours ago, etc.
            diffHours < 24 -> if (diffHours == 1L) "1 hour ago" else "$diffHours hours ago"
            // More than 24 hours: "Date + month HH:MIN" (e.g. 17 Jul 14:30)
            else -> {
                val outputFormat = SimpleDateFormat("d MMM HH:mm", Locale.getDefault())
                outputFormat.format(parsedDate)
            }
        }
    }
}
