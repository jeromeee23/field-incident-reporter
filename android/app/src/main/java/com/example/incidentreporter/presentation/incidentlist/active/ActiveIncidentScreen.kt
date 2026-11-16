package com.example.incidentreporter.presentation.incidentlist.active

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
fun ActiveIncidentScreen(
    uiState: ActiveIncidentUiState,
    onRetry: () -> Unit,
    onCreateReport: () -> Unit,
    onIncidentClick: (Incident) -> Unit,
    onCancelConfirmed: (Incident) -> Unit,
    onRemoveConfirmed: (Incident) -> Unit,
    modifier: Modifier = Modifier,
) {
    IncidentListContent(
        isLoading = uiState.isLoading,
        incidents = uiState.incidents,
        errorMessage = uiState.errorMessage,
        performingActionIncidentIds = uiState.performingActionIncidentIds,
        emptyTitle = stringResource(R.string.home_empty_active_title),
        emptyMessage = stringResource(R.string.home_empty_active_message),
        showCancelAction = true,
        onRetry = onRetry,
        onCreateReport = onCreateReport,
        onIncidentClick = onIncidentClick,
        onCancelConfirmed = onCancelConfirmed,
        onRemoveConfirmed = onRemoveConfirmed,
        modifier = modifier,
    )
}

@Preview(name = "Active - List", showBackground = true)
@Composable
private fun ActiveIncidentListPreview() {
    ActiveIncidentPreview(IncidentPreviewData.activeListState)
}

@Preview(name = "Active - Empty", showBackground = true)
@Composable
private fun ActiveIncidentEmptyPreview() {
    ActiveIncidentPreview(IncidentPreviewData.activeEmptyState)
}

@Preview(name = "Active - Loading", showBackground = true)
@Composable
private fun ActiveIncidentLoadingPreview() {
    ActiveIncidentPreview(IncidentPreviewData.activeLoadingState)
}

@Preview(name = "Active - Error", showBackground = true)
@Composable
private fun ActiveIncidentErrorPreview() {
    ActiveIncidentPreview(IncidentPreviewData.activeErrorState)
}

@Composable
private fun ActiveIncidentPreview(uiState: ActiveIncidentUiState) {
    FieldIncidentReporterTheme(dynamicColor = false) {
        ActiveIncidentScreen(
            uiState = uiState,
            onRetry = {},
            onCreateReport = {},
            onIncidentClick = {},
            onCancelConfirmed = {},
            onRemoveConfirmed = {},
            modifier = Modifier
                .fillMaxSize()
                .background(HomeBackground),
        )
    }
}
