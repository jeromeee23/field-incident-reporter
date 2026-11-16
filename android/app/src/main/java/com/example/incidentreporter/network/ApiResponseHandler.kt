package com.example.incidentreporter.network

import com.example.incidentreporter.data.remote.dto.response.ApiErrorResponse
import com.example.incidentreporter.data.remote.dto.response.ApiResponse
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.JsonSyntaxException
import retrofit2.Response
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

private const val EMPTY_RESPONSE_ERROR_CODE = "EMPTY_RESPONSE"
private const val EMPTY_RESPONSE_ERROR_MESSAGE = "Response body was empty."
private const val UNKNOWN_ERROR_CODE = "UNKNOWN_ERROR"
private const val UNKNOWN_ERROR_MESSAGE = "An unexpected error occurred."

private val errorBodyGson = Gson()

suspend fun <T> safeApiCall(
    call: suspend () -> Response<ApiResponse<T>>,
): Result<T> {
    return safeDirectCall(call).map { apiResponse -> apiResponse.data }
}

suspend fun <T> safeDirectCall(
    call: suspend () -> Response<T>,
): Result<T> {
    return try {
        val response = call()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                Result.success(body)
            } else {
                Result.failure(
                    ApiException(
                        httpStatusCode = response.code(),
                        errorCode = EMPTY_RESPONSE_ERROR_CODE,
                        errorMessage = EMPTY_RESPONSE_ERROR_MESSAGE,
                    ),
                )
            }
        } else {
            Result.failure(toApiException(response))
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: IOException) {
        Result.failure(e)
    } catch (e: JsonSyntaxException) {
        Result.failure(e)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

private fun <T> toApiException(response: Response<T>): ApiException {
    val errorBody = response.errorBody()?.string()
    val apiError = if (errorBody.isNullOrBlank()) {
        null
    } else {
        try {
            errorBodyGson.fromJson(errorBody, ApiErrorResponse::class.java)?.error
        } catch (_: JsonParseException) {
            null
        }
    }
    return ApiException(
        httpStatusCode = response.code(),
        errorCode = apiError?.code ?: UNKNOWN_ERROR_CODE,
        errorMessage = apiError?.message ?: UNKNOWN_ERROR_MESSAGE,
    )
}
