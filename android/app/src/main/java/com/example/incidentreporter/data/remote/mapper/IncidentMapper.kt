package com.example.incidentreporter.data.remote.mapper

import com.example.incidentreporter.data.remote.dto.request.CreateIncidentRequest as CreateIncidentRequestDto
import com.example.incidentreporter.data.remote.dto.request.UpdateIncidentRequest as UpdateIncidentRequestDto
import com.example.incidentreporter.data.remote.dto.request.UpdateIncidentStatusRequest as UpdateIncidentStatusRequestDto
import com.example.incidentreporter.data.remote.dto.response.IncidentDto
import com.example.incidentreporter.domain.model.CreateIncidentRequest
import com.example.incidentreporter.domain.model.Incident
import com.example.incidentreporter.domain.model.IncidentCategory
import com.example.incidentreporter.domain.model.IncidentStatus
import com.example.incidentreporter.domain.model.UpdateIncidentRequest
import com.example.incidentreporter.domain.model.UpdateIncidentStatusRequest

fun IncidentDto.toIncident(): Incident {
    return Incident(
        id = id,
        incidentNumber = incidentNumber,
        title = title,
        description = description,
        category = category.toIncidentCategory(),
        otherCategoryDescription = otherCategoryDescription,
        location = location,
        photos = photos.map { it.toPhoto() },
        status = status.toIncidentStatus(),
        createdAt = createdAt,
        updatedAt = updatedAt,
        completedAt = completedAt,
        cancelledAt = cancelledAt,
    )
}

fun CreateIncidentRequest.toDto(): CreateIncidentRequestDto {
    return CreateIncidentRequestDto(
        id = id,
        title = title,
        description = description,
        category = category.name,
        otherCategoryDescription = otherCategoryDescription,
        location = location,
        photos = photos.map { it.toIncidentPhotoDto() },
    )
}

fun UpdateIncidentRequest.toDto(): UpdateIncidentRequestDto {
    return UpdateIncidentRequestDto(
        title = title,
        description = description,
        category = category.name,
        otherCategoryDescription = otherCategoryDescription,
        location = location,
        photos = photos.map { it.toIncidentPhotoDto() },
    )
}

fun UpdateIncidentStatusRequest.toDto(): UpdateIncidentStatusRequestDto {
    return UpdateIncidentStatusRequestDto(
        status = status.name,
    )
}

private fun String.toIncidentStatus(): IncidentStatus {
    return IncidentStatus.entries.find { it.name == this }
        ?: throw IllegalArgumentException("Unknown incident status: $this")
}

private fun String.toIncidentCategory(): IncidentCategory {
    return IncidentCategory.entries.find { it.name == this }
        ?: throw IllegalArgumentException("Unknown incident category: $this")
}
