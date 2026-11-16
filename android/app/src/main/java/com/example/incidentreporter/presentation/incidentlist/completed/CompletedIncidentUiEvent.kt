package com.example.incidentreporter.presentation.incidentlist.completed

sealed interface CompletedIncidentUiEvent {
    data class ShowMessage(val message: String) : CompletedIncidentUiEvent
}
