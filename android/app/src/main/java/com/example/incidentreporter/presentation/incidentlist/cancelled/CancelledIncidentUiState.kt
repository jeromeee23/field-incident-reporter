package com.example.incidentreporter.presentation.incidentlist.cancelled

import com.example.incidentreporter.domain.model.Incident

data class CancelledIncidentUiState(
    val isLoading: Boolean = false,
    val incidents: List<Incident> = emptyList(),
    val errorMessage: String? = null,
    val performingActionIncidentIds: Set<String> = emptySet(),
    val tabCount: Int = 0,
)
