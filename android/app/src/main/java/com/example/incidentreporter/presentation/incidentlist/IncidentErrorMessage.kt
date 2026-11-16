package com.example.incidentreporter.presentation.incidentlist

internal fun Throwable.toIncidentErrorMessage(): String {
    return message?.takeIf { it.isNotBlank() } ?: "An unexpected error occurred."
}
