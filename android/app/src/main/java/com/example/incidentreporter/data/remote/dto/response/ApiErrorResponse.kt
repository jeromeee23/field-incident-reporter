package com.example.incidentreporter.data.remote.dto.response

data class ApiErrorResponse(
    val error: ApiError,
)

data class ApiError(
    val code: String,
    val message: String,
)
