package com.example.incidentreporter.data.repository

import com.example.incidentreporter.data.remote.dto.response.ApiResponse
import com.example.incidentreporter.data.remote.dto.response.DeleteIncidentResponse
import com.example.incidentreporter.data.remote.dto.response.DeletePhotoResponse
import com.example.incidentreporter.data.remote.dto.response.IncidentDto
import com.example.incidentreporter.data.remote.dto.response.UploadPhotosDataDto
import com.example.incidentreporter.domain.model.CreateIncidentRequest
import com.example.incidentreporter.domain.model.Incident
import com.example.incidentreporter.domain.model.IncidentCategory
import com.example.incidentreporter.domain.model.IncidentStatus
import com.example.incidentreporter.domain.model.Photo
import com.example.incidentreporter.domain.model.UpdateIncidentRequest
import com.example.incidentreporter.network.ApiException
import com.example.incidentreporter.testing.FakeIncidentApiService
import com.example.incidentreporter.testing.FakePhotoApiService
import com.example.incidentreporter.testing.IncidentDtoFixtures
import com.example.incidentreporter.testing.IncidentFixtures
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.io.File
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

class IncidentRepositoryImplTest {

    private val incidentApi = FakeIncidentApiService()
    private val photoApi = FakePhotoApiService()
    private val repository = IncidentRepositoryImpl(incidentApi, photoApi)

    @Test
    fun getIncidents_emptyStatusListSendsNullQuery() = runTest {
        incidentApi.getIncidentsResponse = Response.success(ApiResponse(emptyList()))

        val result = repository.getIncidents(emptyList())

        assertEquals(1, incidentApi.getIncidentsCalls)
        assertEquals(null, incidentApi.lastStatusQuery)
        assertEquals(Result.success(emptyList<Incident>()), result)
    }

    @Test
    fun getIncidents_joinsStatusesAndMapsDomainModels() = runTest {
        val dto = IncidentDtoFixtures.incidentDto()
        incidentApi.getIncidentsResponse = Response.success(ApiResponse(listOf(dto)))

        val result = repository.getIncidents(
            listOf(IncidentStatus.OPEN, IncidentStatus.IN_PROGRESS),
        )

        assertEquals("OPEN,IN_PROGRESS", incidentApi.lastStatusQuery)
        assertEquals(Result.success(listOf(IncidentFixtures.incident())), result)
    }

    @Test
    fun getIncidentById_successMapsDomainModel() = runTest {
        val dto = IncidentDtoFixtures.incidentDto(
            photos = listOf(IncidentDtoFixtures.photoDto()),
        )
        incidentApi.getIncidentResponse = Response.success(ApiResponse(dto))

        val result = repository.getIncidentById("incident-1")

        assertEquals("incident-1", incidentApi.lastGetIncidentId)
        assertEquals(
            Result.success(
                IncidentFixtures.incident(
                    photos = listOf(Photo(id = "photo-1", url = "https://example.test/photo-1.jpg")),
                ),
            ),
            result,
        )
    }

    @Test
    fun createIncident_successMapsDomainModel() = runTest {
        val request = CreateIncidentRequest(
            id = "incident-1",
            title = "Broken Air Conditioning Unit",
            description = "Water leaking from the indoor unit near the reception area.",
            category = IncidentCategory.EQUIPMENT_DAMAGE,
            otherCategoryDescription = null,
            location = "Building A - Reception",
            photos = emptyList(),
        )
        incidentApi.createIncidentResponse = Response.success(
            ApiResponse(IncidentDtoFixtures.incidentDto()),
        )

        val result = repository.createIncident(request)

        assertEquals("incident-1", incidentApi.lastCreateRequest?.id)
        assertEquals("EQUIPMENT_DAMAGE", incidentApi.lastCreateRequest?.category)
        assertEquals(Result.success(IncidentFixtures.incident()), result)
    }

    @Test
    fun updateIncident_successMapsDomainModel() = runTest {
        val request = UpdateIncidentRequest(
            title = "Updated title",
            description = "Updated description",
            category = IncidentCategory.SAFETY_ISSUE,
            otherCategoryDescription = null,
            location = "Building B",
            photos = emptyList(),
        )
        incidentApi.updateIncidentResponse = Response.success(
            ApiResponse(
                IncidentDtoFixtures.incidentDto(
                    title = "Updated title",
                    description = "Updated description",
                    category = "SAFETY_ISSUE",
                    location = "Building B",
                ),
            ),
        )

        val result = repository.updateIncident("incident-1", request)

        assertEquals("incident-1", incidentApi.lastUpdateIncidentId)
        assertEquals("SAFETY_ISSUE", incidentApi.lastUpdateRequest?.category)
        assertEquals("Updated title", result.getOrThrow().title)
        assertEquals(IncidentCategory.SAFETY_ISSUE, result.getOrThrow().category)
    }

    @Test
    fun getIncidentById_httpErrorIsFailureNotSuccess() = runTest {
        incidentApi.getIncidentResponse = errorResponse(
            httpCode = 404,
            body = """{"error":{"code":"INCIDENT_NOT_FOUND","message":"Incident not found"}}""",
        )

        val result = repository.getIncidentById("missing-id")

        val error = result.exceptionOrNull() as ApiException
        assertTrue(result.isFailure)
        assertEquals(404, error.httpStatusCode)
        assertEquals("INCIDENT_NOT_FOUND", error.errorCode)
        assertEquals("Incident not found", error.errorMessage)
    }

