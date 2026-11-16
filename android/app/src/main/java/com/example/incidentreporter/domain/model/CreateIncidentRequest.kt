package com.example.incidentreporter.domain.model

data class CreateIncidentRequest(
    val id: String,
    val title: String,
    val description: String,
    val category: IncidentCategory,
    val otherCategoryDescription: String?,
    val location: String,
    val photos: List<Photo>,
)
