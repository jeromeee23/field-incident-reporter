package com.example.incidentreporter.presentation.incidentlist

import com.example.incidentreporter.network.ApiException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.coroutines.cancellation.CancellationException

class IncidentErrorMappingTest {

    @Test
    fun toIncidentLoadError_mapsNetworkFailures() {
        assertEquals(IncidentLoadError.Network, UnknownHostException().toIncidentLoadError())
        assertEquals(IncidentLoadError.Network, ConnectException().toIncidentLoadError())
    }

    @Test
    fun toIncidentLoadError_mapsTransportFailures() {
        assertEquals(
            IncidentLoadError.Transport(timedOut = true),
            SocketTimeoutException().toIncidentLoadError(),
        )
        assertEquals(
            IncidentLoadError.Transport(timedOut = false),
            IOException("Broken pipe").toIncidentLoadError(),
        )
    }

    @Test
    fun toIncidentLoadError_mapsApiExceptionMessage() {
        val error = ApiException(
            httpStatusCode = 404,
            errorCode = "INCIDENT_NOT_FOUND",
            errorMessage = "Incident not found",
        )

        assertEquals(IncidentLoadError.Api("Incident not found"), error.toIncidentLoadError())
    }

    @Test
    fun toIncidentLoadError_blankApiMessageBecomesEmptyApiError() {
        val error = ApiException(
            httpStatusCode = 500,
            errorCode = "UNKNOWN_ERROR",
            errorMessage = "   ",
        )

        assertEquals(IncidentLoadError.Api(""), error.toIncidentLoadError())
    }

    @Test
    fun toIncidentLoadError_unexpectedFailures() {
        assertEquals(
            IncidentLoadError.Unexpected,
            IllegalStateException("Network down").toIncidentLoadError(),
        )
    }

    @Test
    fun toIncidentLoadError_rethrowsCancellationException() {
        var thrown: Throwable? = null

        try {
            CancellationException("cancelled").toIncidentLoadError()
        } catch (error: CancellationException) {
            thrown = error
        }

        assertTrue(thrown is CancellationException)
        assertEquals("cancelled", thrown?.message)
    }

    @Test
    fun toIncidentErrorMessage_usesThrowableMessage() {
        assertEquals(
            "Update failed",
            IllegalStateException("Update failed").toIncidentErrorMessage(),
        )
    }

    @Test
    fun toIncidentErrorMessage_blankMessageUsesFallback() {
        assertEquals(
            "An unexpected error occurred.",
            IllegalStateException("  ").toIncidentErrorMessage(),
        )
        assertEquals(
            "An unexpected error occurred.",
            RuntimeException().toIncidentErrorMessage(),
        )
    }
}
