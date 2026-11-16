package com.example.incidentreporter.presentation.incidentlist

import com.example.incidentreporter.network.ApiException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.coroutines.cancellation.CancellationException

sealed interface IncidentLoadError {
    data object Network : IncidentLoadError
    data class Transport(val timedOut: Boolean) : IncidentLoadError
    data class Api(val message: String) : IncidentLoadError
    data object Unexpected : IncidentLoadError
}

internal fun Throwable.toIncidentErrorMessage(): String {
    return message?.takeIf { it.isNotBlank() } ?: "An unexpected error occurred."
}

internal fun Throwable.toIncidentLoadError(): IncidentLoadError {
    if (this is CancellationException) throw this
    return when (this) {
        is UnknownHostException,
        is ConnectException,
        -> IncidentLoadError.Network
        is SocketTimeoutException -> IncidentLoadError.Transport(timedOut = true)
        is IOException -> IncidentLoadError.Transport(timedOut = false)
        is ApiException -> IncidentLoadError.Api(
            errorMessage.takeIf { it.isNotBlank() }.orEmpty(),
        )
        else -> IncidentLoadError.Unexpected
    }
}
