package com.example.incidentreporter.presentation.incidentlist.active

import com.example.incidentreporter.domain.model.Incident
import com.example.incidentreporter.presentation.incidentlist.IncidentLoadError

data class ActiveIncidentUiState(
    val isLoading: Boolean = false,
    val incidents: List<Incident> = emptyList(),
    val loadError: IncidentLoadError? = null,
    val performingActionIncidentIds: Set<String> = emptySet(),
    val tabCount: Int = 0,
)
