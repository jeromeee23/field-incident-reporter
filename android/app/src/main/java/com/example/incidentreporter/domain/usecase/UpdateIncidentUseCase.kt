package com.example.incidentreporter.domain.usecase

import com.example.incidentreporter.domain.model.Incident
import com.example.incidentreporter.domain.model.UpdateIncidentRequest
import com.example.incidentreporter.domain.repository.IncidentRepository
import javax.inject.Inject

class UpdateIncidentUseCase @Inject constructor(
    private val incidentRepository: IncidentRepository,
) {

    suspend operator fun invoke(
        incidentId: String,
        request: UpdateIncidentRequest,
    ): Result<Incident> {
        return incidentRepository.updateIncident(incidentId, request)
    }
}
