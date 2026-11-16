package com.example.incidentreporter.presentation.incidentlist.active

sealed interface ActiveIncidentUiEvent {
    data class ShowMessage(val message: String) : ActiveIncidentUiEvent
}
