package com.example.incidentreporter.presentation.incidentlist.completed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.incidentreporter.R
import com.example.incidentreporter.domain.model.Incident
import com.example.incidentreporter.presentation.incidentlist.IncidentPreviewData
import com.example.incidentreporter.presentation.incidentlist.components.IncidentListContent
import com.example.incidentreporter.presentation.theme.FieldIncidentReporterTheme
import com.example.incidentreporter.presentation.theme.HomeBackground

@Composable
fun CompletedIncidentScreen(
    uiState: CompletedIncidentUiState,
    onRetry: () -> Unit,
    onCreateReport: () -> Unit,
    onIncidentClick: (Incident) -> Unit,
    onRemoveConfirmed: (Incident) -> Unit,
    modifier: Modifier = Modifier,
) {
    IncidentListContent(
        isLoading = uiState.isLoading,
        incidents = uiState.incidents,
        loadError = uiState.loadError,
        performingActionIncidentIds = uiState.performingActionIncidentIds,
        emptyTitle = stringResource(R.string.home_empty_completed_title),
        emptyMessage = stringResource(R.string.home_empty_completed_message),
        showCancelAction = false,
        onRetry = onRetry,
        onCreateReport = onCreateReport,
        onIncidentClick = onIncidentClick,
        onCancelConfirmed = {},
        onRemoveConfirmed = onRemoveConfirmed,
        modifier = modifier,
    )
}

@Preview(name = "Completed - List", showBackground = true)
@Composable
private fun CompletedIncidentListPreview() {
    CompletedIncidentPreview(IncidentPreviewData.completedListState)
}

@Preview(name = "Completed - Empty", showBackground = true)
@Composable
private fun CompletedIncidentEmptyPreview() {
    CompletedIncidentPreview(IncidentPreviewData.completedEmptyState)
}

@Composable
private fun CompletedIncidentPreview(uiState: CompletedIncidentUiState) {
    FieldIncidentReporterTheme(dynamicColor = false) {
        CompletedIncidentScreen(
            uiState = uiState,
            onRetry = {},
            onCreateReport = {},
            onIncidentClick = {},
            onRemoveConfirmed = {},
            modifier = Modifier
                .fillMaxSize()
                .background(HomeBackground),
        )
    }
}
