package com.example.incidentreporter.network

class ApiException(
    val httpStatusCode: Int,
    val errorCode: String,
    val errorMessage: String,
) : Exception(errorMessage)
