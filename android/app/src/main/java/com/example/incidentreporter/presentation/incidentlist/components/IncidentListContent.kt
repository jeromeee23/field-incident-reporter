package com.example.incidentreporter.presentation.incidentlist.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.incidentreporter.domain.model.Incident

@Composable
fun IncidentListContent(
    isLoading: Boolean,
    incidents: List<Incident>,
    errorMessage: String?,
    performingActionIncidentIds: Set<String>,
    emptyTitle: String,
    emptyMessage: String,
    showCancelAction: Boolean,
    onRetry: () -> Unit,
    onCreateReport: () -> Unit,
    onIncidentClick: (Incident) -> Unit,
    onCancelConfirmed: (Incident) -> Unit,
    onRemoveConfirmed: (Incident) -> Unit,
    modifier: Modifier = Modifier,
) {
    var pendingCancelId by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingRemoveId by rememberSaveable { mutableStateOf<String?>(null) }
    val pendingCancel = incidents.firstOrNull { it.id == pendingCancelId }
    val pendingRemove = incidents.firstOrNull { it.id == pendingRemoveId }

    when {
        isLoading -> {
            IncidentListLoading(modifier = modifier)
        }

        errorMessage != null -> {
            IncidentListError(
                onRetry = onRetry,
                modifier = modifier,
            )
        }

        incidents.isEmpty() -> {
            IncidentListEmpty(
                title = emptyTitle,
                message = emptyMessage,
                onCreateReport = onCreateReport,
                modifier = modifier,
            )
        }

        else -> {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(
                    items = incidents,
                    key = { it.id },
                ) { incident ->
                    IncidentSwipeRow(
                        showCancelAction = showCancelAction,
                        isActionInProgress = incident.id in performingActionIncidentIds,
                        onCancel = { pendingCancelId = incident.id },
                        onRemove = { pendingRemoveId = incident.id },
                    ) {
                        IncidentCard(
                            incident = incident,
                            onClick = { onIncidentClick(incident) },
                        )
                    }
                }
            }
        }
    }

    if (pendingCancel != null) {
        CancelReportDialog(
            onKeepReport = { pendingCancelId = null },
            onCancelReport = {
                pendingCancelId = null
                onCancelConfirmed(pendingCancel)
            },
        )
    }

    if (pendingRemove != null) {
        RemoveReportDialog(
            onClose = { pendingRemoveId = null },
            onRemove = {
                pendingRemoveId = null
                onRemoveConfirmed(pendingRemove)
            },
        )
    }
}
