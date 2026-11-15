package com.example.incidentreporter.data.remote.api

import com.example.incidentreporter.data.remote.dto.request.CreateIncidentRequest
import com.example.incidentreporter.data.remote.dto.request.UpdateIncidentRequest
import com.example.incidentreporter.data.remote.dto.request.UpdateIncidentStatusRequest
import com.example.incidentreporter.data.remote.dto.response.ApiResponse
import com.example.incidentreporter.data.remote.dto.response.DeleteIncidentResponse
import com.example.incidentreporter.data.remote.dto.response.IncidentDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface IncidentApiService {

    @GET("incidents")
    suspend fun getIncidents(
        @Query("status") status: String? = null,
    ): Response<ApiResponse<List<IncidentDto>>>

    @GET("incidents/{id}")
    suspend fun getIncident(
        @Path("id") id: String,
    ): Response<ApiResponse<IncidentDto>>

    @POST("incidents")
    suspend fun createIncident(
        @Body request: CreateIncidentRequest,
    ): Response<ApiResponse<IncidentDto>>

    @PATCH("incidents/{id}")
    suspend fun updateIncident(
        @Path("id") id: String,
        @Body request: UpdateIncidentRequest,
    ): Response<ApiResponse<IncidentDto>>

    @PATCH("incidents/{id}/status")
    suspend fun updateIncidentStatus(
        @Path("id") id: String,
        @Body request: UpdateIncidentStatusRequest,
    ): Response<ApiResponse<IncidentDto>>

    @DELETE("incidents/{id}")
    suspend fun deleteIncident(
        @Path("id") id: String,
    ): Response<DeleteIncidentResponse>
}
