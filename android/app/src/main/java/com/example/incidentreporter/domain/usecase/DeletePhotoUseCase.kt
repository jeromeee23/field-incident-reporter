package com.example.incidentreporter.domain.usecase

import com.example.incidentreporter.domain.repository.IncidentRepository
import javax.inject.Inject

class DeletePhotoUseCase @Inject constructor(
    private val incidentRepository: IncidentRepository,
) {

    suspend operator fun invoke(
        photoId: String,
    ): Result<Unit> {
        return incidentRepository.deletePhoto(photoId)
    }
}
