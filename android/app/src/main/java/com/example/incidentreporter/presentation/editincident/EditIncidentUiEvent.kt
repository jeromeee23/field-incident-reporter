package com.example.incidentreporter.presentation.editincident

sealed interface EditIncidentUiEvent {
    data object NavigateBack : EditIncidentUiEvent
    data class ShowPhotoError(val message: String) : EditIncidentUiEvent
    data class ShowMessage(val message: String) : EditIncidentUiEvent
}
