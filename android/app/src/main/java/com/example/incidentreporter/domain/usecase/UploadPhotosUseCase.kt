package com.example.incidentreporter.domain.usecase

import com.example.incidentreporter.domain.model.Photo
import com.example.incidentreporter.domain.repository.IncidentRepository
import java.io.File
import javax.inject.Inject

class UploadPhotosUseCase @Inject constructor(
    private val incidentRepository: IncidentRepository,
) {

    suspend operator fun invoke(
        incidentId: String,
        photos: List<File>,
    ): Result<List<Photo>> {
        return incidentRepository.uploadPhotos(incidentId, photos)
    }
}
