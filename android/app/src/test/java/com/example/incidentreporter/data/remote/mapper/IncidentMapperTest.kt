package com.example.incidentreporter.data.remote.mapper

import com.example.incidentreporter.domain.model.CreateIncidentRequest
import com.example.incidentreporter.domain.model.IncidentCategory
import com.example.incidentreporter.domain.model.IncidentStatus
import com.example.incidentreporter.domain.model.Photo
import com.example.incidentreporter.domain.model.UpdateIncidentRequest
import com.example.incidentreporter.domain.model.UpdateIncidentStatusRequest
import com.example.incidentreporter.testing.IncidentDtoFixtures
import com.example.incidentreporter.testing.IncidentFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class IncidentMapperTest {

    @Test
    fun toIncident_mapsFieldsPhotosAndNullableTimestamps() {
        val dto = IncidentDtoFixtures.incidentDto(
            otherCategoryDescription = null,
            photos = listOf(IncidentDtoFixtures.photoDto()),
            completedAt = null,
            cancelledAt = null,
        )

        val incident = dto.toIncident()

        assertEquals(
            IncidentFixtures.incident(
                photos = listOf(Photo(id = "photo-1", url = "https://example.test/photo-1.jpg")),
            ),
            incident,
        )
    }

    @Test
    fun toIncident_mapsCompletedAndCancelledTimestamps() {
        val dto = IncidentDtoFixtures.incidentDto(
            status = "COMPLETED",
            completedAt = "2026-11-12T15:00:00Z",
            cancelledAt = "2026-11-12T16:00:00Z",
        )

        val incident = dto.toIncident()

        assertEquals(IncidentStatus.COMPLETED, incident.status)
        assertEquals("2026-11-12T15:00:00Z", incident.completedAt)
        assertEquals("2026-11-12T16:00:00Z", incident.cancelledAt)
    }

    @Test
    fun toIncident_unknownStatusThrows() {
        val error = assertThrows(IllegalArgumentException::class.java) {
            IncidentDtoFixtures.incidentDto(status = "CLOSED").toIncident()
        }

        assertEquals("Unknown incident status: CLOSED", error.message)
    }

    @Test
    fun toIncident_unknownCategoryThrows() {
        val error = assertThrows(IllegalArgumentException::class.java) {
            IncidentDtoFixtures.incidentDto(category = "WEATHER").toIncident()
        }

        assertEquals("Unknown incident category: WEATHER", error.message)
    }

    @Test
    fun createRequestToDto_mapsEnumNamesAndPhotos() {
        val request = CreateIncidentRequest(
            id = "incident-1",
            title = "Broken Air Conditioning Unit",
            description = "Water leaking from the indoor unit near the reception area.",
            category = IncidentCategory.OTHER,
            otherCategoryDescription = "HVAC leak",
            location = "Building A - Reception",
            photos = listOf(Photo(id = "photo-1", url = "https://example.test/photo-1.jpg")),
        )

        val dto = request.toDto()

        assertEquals("incident-1", dto.id)
        assertEquals("OTHER", dto.category)
        assertEquals("HVAC leak", dto.otherCategoryDescription)
        assertEquals(listOf(IncidentDtoFixtures.photoDto()), dto.photos)
    }

    @Test
    fun updateRequestToDto_mapsEnumNamesAndPhotos() {
        val request = UpdateIncidentRequest(
            title = "Updated title",
            description = "Updated description",
            category = IncidentCategory.SAFETY_ISSUE,
            otherCategoryDescription = null,
            location = "Building B",
            photos = listOf(Photo(id = "photo-2", url = "https://example.test/photo-2.jpg")),
        )

        val dto = request.toDto()

        assertEquals("SAFETY_ISSUE", dto.category)
        assertEquals(null, dto.otherCategoryDescription)
        assertEquals("photo-2", dto.photos.single().id)
        assertEquals("https://example.test/photo-2.jpg", dto.photos.single().url)
    }

    @Test
    fun updateStatusRequestToDto_mapsStatusName() {
        val dto = UpdateIncidentStatusRequest(status = IncidentStatus.IN_PROGRESS).toDto()

        assertEquals("IN_PROGRESS", dto.status)
    }

    @Test
    fun photo_roundTripsBetweenDomainAndDto() {
        val photo = Photo(id = "photo-1", url = "https://example.test/photo-1.jpg")

        assertEquals(photo, photo.toIncidentPhotoDto().toPhoto())
    }
}
