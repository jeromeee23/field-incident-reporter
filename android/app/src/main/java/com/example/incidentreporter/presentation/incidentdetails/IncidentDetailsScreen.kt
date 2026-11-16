package com.example.incidentreporter.presentation.incidentdetails

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.incidentreporter.R
import com.example.incidentreporter.domain.model.Incident
import com.example.incidentreporter.domain.model.IncidentCategory
import com.example.incidentreporter.domain.model.IncidentStatus
import com.example.incidentreporter.domain.model.Photo
import com.example.incidentreporter.presentation.incidentdetails.components.IncidentDetailsContent
import com.example.incidentreporter.presentation.incidentdetails.components.IncidentPhotoViewer
import com.example.incidentreporter.presentation.incidentdetails.components.UpdateStatusDialog
import com.example.incidentreporter.presentation.incidentlist.components.CancelReportDialog
import com.example.incidentreporter.presentation.incidentlist.components.RemoveReportDialog
import com.example.incidentreporter.presentation.theme.FieldIncidentReporterTheme
import com.example.incidentreporter.presentation.theme.HomeCard
import com.example.incidentreporter.presentation.theme.HomeFabBlue
import com.example.incidentreporter.presentation.theme.HomeNavy
import com.example.incidentreporter.presentation.theme.HomeSkeleton
import com.example.incidentreporter.presentation.theme.HomeTextMuted
import com.example.incidentreporter.presentation.theme.HomeTextPrimary
import com.example.incidentreporter.presentation.theme.HomeTextSecondary

@Composable
fun IncidentDetailsScreen(
    incidentId: String,
    onBack: () -> Unit,
    onEditClick: (String) -> Unit = {},
    viewModel: IncidentDetailsViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(incidentId) {
        viewModel.loadIncident(incidentId)
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is IncidentDetailsUiEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                IncidentDetailsUiEvent.NavigateBack -> onBack()
            }
        }
    }

    IncidentDetailsScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onRetry = viewModel::retry,
        onEditClick = {
            uiState.incident?.id?.let(onEditClick)
        },
        onUpdateStatus = viewModel::updateStatus,
        onCancelConfirmed = viewModel::cancelIncident,
        onRemoveConfirmed = viewModel::deleteIncident,
    )
}

@Composable
internal fun IncidentDetailsScreenContent(
    uiState: IncidentDetailsUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onEditClick: () -> Unit,
    onUpdateStatus: (IncidentStatus) -> Unit,
    onCancelConfirmed: () -> Unit,
    onRemoveConfirmed: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    var showUpdateStatusDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var showRemoveDialog by remember { mutableStateOf(false) }
    var photoViewerIndex by remember { mutableStateOf<Int?>(null) }
    val incident = uiState.incident

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = HomeCard,
            contentWindowInsets = WindowInsets.navigationBars,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = { IncidentDetailsTopBar(onBack = onBack) },
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                when {
                    uiState.isLoading && incident == null -> {
                        IncidentDetailsLoading(modifier = Modifier.fillMaxSize())
                    }

                    uiState.errorMessage != null && incident == null -> {
                        IncidentDetailsError(
                            onRetry = onRetry,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }

                    incident != null -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 20.dp, vertical = 20.dp),
                        ) {
                            IncidentDetailsContent(
                                incident = incident,
                                canEdit = uiState.canEdit,
                                canUpdateStatus = uiState.canUpdateStatus,
                                canCancel = uiState.canCancel,
                                canRemove = uiState.canRemove,
                                isUpdatingStatus = uiState.isUpdatingStatus,
                                isCancelling = uiState.isCancelling,
                                isDeleting = uiState.isDeleting,
                                isActionInProgress = uiState.isActionInProgress,
                                onPhotoClick = { index -> photoViewerIndex = index },
                                onEditClick = onEditClick,
                                onUpdateStatusClick = { showUpdateStatusDialog = true },
                                onCancelClick = { showCancelDialog = true },
                                onRemoveClick = { showRemoveDialog = true },
                            )
                        }
                    }
                }
            }
        }

    if (showUpdateStatusDialog && incident != null) {
        val availableStatuses = incident.status.nextAllowedStatuses()
        if (availableStatuses.isNotEmpty()) {
            UpdateStatusDialog(
                availableStatuses = availableStatuses,
                onDismiss = { showUpdateStatusDialog = false },
                onConfirm = { status ->
                    showUpdateStatusDialog = false
                    onUpdateStatus(status)
                },
            )
        }
    }

    if (showCancelDialog) {
        CancelReportDialog(
            onKeepReport = { showCancelDialog = false },
            onCancelReport = {
                showCancelDialog = false
                onCancelConfirmed()
            },
        )
    }

    if (showRemoveDialog) {
        RemoveReportDialog(
            onClose = { showRemoveDialog = false },
            onRemove = {
                showRemoveDialog = false
                onRemoveConfirmed()
            },
        )
    }

        val viewerPhotos = incident?.photos.orEmpty()
        val viewerIndex = photoViewerIndex
        if (viewerIndex != null && viewerPhotos.isNotEmpty()) {
            IncidentPhotoViewer(
                photos = viewerPhotos,
                initialIndex = viewerIndex,
                onClose = { photoViewerIndex = null },
            )
        }
    }
}