    @Test
    fun getIncidents_ioExceptionIsFailure() = runTest {
        val ioException = IOException("Failed to connect")
        incidentApi.getIncidentsException = ioException

        val result = repository.getIncidents(listOf(IncidentStatus.OPEN))

        assertEquals(ioException, result.exceptionOrNull())
    }

    @Test
    fun getIncidentById_unknownStatusIsMappingFailure() = runTest {
        incidentApi.getIncidentResponse = Response.success(
            ApiResponse(IncidentDtoFixtures.incidentDto(status = "CLOSED")),
        )

        val result = repository.getIncidentById("incident-1")

        assertTrue(result.isFailure)
        assertEquals(
            "Unknown incident status: CLOSED",
            result.exceptionOrNull()?.message,
        )
    }

    @Test
    fun getIncidentById_rethrowsCancellationException() = runTest {
        incidentApi.getIncidentException = CancellationException("cancelled")
        var thrown: Throwable? = null

        try {
            repository.getIncidentById("incident-1")
        } catch (error: CancellationException) {
            thrown = error
        }

        assertTrue(thrown is CancellationException)
        assertEquals("cancelled", thrown?.message)
    }

    @Test
    fun deleteIncident_successStatusReturnsUnit() = runTest {
        incidentApi.deleteIncidentResponse = Response.success(
            DeleteIncidentResponse(status = "SUCCESS", message = "Deleted"),
        )

        val result = repository.deleteIncident("incident-1")

        assertEquals("incident-1", incidentApi.lastDeleteIncidentId)
        assertEquals(Result.success(Unit), result)
    }

    @Test
    fun deleteIncident_nonSuccessStatusIsFailure() = runTest {
        incidentApi.deleteIncidentResponse = Response.success(
            DeleteIncidentResponse(status = "FAILED", message = "Could not delete incident"),
        )

        val result = repository.deleteIncident("incident-1")

        assertTrue(result.isFailure)
        assertEquals("Could not delete incident", result.exceptionOrNull()?.message)
    }

    @Test
    fun deletePhoto_successStatusReturnsUnit() = runTest {
        photoApi.deletePhotoResponse = Response.success(
            DeletePhotoResponse(status = "SUCCESS", message = "Deleted", photoId = "photo-1"),
        )

        val result = repository.deletePhoto("photo-1")

        assertEquals("photo-1", photoApi.lastDeletePhotoId)
        assertEquals(Result.success(Unit), result)
    }

    @Test
    fun deletePhoto_nonSuccessStatusIsFailure() = runTest {
        photoApi.deletePhotoResponse = Response.success(
            DeletePhotoResponse(
                status = "FAILED",
                message = "Could not delete photo",
                photoId = "photo-1",
            ),
        )

        val result = repository.deletePhoto("photo-1")

        assertTrue(result.isFailure)
        assertEquals("Could not delete photo", result.exceptionOrNull()?.message)
    }

    @Test
    fun uploadPhotos_moreThanFiveFailsWithoutCallingApi() = runTest {
        val photos = List(6) { index -> SizedFile("photo-$index.jpg", size = 16) }

        val result = repository.uploadPhotos("incident-1", photos)

        assertEquals(0, photoApi.uploadPhotosCalls)
        assertEquals(
            "A maximum of 5 photos can be uploaded.",
            result.exceptionOrNull()?.message,
        )
    }

    @Test
    fun uploadPhotos_unsupportedExtensionFailsWithoutCallingApi() = runTest {
        val result = repository.uploadPhotos(
            "incident-1",
            listOf(SizedFile("photo.gif", size = 16)),
        )

        assertEquals(0, photoApi.uploadPhotosCalls)
        assertEquals(
            "Unsupported photo format: photo.gif",
            result.exceptionOrNull()?.message,
        )
    }

    @Test
    fun uploadPhotos_fileOverSizeLimitFailsWithoutCallingApi() = runTest {
        val oversized = 5L * 1024 * 1024 + 1
        val result = repository.uploadPhotos(
            "incident-1",
            listOf(SizedFile("photo.jpg", size = oversized)),
        )

        assertEquals(0, photoApi.uploadPhotosCalls)
        assertEquals(
            "Photo exceeds the 5 MB size limit: photo.jpg",
            result.exceptionOrNull()?.message,
        )
    }

    @Test
    fun uploadPhotos_successMapsReturnedPhotos() = runTest {
        val file = File.createTempFile("incident_photo_", ".jpg").apply {
            writeBytes(byteArrayOf(1, 2, 3, 4))
            deleteOnExit()
        }
        photoApi.uploadPhotosResponse = Response.success(
            ApiResponse(UploadPhotosDataDto(photos = listOf(IncidentDtoFixtures.photoDto()))),
        )

        val result = repository.uploadPhotos("incident-1", listOf(file))

        assertEquals(1, photoApi.uploadPhotosCalls)
        assertEquals(
            Result.success(listOf(Photo(id = "photo-1", url = "https://example.test/photo-1.jpg"))),
            result,
        )
    }

    private fun errorResponse(
        httpCode: Int,
        body: String,
    ): Response<ApiResponse<IncidentDto>> {
        return Response.error(
            httpCode,
            body.toResponseBody("application/json".toMediaType()),
        )
    }

    private class SizedFile(
        name: String,
        private val size: Long,
    ) : File(name) {
        override fun length(): Long = size
    }
}
