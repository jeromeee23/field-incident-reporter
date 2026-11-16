package com.example.incidentreporter.presentation.incidentdetails

import com.example.incidentreporter.domain.model.Incident
import com.example.incidentreporter.domain.model.IncidentStatus

data class IncidentDetailsUiState(
    val isLoading: Boolean = true,
    val incident: Incident? = null,
    val errorMessage: String? = null,
    val isUpdatingStatus: Boolean = false,
    val isCancelling: Boolean = false,
    val isDeleting: Boolean = false,
) {
    val isActionInProgress: Boolean
        get() = isUpdatingStatus || isCancelling || isDeleting

    val canEdit: Boolean
        get() = incident?.status.allowsLifecycleActions()

    val canUpdateStatus: Boolean
        get() = !incident?.status?.nextAllowedStatuses().isNullOrEmpty()

    val canCancel: Boolean
        get() = incident?.status.allowsLifecycleActions()

    val canRemove: Boolean
        get() = incident != null
}

internal fun IncidentStatus.nextAllowedStatuses(): List<IncidentStatus> {
    return when (this) {
        IncidentStatus.OPEN -> listOf(IncidentStatus.IN_PROGRESS)
        IncidentStatus.IN_PROGRESS -> listOf(IncidentStatus.COMPLETED)
        IncidentStatus.COMPLETED,
        IncidentStatus.CANCELLED,
        -> emptyList()
    }
}

internal fun IncidentStatus?.allowsLifecycleActions(): Boolean {
    return this == IncidentStatus.OPEN || this == IncidentStatus.IN_PROGRESS
}
