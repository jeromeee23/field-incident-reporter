package com.example.incidentreporter.domain.usecase

import com.example.incidentreporter.domain.repository.IncidentRepository
import javax.inject.Inject

class DeleteIncidentUseCase @Inject constructor(
    private val incidentRepository: IncidentRepository,
) {

    suspend operator fun invoke(
        incidentId: String,
    ): Result<Unit> {
        return incidentRepository.deleteIncident(incidentId)
    }
}
