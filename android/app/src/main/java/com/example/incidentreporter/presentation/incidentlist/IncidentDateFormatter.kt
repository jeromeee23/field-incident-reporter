package com.example.incidentreporter.presentation.incidentlist

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val isoPatterns = listOf(
    "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
    "yyyy-MM-dd'T'HH:mm:ssXXX",
    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
    "yyyy-MM-dd'T'HH:mm:ss'Z'",
)

internal fun formatIncidentCreatedAt(createdAt: String): String {
    val parsed = parseIsoDate(createdAt) ?: return createdAt
    val formatter = SimpleDateFormat("MMM d, yyyy · hh:mm a", Locale.US)
    return formatter.format(parsed)
}

private fun parseIsoDate(value: String): Date? {
    isoPatterns.forEach { pattern ->
        val format = SimpleDateFormat(pattern, Locale.US)
        if (pattern.endsWith("'Z'")) {
            format.timeZone = TimeZone.getTimeZone("UTC")
        }
        try {
            format.parse(value)?.let { return it }
        } catch (_: ParseException) {
            // Try the next ISO pattern.
        }
    }
    return null
}
