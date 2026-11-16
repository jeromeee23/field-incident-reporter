package com.example.incidentreporter.domain.usecase

import com.example.incidentreporter.domain.model.CreateIncidentRequest
import com.example.incidentreporter.domain.model.Incident
import com.example.incidentreporter.domain.repository.IncidentRepository
import javax.inject.Inject

class CreateIncidentUseCase @Inject constructor(
    private val incidentRepository: IncidentRepository,
) {

    suspend operator fun invoke(
        request: CreateIncidentRequest,
    ): Result<Incident> {
        return incidentRepository.createIncident(request)
    }
}
