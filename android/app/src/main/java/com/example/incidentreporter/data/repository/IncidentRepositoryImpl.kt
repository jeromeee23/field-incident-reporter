package com.example.incidentreporter.data.repository

import com.example.incidentreporter.data.remote.api.IncidentApiService
import com.example.incidentreporter.data.remote.api.PhotoApiService
import com.example.incidentreporter.data.remote.mapper.toDto
import com.example.incidentreporter.data.remote.mapper.toIncident
import com.example.incidentreporter.data.remote.mapper.toPhoto
import com.example.incidentreporter.domain.model.CreateIncidentRequest
import com.example.incidentreporter.domain.model.Incident
import com.example.incidentreporter.domain.model.IncidentStatus
import com.example.incidentreporter.domain.model.Photo
import com.example.incidentreporter.domain.model.UpdateIncidentRequest
import com.example.incidentreporter.domain.model.UpdateIncidentStatusRequest
import com.example.incidentreporter.domain.repository.IncidentRepository
import com.example.incidentreporter.network.safeApiCall
import com.example.incidentreporter.network.safeDirectCall
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

private const val MAX_PHOTOS = 5
private const val MAX_PHOTO_SIZE_BYTES = 5L * 1024 * 1024
private const val DELETE_SUCCESS_STATUS = "SUCCESS"
private val SUPPORTED_EXTENSIONS = setOf("jpg", "jpeg", "png", "webp")
private val TEXT_PLAIN_MEDIA_TYPE = "text/plain".toMediaType()

@Singleton
class IncidentRepositoryImpl @Inject constructor(
    private val incidentApiService: IncidentApiService,
    private val photoApiService: PhotoApiService,
) : IncidentRepository {

    override suspend fun getIncidents(
        statuses: List<IncidentStatus>,
    ): Result<List<Incident>> {
        val statusQuery = statuses
            .takeIf { it.isNotEmpty() }
            ?.joinToString(",") { it.name }
        return safeApiCall { incidentApiService.getIncidents(statusQuery) }
            .map { dtos -> dtos.map { it.toIncident() } }
    }

    override suspend fun getIncidentById(
        id: String,
    ): Result<Incident> {
        return safeApiCall { incidentApiService.getIncident(id) }
            .map { it.toIncident() }
    }

    override suspend fun createIncident(
        request: CreateIncidentRequest,
    ): Result<Incident> {
        return safeApiCall { incidentApiService.createIncident(request.toDto()) }
            .map { it.toIncident() }
    }

    override suspend fun updateIncident(
        id: String,
        request: UpdateIncidentRequest,
    ): Result<Incident> {
        return safeApiCall { incidentApiService.updateIncident(id, request.toDto()) }
            .map { it.toIncident() }
    }

    override suspend fun updateIncidentStatus(
        id: String,
        request: UpdateIncidentStatusRequest,
    ): Result<Incident> {
        return safeApiCall {
            incidentApiService.updateIncidentStatus(id, request.toDto())
        }.map { it.toIncident() }
    }

    override suspend fun deleteIncident(
        id: String,
    ): Result<Unit> {
        return safeDirectCall { incidentApiService.deleteIncident(id) }
            .mapCatching { response ->
                check(response.status == DELETE_SUCCESS_STATUS) { response.message }
            }
    }

    override suspend fun uploadPhotos(
        incidentId: String,
        photos: List<File>,
    ): Result<List<Photo>> {
        validatePhotos(photos).getOrElse { return Result.failure(it) }

        val incidentIdBody = incidentId.toRequestBody(TEXT_PLAIN_MEDIA_TYPE)
        val photoParts = photos.map { it.toPhotoPart() }

        return safeApiCall {
            photoApiService.uploadPhotos(incidentIdBody, photoParts)
        }.map { dtos -> dtos.map { it.toPhoto() } }
    }

    override suspend fun deletePhoto(
        photoId: String,
    ): Result<Unit> {
        return safeDirectCall { photoApiService.deletePhoto(photoId) }
            .mapCatching { response ->
                check(response.status == DELETE_SUCCESS_STATUS) { response.message }
            }
    }

    private fun validatePhotos(photos: List<File>): Result<Unit> {
        if (photos.size > MAX_PHOTOS) {
            return Result.failure(
                IllegalArgumentException("A maximum of $MAX_PHOTOS photos can be uploaded."),
            )
        }
        photos.forEach { file ->
            val extension = file.extension.lowercase()
            if (extension !in SUPPORTED_EXTENSIONS) {
                return Result.failure(
                    IllegalArgumentException("Unsupported photo format: ${file.name}"),
                )
            }
            if (file.length() > MAX_PHOTO_SIZE_BYTES) {
                return Result.failure(
                    IllegalArgumentException("Photo exceeds the 5 MB size limit: ${file.name}"),
                )
            }
        }
        return Result.success(Unit)
    }

    private fun File.toPhotoPart(): MultipartBody.Part {
        val mediaType = mimeTypeForExtension(extension).toMediaType()
        return MultipartBody.Part.createFormData(
            name = "photos",
            filename = name,
            body = asRequestBody(mediaType),
        )
    }

    private fun mimeTypeForExtension(extension: String): String {
        return when (extension.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "webp" -> "image/webp"
            else -> error("Unsupported photo format: $extension")
        }
    }
}
