package com.example.incidentreporter.domain.model

data class UpdateIncidentRequest(
    val title: String,
    val description: String,
    val category: IncidentCategory,
    val otherCategoryDescription: String?,
    val location: String,
    val photos: List<Photo>,
)