@Composable
private fun IncidentDetailsTopBar(
    onBack: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(HomeNavy)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                painter = painterResource(R.drawable.ic_create_back),
                contentDescription = stringResource(R.string.create_back),
                tint = Color.White,
            )
        }
        Text(
            text = stringResource(R.string.details_title),
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun IncidentDetailsLoading(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = HomeFabBlue)
    }
}

@Composable
private fun IncidentDetailsError(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(HomeSkeleton),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_home_wifi_off),
                contentDescription = null,
                tint = HomeTextMuted,
                modifier = Modifier.size(32.dp),
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.details_error_title),
            color = HomeTextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.details_error_message),
            color = HomeTextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = HomeFabBlue,
                contentColor = Color.White,
            ),
        ) {
            Text(
                text = stringResource(R.string.details_retry),
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Preview(name = "Incident Details", showBackground = true, showSystemUi = true)
@Composable
private fun IncidentDetailsPreview() {
    FieldIncidentReporterTheme(dynamicColor = false) {
        IncidentDetailsScreenContent(
            uiState = IncidentDetailsUiState(incident = previewIncident()),
            onBack = {},
            onRetry = {},
            onEditClick = {},
            onUpdateStatus = {},
            onCancelConfirmed = {},
            onRemoveConfirmed = {},
        )
    }
}

@Preview(name = "Incident Details Loading", showBackground = true, showSystemUi = true)
@Composable
private fun IncidentDetailsLoadingPreview() {
    FieldIncidentReporterTheme(dynamicColor = false) {
        IncidentDetailsScreenContent(
            uiState = IncidentDetailsUiState(isLoading = true),
            onBack = {},
            onRetry = {},
            onEditClick = {},
            onUpdateStatus = {},
            onCancelConfirmed = {},
            onRemoveConfirmed = {},
        )
    }
}

@Preview(name = "Incident Details Error", showBackground = true, showSystemUi = true)
@Composable
private fun IncidentDetailsErrorPreview() {
    FieldIncidentReporterTheme(dynamicColor = false) {
        IncidentDetailsScreenContent(
            uiState = IncidentDetailsUiState(
                isLoading = false,
                errorMessage = "Unable to load incident",
            ),
            onBack = {},
            onRetry = {},
            onEditClick = {},
            onUpdateStatus = {},
            onCancelConfirmed = {},
            onRemoveConfirmed = {},
        )
    }
}

private fun previewIncident(): Incident {
    return Incident(
        id = "1",
        incidentNumber = "INC-00124",
        title = "Broken Air Conditioning Unit",
        description = "Water leaking from the indoor unit near the reception area.",
        category = IncidentCategory.EQUIPMENT_DAMAGE,
        otherCategoryDescription = null,
        location = "Building A - Reception",
        photos = listOf(
            Photo(id = "p1", url = "https://example.com/photo-1.jpg"),
            Photo(id = "p2", url = "https://example.com/photo-2.jpg"),
            Photo(id = "p3", url = "https://example.com/photo-3.jpg"),
            Photo(id = "p4", url = "https://example.com/photo-4.jpg"),
        ),
        status = IncidentStatus.IN_PROGRESS,
        createdAt = "2026-11-12T09:30:00Z",
        updatedAt = "2026-11-12T14:18:00Z",
        completedAt = null,
        cancelledAt = null,
    )
}
