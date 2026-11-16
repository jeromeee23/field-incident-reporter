package com.example.incidentreporter.domain.usecase

import com.example.incidentreporter.domain.model.Incident
import com.example.incidentreporter.domain.model.UpdateIncidentStatusRequest
import com.example.incidentreporter.domain.repository.IncidentRepository
import javax.inject.Inject

class UpdateIncidentStatusUseCase @Inject constructor(
    private val incidentRepository: IncidentRepository,
) {

    suspend operator fun invoke(
        incidentId: String,
        request: UpdateIncidentStatusRequest,
    ): Result<Incident> {
        return incidentRepository.updateIncidentStatus(incidentId, request)
    }
}
