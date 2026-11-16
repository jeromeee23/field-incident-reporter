package com.example.incidentreporter.presentation.incidentlist.cancelled

import com.example.incidentreporter.domain.model.Incident
import com.example.incidentreporter.presentation.incidentlist.IncidentLoadError

data class CancelledIncidentUiState(
    val isLoading: Boolean = false,
    val incidents: List<Incident> = emptyList(),
    val loadError: IncidentLoadError? = null,
    val performingActionIncidentIds: Set<String> = emptySet(),
    val tabCount: Int = 0,
)
