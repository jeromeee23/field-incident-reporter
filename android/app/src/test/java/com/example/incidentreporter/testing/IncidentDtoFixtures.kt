package com.example.incidentreporter.testing

import com.example.incidentreporter.data.remote.dto.response.IncidentDto
import com.example.incidentreporter.data.remote.dto.response.IncidentPhotoDto

object IncidentDtoFixtures {

    fun photoDto(
        id: String = "photo-1",
        url: String = "https://example.test/photo-1.jpg",
    ): IncidentPhotoDto {
        return IncidentPhotoDto(
            id = id,
            url = url,
        )
    }

    fun incidentDto(
        id: String = "incident-1",
        incidentNumber: String = "INC-00124",
        title: String = "Broken Air Conditioning Unit",
        description: String = "Water leaking from the indoor unit near the reception area.",
        category: String = "EQUIPMENT_DAMAGE",
        otherCategoryDescription: String? = null,
        location: String = "Building A - Reception",
        photos: List<IncidentPhotoDto> = emptyList(),
        status: String = "OPEN",
        createdAt: String = "2026-11-12T09:30:00Z",
        updatedAt: String = "2026-11-12T09:30:00Z",
        completedAt: String? = null,
        cancelledAt: String? = null,
    ): IncidentDto {
        return IncidentDto(
            id = id,
            incidentNumber = incidentNumber,
            title = title,
            description = description,
            category = category,
            otherCategoryDescription = otherCategoryDescription,
            location = location,
            photos = photos,
            status = status,
            createdAt = createdAt,
            updatedAt = updatedAt,
            completedAt = completedAt,
            cancelledAt = cancelledAt,
        )
    }
}
