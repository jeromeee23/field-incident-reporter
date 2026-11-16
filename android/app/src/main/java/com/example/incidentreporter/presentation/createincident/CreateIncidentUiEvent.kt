package com.example.incidentreporter.presentation.createincident

sealed interface CreateIncidentUiEvent {
    data class NavigateToIncidentCreated(val incidentId: String) : CreateIncidentUiEvent
    data class ShowPhotoError(val message: String) : CreateIncidentUiEvent
}
