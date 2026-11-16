package com.example.incidentreporter.data.remote.mapper

import com.example.incidentreporter.data.remote.dto.response.IncidentPhotoDto
import com.example.incidentreporter.domain.model.Photo

fun IncidentPhotoDto.toPhoto(): Photo {
    return Photo(
        id = id,
        url = url,
    )
}

fun Photo.toIncidentPhotoDto(): IncidentPhotoDto {
    return IncidentPhotoDto(
        id = id,
        url = url,
    )
}
