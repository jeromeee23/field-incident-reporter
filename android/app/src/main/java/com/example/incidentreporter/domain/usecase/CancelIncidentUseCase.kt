package com.example.incidentreporter.domain.usecase

import com.example.incidentreporter.domain.model.Incident
import com.example.incidentreporter.domain.model.IncidentStatus
import com.example.incidentreporter.domain.model.UpdateIncidentStatusRequest
import com.example.incidentreporter.domain.repository.IncidentRepository
import javax.inject.Inject

class CancelIncidentUseCase @Inject constructor(
    private val incidentRepository: IncidentRepository,
) {

    suspend operator fun invoke(
        incidentId: String,
    ): Result<Incident> {
        return incidentRepository.updateIncidentStatus(
            id = incidentId,
            request = UpdateIncidentStatusRequest(status = IncidentStatus.CANCELLED),
        )
    }
}
