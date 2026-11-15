package com.example.incidentreporter.data.remote.dto.response

data class IncidentDto(
    val id: String,
    val incidentNumber: String,
    val title: String,
    val description: String,
    val category: String,
    val otherCategoryDescription: String?,
    val location: String,
    val photos: List<IncidentPhotoDto>,
    val status: String,
    val createdAt: String,
    val updatedAt: String,
    val completedAt: String?,
    val cancelledAt: String?,
)
