package com.example.incidentreporter.testing

import com.example.incidentreporter.data.remote.api.IncidentApiService
import com.example.incidentreporter.data.remote.dto.request.CreateIncidentRequest
import com.example.incidentreporter.data.remote.dto.request.UpdateIncidentRequest
import com.example.incidentreporter.data.remote.dto.request.UpdateIncidentStatusRequest
import com.example.incidentreporter.data.remote.dto.response.ApiResponse
import com.example.incidentreporter.data.remote.dto.response.DeleteIncidentResponse
import com.example.incidentreporter.data.remote.dto.response.IncidentDto
import retrofit2.Response

class FakeIncidentApiService : IncidentApiService {

    var getIncidentsCalls = 0
    var getIncidentCalls = 0
    var createIncidentCalls = 0
    var updateIncidentCalls = 0
    var updateIncidentStatusCalls = 0
    var deleteIncidentCalls = 0

    var lastStatusQuery: String? = null
    var lastGetIncidentId: String? = null
    var lastCreateRequest: CreateIncidentRequest? = null
    var lastUpdateIncidentId: String? = null
    var lastUpdateRequest: UpdateIncidentRequest? = null
    var lastUpdateStatusId: String? = null
    var lastUpdateStatusRequest: UpdateIncidentStatusRequest? = null
    var lastDeleteIncidentId: String? = null

    var getIncidentsResponse: Response<ApiResponse<List<IncidentDto>>>? = null
    var getIncidentResponse: Response<ApiResponse<IncidentDto>>? = null
    var createIncidentResponse: Response<ApiResponse<IncidentDto>>? = null
    var updateIncidentResponse: Response<ApiResponse<IncidentDto>>? = null
    var updateIncidentStatusResponse: Response<ApiResponse<IncidentDto>>? = null
    var deleteIncidentResponse: Response<DeleteIncidentResponse>? = null

    var getIncidentsException: Throwable? = null
    var getIncidentException: Throwable? = null
    var createIncidentException: Throwable? = null
    var updateIncidentException: Throwable? = null
    var deleteIncidentException: Throwable? = null

    override suspend fun getIncidents(
        status: String?,
    ): Response<ApiResponse<List<IncidentDto>>> {
        getIncidentsCalls += 1
        lastStatusQuery = status
        getIncidentsException?.let { throw it }
        return requireResponse(getIncidentsResponse, "getIncidents")
    }

    override suspend fun getIncident(
        id: String,
    ): Response<ApiResponse<IncidentDto>> {
        getIncidentCalls += 1
        lastGetIncidentId = id
        getIncidentException?.let { throw it }
        return requireResponse(getIncidentResponse, "getIncident")
    }

    override suspend fun createIncident(
        request: CreateIncidentRequest,
    ): Response<ApiResponse<IncidentDto>> {
        createIncidentCalls += 1
        lastCreateRequest = request
        createIncidentException?.let { throw it }
        return requireResponse(createIncidentResponse, "createIncident")
    }

    override suspend fun updateIncident(
        id: String,
        request: UpdateIncidentRequest,
    ): Response<ApiResponse<IncidentDto>> {
        updateIncidentCalls += 1
        lastUpdateIncidentId = id
        lastUpdateRequest = request
        updateIncidentException?.let { throw it }
        return requireResponse(updateIncidentResponse, "updateIncident")
    }

    override suspend fun updateIncidentStatus(
        id: String,
        request: UpdateIncidentStatusRequest,
    ): Response<ApiResponse<IncidentDto>> {
        updateIncidentStatusCalls += 1
        lastUpdateStatusId = id
        lastUpdateStatusRequest = request
        return requireResponse(updateIncidentStatusResponse, "updateIncidentStatus")
    }

    override suspend fun deleteIncident(
        id: String,
    ): Response<DeleteIncidentResponse> {
        deleteIncidentCalls += 1
        lastDeleteIncidentId = id
        deleteIncidentException?.let { throw it }
        return requireResponse(deleteIncidentResponse, "deleteIncident")
    }

    private fun <T> requireResponse(
        response: Response<T>?,
        methodName: String,
    ): Response<T> {
        return response ?: error("No $methodName response configured")
    }
}
