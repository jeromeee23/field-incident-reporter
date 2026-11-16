package com.example.incidentreporter.domain.usecase

import com.example.incidentreporter.domain.model.Incident
import com.example.incidentreporter.domain.repository.IncidentRepository
import javax.inject.Inject

class GetIncidentByIdUseCase @Inject constructor(
    private val incidentRepository: IncidentRepository,
) {

    suspend operator fun invoke(
        incidentId: String,
    ): Result<Incident> {
        return incidentRepository.getIncidentById(incidentId)
    }
}
