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

    var getIncidentsCalls = 0
    var getIncidentCalls = 0
    var createIncidentCalls = 0
    var updateIncidentCalls = 0
    var updateStatusCalls = 0
    var deleteCalls = 0
    var uploadPhotosCalls = 0
    var deletePhotoCalls = 0

    var getIncidentsResult: Result<List<Incident>>? = null
    var getIncidentResult: Result<Incident>? = null
    var createIncidentResult: Result<Incident>? = null
    var updateIncidentResult: Result<Incident>? = null
    var updateStatusResult: Result<Incident>? = null
    var deleteResult: Result<Unit>? = null
    var uploadPhotosResult: Result<List<Photo>>? = null
    var deletePhotoResult: Result<Unit>? = null

    var getIncidentsException: Throwable? = null
    var getIncidentException: Throwable? = null
    var createIncidentException: Throwable? = null
    var updateIncidentException: Throwable? = null
    var updateStatusException: Throwable? = null
    var deleteException: Throwable? = null
    var uploadPhotosException: Throwable? = null
    var deletePhotoException: Throwable? = null

    var getIncidentsGate: CompletableDeferred<Unit>? = null
    var getIncidentGate: CompletableDeferred<Unit>? = null
    var createIncidentGate: CompletableDeferred<Unit>? = null
    var updateIncidentGate: CompletableDeferred<Unit>? = null
    var updateStatusGate: CompletableDeferred<Unit>? = null
    var uploadPhotosGate: CompletableDeferred<Unit>? = null

    var lastGetIncidentsStatuses: List<IncidentStatus>? = null
    var lastCreateIncidentRequest: CreateIncidentRequest? = null
    var lastUpdateIncidentId: String? = null
    var lastUpdateIncidentRequest: UpdateIncidentRequest? = null
    var lastUpdateStatusRequest: UpdateIncidentStatusRequest? = null
    var lastUploadPhotosIncidentId: String? = null
    var lastUploadPhotos: List<File>? = null
    var lastDeletedPhotoId: String? = null

    override suspend fun getIncidents(
        statuses: List<IncidentStatus>,
    ): Result<List<Incident>> {
        getIncidentsCalls += 1
        lastGetIncidentsStatuses = statuses
        getIncidentsGate?.await()
        getIncidentsException?.let { throw it }
        getIncidentsResult?.let { return it }
        val values = incidents.values.toList()
        val filtered = if (statuses.isEmpty()) {
            values
        } else {
            values.filter { incident -> incident.status in statuses }
        }
        return Result.success(filtered)
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
        createIncidentCalls += 1
        lastCreateIncidentRequest = request
        createIncidentGate?.await()
        createIncidentException?.let { throw it }
        createIncidentResult?.let { return it }
        val created = Incident(
            id = request.id,
            incidentNumber = "INC-${request.id.takeLast(5)}",
            title = request.title,
            description = request.description,
            category = request.category,
            otherCategoryDescription = request.otherCategoryDescription,
            location = request.location,
            photos = request.photos,
            status = IncidentStatus.OPEN,
            createdAt = MUTATION_TIMESTAMP,
            updatedAt = MUTATION_TIMESTAMP,
            completedAt = null,
            cancelledAt = null,
        )
        incidents[created.id] = created
        return Result.success(created)
    }

    override suspend fun updateIncident(
        id: String,
        request: UpdateIncidentRequest,
    ): Result<Incident> {
        updateIncidentCalls += 1
        lastUpdateIncidentId = id
        lastUpdateIncidentRequest = request
        updateIncidentGate?.await()
        updateIncidentException?.let { throw it }
        updateIncidentResult?.let { return it }
        val current = incidents[id]
            ?: return Result.failure(IllegalStateException("Incident not found"))
        val updated = current.copy(
            title = request.title,
            description = request.description,
            category = request.category,
            otherCategoryDescription = request.otherCategoryDescription,
            location = request.location,
            photos = request.photos,
            updatedAt = MUTATION_TIMESTAMP,
        )
        incidents[id] = updated
        return Result.success(updated)
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
            updatedAt = MUTATION_TIMESTAMP,
            completedAt = if (request.status == IncidentStatus.COMPLETED) {
                MUTATION_TIMESTAMP
            } else {
                current.completedAt
            },
            cancelledAt = if (request.status == IncidentStatus.CANCELLED) {
                MUTATION_TIMESTAMP
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
        uploadPhotosCalls += 1
        lastUploadPhotosIncidentId = incidentId
        lastUploadPhotos = photos
        uploadPhotosGate?.await()
        uploadPhotosException?.let { throw it }
        uploadPhotosResult?.let { return it }
        val uploaded = photos.mapIndexed { index, file ->
            Photo(
                id = "photo-${index + 1}",
                url = "https://example.test/${file.name}",
            )
        }
        return Result.success(uploaded)
    }

    override suspend fun deletePhoto(photoId: String): Result<Unit> {
        deletePhotoCalls += 1
        lastDeletedPhotoId = photoId
        deletePhotoException?.let { throw it }
        deletePhotoResult?.let { return it }
        incidents.replaceAll { _, incident ->
            incident.copy(photos = incident.photos.filterNot { photo -> photo.id == photoId })
        }
        return Result.success(Unit)
    }

    private companion object {
        const val MUTATION_TIMESTAMP = "2026-11-12T14:18:00Z"
    }
}
