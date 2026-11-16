package com.example.incidentreporter.testing

import com.example.incidentreporter.domain.model.Incident
import com.example.incidentreporter.domain.model.IncidentCategory
import com.example.incidentreporter.domain.model.IncidentStatus
import com.example.incidentreporter.domain.model.Photo

object IncidentFixtures {

    fun incident(
        id: String = "incident-1",
        status: IncidentStatus = IncidentStatus.OPEN,
        photos: List<Photo> = emptyList(),
        otherCategoryDescription: String? = null,
        completedAt: String? = null,
        cancelledAt: String? = null,
    ): Incident {
        return Incident(
            id = id,
            incidentNumber = "INC-00124",
            title = "Broken Air Conditioning Unit",
            description = "Water leaking from the indoor unit near the reception area.",
            category = IncidentCategory.EQUIPMENT_DAMAGE,
            otherCategoryDescription = otherCategoryDescription,
            location = "Building A - Reception",
            photos = photos,
            status = status,
            createdAt = "2026-11-12T09:30:00Z",
            updatedAt = "2026-11-12T09:30:00Z",
            completedAt = completedAt,
            cancelledAt = cancelledAt,
        )
    }
}
