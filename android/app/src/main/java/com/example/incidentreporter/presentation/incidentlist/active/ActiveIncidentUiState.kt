package com.example.incidentreporter.presentation.incidentlist.active

import com.example.incidentreporter.domain.model.Incident

data class ActiveIncidentUiState(
    val isLoading: Boolean = false,
    val incidents: List<Incident> = emptyList(),
    val errorMessage: String? = null,
    val performingActionIncidentIds: Set<String> = emptySet(),
    val tabCount: Int = 0,
)
