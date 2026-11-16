package com.example.incidentreporter.presentation.incidentdetails

sealed interface IncidentDetailsUiEvent {
    data class ShowMessage(val message: String) : IncidentDetailsUiEvent
    data object NavigateBack : IncidentDetailsUiEvent
}
