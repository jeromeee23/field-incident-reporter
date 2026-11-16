package com.example.incidentreporter.network

import com.example.incidentreporter.data.remote.dto.response.ApiResponse
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

class ApiResponseHandlerTest {

    @Test
    fun safeDirectCall_successReturnsBody() = runTest {
        val body = ApiResponse(data = "ok")

        val result = safeDirectCall { Response.success(body) }

        assertEquals(Result.success(body), result)
    }

    @Test
    fun safeApiCall_successUnwrapsData() = runTest {
        val result = safeApiCall { Response.success(ApiResponse(data = "ok")) }

        assertEquals(Result.success("ok"), result)
    }

    @Test
    fun safeDirectCall_emptySuccessBodyReturnsApiException() = runTest {
        val result = safeDirectCall<String> { Response.success(null) }

        val error = result.exceptionOrNull() as ApiException
        assertEquals(200, error.httpStatusCode)
        assertEquals("EMPTY_RESPONSE", error.errorCode)
        assertEquals("Response body was empty.", error.errorMessage)
    }

    @Test
    fun safeDirectCall_httpErrorParsesApiErrorBody() = runTest {
        val result = safeDirectCall<String> {
            errorResponse(
                httpCode = 404,
                body = """{"error":{"code":"INCIDENT_NOT_FOUND","message":"Incident not found"}}""",
            )
        }

        val error = result.exceptionOrNull() as ApiException
        assertEquals(404, error.httpStatusCode)
        assertEquals("INCIDENT_NOT_FOUND", error.errorCode)
        assertEquals("Incident not found", error.errorMessage)
    }

    @Test
    fun safeDirectCall_malformedErrorBodyUsesUnknownError() = runTest {
        val result = safeDirectCall<String> {
            errorResponse(httpCode = 500, body = "not-json")
        }

        val error = result.exceptionOrNull() as ApiException
        assertEquals(500, error.httpStatusCode)
        assertEquals("UNKNOWN_ERROR", error.errorCode)
        assertEquals("An unexpected error occurred.", error.errorMessage)
    }

    @Test
    fun safeDirectCall_ioExceptionIsReturnedAsFailure() = runTest {
        val ioException = IOException("Failed to connect")

        val result = safeDirectCall<String> { throw ioException }

        assertEquals(ioException, result.exceptionOrNull())
    }

    @Test
    fun safeDirectCall_jsonSyntaxExceptionIsReturnedAsFailure() = runTest {
        val parseError = JsonSyntaxException("Invalid JSON")

        val result = safeDirectCall<String> { throw parseError }

        assertEquals(parseError, result.exceptionOrNull())
    }

    @Test
    fun safeDirectCall_rethrowsCancellationException() = runTest {
        var thrown: Throwable? = null

        try {
            safeDirectCall<String> { throw CancellationException("cancelled") }
        } catch (error: CancellationException) {
            thrown = error
        }

        assertTrue(thrown is CancellationException)
        assertEquals("cancelled", thrown?.message)
    }

    private fun errorResponse(
        httpCode: Int,
        body: String,
    ): Response<String> {
        return Response.error(
            httpCode,
            body.toResponseBody("application/json".toMediaType()),
        )
    }
}
