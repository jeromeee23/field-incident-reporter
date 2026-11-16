package com.example.incidentreporter.testing

import com.example.incidentreporter.domain.model.CreateIncidentRequest
import com.example.incidentreporter.domain.model.Incident
import com.example.incidentreporter.domain.model.IncidentStatus
import com.example.incidentreporter.domain.model.Photo
import com.example.incidentreporter.domain.model.UpdateIncidentRequest
import com.example.incidentreporter.domain.model.UpdateIncidentStatusRequest
import com.example.incidentreporter.domain.repository.IncidentRepository
import kotlinx.coroutines.CompletableDeferred
import java.io.File

class FakeIncidentRepository : IncidentRepository {

    val incidents = mutableMapOf<String, Incident>()

    var getIncidentCalls = 0
    var updateStatusCalls = 0
    var deleteCalls = 0

    var getIncidentResult: Result<Incident>? = null
    var updateStatusResult: Result<Incident>? = null
    var deleteResult: Result<Unit>? = null

    var getIncidentException: Throwable? = null
    var updateStatusException: Throwable? = null
    var deleteException: Throwable? = null

    var getIncidentGate: CompletableDeferred<Unit>? = null
    var updateStatusGate: CompletableDeferred<Unit>? = null

    var lastUpdateStatusRequest: UpdateIncidentStatusRequest? = null

    override suspend fun getIncidents(
        statuses: List<IncidentStatus>,
    ): Result<List<Incident>> {
        throw UnsupportedOperationException()
    }

    override suspend fun getIncidentById(id: String): Result<Incident> {
        getIncidentCalls += 1
        getIncidentGate?.await()
        getIncidentException?.let { throw it }
        getIncidentResult?.let { return it }
        return incidents[id]?.let { Result.success(it) }
            ?: Result.failure(IllegalStateException("Incident not found"))
    }

    override suspend fun createIncident(
        request: CreateIncidentRequest,
    ): Result<Incident> {
        throw UnsupportedOperationException()
    }

    override suspend fun updateIncident(
        id: String,
        request: UpdateIncidentRequest,
    ): Result<Incident> {
        throw UnsupportedOperationException()
    }

    override suspend fun updateIncidentStatus(
        id: String,
        request: UpdateIncidentStatusRequest,
    ): Result<Incident> {
        updateStatusCalls += 1
        lastUpdateStatusRequest = request
        updateStatusGate?.await()
        updateStatusException?.let { throw it }
        updateStatusResult?.let { return it }
        val current = incidents[id]
            ?: return Result.failure(IllegalStateException("Incident not found"))
        val updated = current.copy(
            status = request.status,
            updatedAt = "2026-11-12T14:18:00Z",
            completedAt = if (request.status == IncidentStatus.COMPLETED) {
                "2026-11-12T14:18:00Z"
            } else {
                current.completedAt
            },
            cancelledAt = if (request.status == IncidentStatus.CANCELLED) {
                "2026-11-12T14:18:00Z"
            } else {
                current.cancelledAt
            },
        )
        incidents[id] = updated
        return Result.success(updated)
    }

    override suspend fun deleteIncident(id: String): Result<Unit> {
        deleteCalls += 1
        deleteException?.let { throw it }
        deleteResult?.let { return it }
        incidents.remove(id)
        return Result.success(Unit)
    }

    override suspend fun uploadPhotos(
        incidentId: String,
        photos: List<File>,
    ): Result<List<Photo>> {
        throw UnsupportedOperationException()
    }

    override suspend fun deletePhoto(photoId: String): Result<Unit> {
        throw UnsupportedOperationException()
    }
}
