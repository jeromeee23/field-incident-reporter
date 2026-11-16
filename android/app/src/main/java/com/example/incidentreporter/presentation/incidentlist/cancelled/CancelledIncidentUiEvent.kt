package com.example.incidentreporter.presentation.incidentlist.cancelled

sealed interface CancelledIncidentUiEvent {
    data class ShowMessage(val message: String) : CancelledIncidentUiEvent
}
