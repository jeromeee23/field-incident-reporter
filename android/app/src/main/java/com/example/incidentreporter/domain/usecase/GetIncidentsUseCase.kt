package com.example.incidentreporter.domain.usecase

import com.example.incidentreporter.domain.model.Incident
import com.example.incidentreporter.domain.model.IncidentStatus
import com.example.incidentreporter.domain.repository.IncidentRepository
import javax.inject.Inject

class GetIncidentsUseCase @Inject constructor(
    private val incidentRepository: IncidentRepository,
) {

    suspend operator fun invoke(
        statuses: List<IncidentStatus>,
    ): Result<List<Incident>> {
        return incidentRepository.getIncidents(statuses)
    }
}
