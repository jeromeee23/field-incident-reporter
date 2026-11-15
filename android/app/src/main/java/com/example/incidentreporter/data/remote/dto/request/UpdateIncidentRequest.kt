package com.example.incidentreporter.data.remote.dto.request

import com.example.incidentreporter.data.remote.dto.response.IncidentPhotoDto

data class UpdateIncidentRequest(
    val title: String,
    val description: String,
    val category: String,
    val otherCategoryDescription: String?,
    val location: String,
    val photos: List<IncidentPhotoDto>,
)
