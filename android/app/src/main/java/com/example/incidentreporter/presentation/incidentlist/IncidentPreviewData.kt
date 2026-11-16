package com.example.incidentreporter.presentation.incidentlist

import com.example.incidentreporter.domain.model.Incident
import com.example.incidentreporter.domain.model.IncidentCategory
import com.example.incidentreporter.domain.model.IncidentStatus
import com.example.incidentreporter.presentation.incidentlist.active.ActiveIncidentUiState
import com.example.incidentreporter.presentation.incidentlist.cancelled.CancelledIncidentUiState
import com.example.incidentreporter.presentation.incidentlist.completed.CompletedIncidentUiState

internal object IncidentPreviewData {

    val activeIncidents = listOf(
        previewIncident(
            id = "1",
            number = "INC-00124",
            title = "Broken Air Conditioning Unit",
            description = "Water leaking from the indoor unit near the reception area.",
            status = IncidentStatus.OPEN,
            createdAt = "2026-11-12T09:30:00Z",
        ),
        previewIncident(
            id = "2",
            number = "INC-00123",
            title = "Damaged Parking Barrier",
            description = "Barrier arm is not functioning properly and is stuck.",
            status = IncidentStatus.IN_PROGRESS,
            createdAt = "2026-11-11T16:15:00Z",
        ),
        previewIncident(
            id = "3",
            number = "INC-00122",
            title = "Electrical Panel Issue",
            description = "Unusual noise coming from the electrical panel.",
            status = IncidentStatus.OPEN,
            createdAt = "2026-11-11T11:20:00Z",
        ),
    )

    val completedIncidents = listOf(
        previewIncident(
            id = "4",
            number = "INC-00118",
            title = "Lobby Light Replacement",
            description = "Flickering lights in the main lobby have been replaced.",
            status = IncidentStatus.COMPLETED,
            createdAt = "2026-11-08T10:00:00Z",
            completedAt = "2026-11-10T15:00:00Z",
        ),
    )

    val cancelledIncidents = listOf(
        previewIncident(
            id = "5",
            number = "INC-00110",
            title = "Duplicate Safety Report",
            description = "Opened in error. Duplicate of an existing report.",
            status = IncidentStatus.CANCELLED,
            createdAt = "2026-11-05T08:45:00Z",
            cancelledAt = "2026-11-05T09:10:00Z",
        ),
    )

    val activeListState = ActiveIncidentUiState(
        incidents = activeIncidents,
        tabCount = activeIncidents.size,
    )

    val activeLoadingState = ActiveIncidentUiState(isLoading = true)

    val activeEmptyState = ActiveIncidentUiState()

    val activeErrorState = ActiveIncidentUiState(
        errorMessage = "Unable to load reports",
    )

    val completedListState = CompletedIncidentUiState(
        incidents = completedIncidents,
        tabCount = completedIncidents.size,
    )

    val completedEmptyState = CompletedIncidentUiState()

    val cancelledListState = CancelledIncidentUiState(
        incidents = cancelledIncidents,
        tabCount = cancelledIncidents.size,
    )

    val cancelledEmptyState = CancelledIncidentUiState()

    private fun previewIncident(
        id: String,
        number: String,
        title: String,
        description: String,
        status: IncidentStatus,
        createdAt: String,
        completedAt: String? = null,
        cancelledAt: String? = null,
    ): Incident {
        return Incident(
            id = id,
            incidentNumber = number,
            title = title,
            description = description,
            category = IncidentCategory.SAFETY_ISSUE,
            otherCategoryDescription = null,
            location = "Building A - Reception",
            photos = emptyList(),
            status = status,
            createdAt = createdAt,
            updatedAt = createdAt,
            completedAt = completedAt,
            cancelledAt = cancelledAt,
        )
    }
}
