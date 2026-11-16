package com.example.incidentreporter.domain.model

data class Incident(
    val id: String,
    val incidentNumber: String,
    val title: String,
    val description: String,
    val category: IncidentCategory,
    val otherCategoryDescription: String?,
    val location: String,
    val photos: List<Photo>,
    val status: IncidentStatus,
    val createdAt: String,
    val updatedAt: String,
    val completedAt: String?,
    val cancelledAt: String?,
)
