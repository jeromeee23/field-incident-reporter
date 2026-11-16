package com.example.incidentreporter.presentation.incidentlist.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButtonDefaults
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.incidentreporter.R
import com.example.incidentreporter.domain.model.Incident
import com.example.incidentreporter.presentation.incidentlist.IncidentPreviewData
import com.example.incidentreporter.presentation.incidentlist.active.ActiveIncidentScreen
import com.example.incidentreporter.presentation.incidentlist.active.ActiveIncidentUiEvent
import com.example.incidentreporter.presentation.incidentlist.active.ActiveIncidentUiState
import com.example.incidentreporter.presentation.incidentlist.active.ActiveIncidentViewModel
import com.example.incidentreporter.presentation.incidentlist.cancelled.CancelledIncidentScreen
import com.example.incidentreporter.presentation.incidentlist.cancelled.CancelledIncidentUiEvent
import com.example.incidentreporter.presentation.incidentlist.cancelled.CancelledIncidentUiState
import com.example.incidentreporter.presentation.incidentlist.cancelled.CancelledIncidentViewModel
import com.example.incidentreporter.presentation.incidentlist.completed.CompletedIncidentScreen
import com.example.incidentreporter.presentation.incidentlist.completed.CompletedIncidentUiEvent
import com.example.incidentreporter.presentation.incidentlist.completed.CompletedIncidentUiState
import com.example.incidentreporter.presentation.incidentlist.completed.CompletedIncidentViewModel
import com.example.incidentreporter.presentation.incidentlist.components.IncidentTabBar
import com.example.incidentreporter.presentation.theme.FieldIncidentReporterTheme
import com.example.incidentreporter.presentation.theme.HomeBackground
import com.example.incidentreporter.presentation.theme.HomeFabBlue
import com.example.incidentreporter.presentation.theme.HomeHeaderIconBackground
import com.example.incidentreporter.presentation.theme.HomeNavy

@Composable
fun HomeScreen(
    onCreateReportClick: () -> Unit = {},
    onIncidentClick: (String) -> Unit = {},
) {
    val activeViewModel: ActiveIncidentViewModel = viewModel()
    val completedViewModel: CompletedIncidentViewModel = viewModel()
    val cancelledViewModel: CancelledIncidentViewModel = viewModel()

    val activeState by activeViewModel.uiState.collectAsStateWithLifecycle()
    val completedState by completedViewModel.uiState.collectAsStateWithLifecycle()
    val cancelledState by cancelledViewModel.uiState.collectAsStateWithLifecycle()

    var selectedTab by rememberSaveable { mutableStateOf(HomeTab.ACTIVE) }
    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(selectedTab, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            when (selectedTab) {
                HomeTab.ACTIVE -> {
                    completedViewModel.cancelLoading()
                    cancelledViewModel.cancelLoading()
                    activeViewModel.loadIncidents()
                }

                HomeTab.COMPLETED -> {
                    activeViewModel.cancelLoading()
                    cancelledViewModel.cancelLoading()
                    completedViewModel.loadIncidents()
                }

                HomeTab.CANCELLED -> {
                    activeViewModel.cancelLoading()
                    completedViewModel.cancelLoading()
                    cancelledViewModel.loadIncidents()
                }
            }
        }
    }

    LaunchedEffect(activeViewModel) {
        activeViewModel.events.collect { event ->
            when (event) {
                is ActiveIncidentUiEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }
    LaunchedEffect(completedViewModel) {
        completedViewModel.events.collect { event ->
            when (event) {
                is CompletedIncidentUiEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }
    LaunchedEffect(cancelledViewModel) {
        cancelledViewModel.events.collect { event ->
            when (event) {
                is CancelledIncidentUiEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    HomeScreenContent(
        selectedTab = selectedTab,
        activeState = activeState,
        completedState = completedState,
        cancelledState = cancelledState,
        snackbarHostState = snackbarHostState,
        onTabSelected = { selectedTab = it },
        onCreateReportClick = onCreateReportClick,
        onIncidentClick = onIncidentClick,
        onActiveRetry = activeViewModel::loadIncidents,
        onCompletedRetry = completedViewModel::loadIncidents,
        onCancelledRetry = cancelledViewModel::loadIncidents,
        onActiveCancel = { activeViewModel.cancelIncident(it.id) },
        onActiveRemove = { activeViewModel.deleteIncident(it.id) },
        onCompletedRemove = { completedViewModel.deleteIncident(it.id) },
        onCancelledRemove = { cancelledViewModel.deleteIncident(it.id) },
    )
}

@Composable
internal fun HomeScreenContent(
    selectedTab: HomeTab,
    activeState: ActiveIncidentUiState,
    completedState: CompletedIncidentUiState,
    cancelledState: CancelledIncidentUiState,
    onTabSelected: (HomeTab) -> Unit,
    onCreateReportClick: () -> Unit,
    onIncidentClick: (String) -> Unit,
    onActiveRetry: () -> Unit,
    onCompletedRetry: () -> Unit,
    onCancelledRetry: () -> Unit,
    onActiveCancel: (Incident) -> Unit,
    onActiveRemove: (Incident) -> Unit,
    onCompletedRemove: (Incident) -> Unit,
    onCancelledRemove: (Incident) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val showFab = when (selectedTab) {
        HomeTab.ACTIVE -> !activeState.isLoading &&
            activeState.loadError == null &&
            activeState.incidents.isNotEmpty()

        HomeTab.COMPLETED -> !completedState.isLoading &&
            completedState.loadError == null &&
            completedState.incidents.isNotEmpty()

        HomeTab.CANCELLED -> !cancelledState.isLoading &&
            cancelledState.loadError == null &&
            cancelledState.incidents.isNotEmpty()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = HomeBackground,
        contentWindowInsets = WindowInsets.navigationBars,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (showFab) {
                CreateReportFab(onClick = onCreateReportClick)
            }
        },
        floatingActionButtonPosition = FabPosition.End,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            HomeHeader()
            IncidentTabBar(
                selectedTab = selectedTab,
                activeCount = activeState.tabCount,
                completedCount = completedState.tabCount,
                cancelledCount = cancelledState.tabCount,
                onTabSelected = onTabSelected,
            )
            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    HomeTab.ACTIVE -> ActiveIncidentScreen(
                        uiState = activeState,
                        onRetry = onActiveRetry,
                        onCreateReport = onCreateReportClick,
                        onIncidentClick = { onIncidentClick(it.id) },
                        onCancelConfirmed = onActiveCancel,
                        onRemoveConfirmed = onActiveRemove,
                        modifier = Modifier.fillMaxSize(),
                    )

                    HomeTab.COMPLETED -> CompletedIncidentScreen(
                        uiState = completedState,
                        onRetry = onCompletedRetry,
                        onCreateReport = onCreateReportClick,
                        onIncidentClick = { onIncidentClick(it.id) },
                        onRemoveConfirmed = onCompletedRemove,
                        modifier = Modifier.fillMaxSize(),
                    )

                    HomeTab.CANCELLED -> CancelledIncidentScreen(
                        uiState = cancelledState,
                        onRetry = onCancelledRetry,
                        onCreateReport = onCreateReportClick,
                        onIncidentClick = { onIncidentClick(it.id) },
                        onRemoveConfirmed = onCancelledRemove,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(HomeNavy)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(HomeHeaderIconBackground),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_home_app_mark),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.home_title),
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.home_subtitle),
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 12.sp,
            )
        }
        IconButton(onClick = {}) {
            Icon(
                painter = painterResource(R.drawable.ic_home_search),
                contentDescription = stringResource(R.string.home_search),
                tint = Color.White,
            )
        }
    }
}

@Composable
private fun CreateReportFab(
    onClick: () -> Unit,
) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        icon = {
            Icon(
                painter = painterResource(R.drawable.ic_home_add),
                contentDescription = null,
            )
        },
        text = {
            Text(
                text = stringResource(R.string.home_create_report),
                fontWeight = FontWeight.SemiBold,
            )
        },
        containerColor = HomeFabBlue,
        contentColor = Color.White,
        shape = RoundedCornerShape(28.dp),
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
    )
}

@Preview(name = "Home - Active", showBackground = true, showSystemUi = true)
@Composable
private fun HomeActivePreview() {
    HomePreview(selectedTab = HomeTab.ACTIVE)
}

@Preview(name = "Home - Completed", showBackground = true, showSystemUi = true)
@Composable
private fun HomeCompletedPreview() {
    HomePreview(selectedTab = HomeTab.COMPLETED)
}

@Preview(name = "Home - Cancelled", showBackground = true, showSystemUi = true)
@Composable
private fun HomeCancelledPreview() {
    HomePreview(selectedTab = HomeTab.CANCELLED)
}

@Preview(name = "Home - Empty", showBackground = true, showSystemUi = true)
@Composable
private fun HomeEmptyPreview() {
    HomePreview(
        selectedTab = HomeTab.ACTIVE,
        activeState = IncidentPreviewData.activeEmptyState,
    )
}

@Preview(name = "Home - Loading", showBackground = true, showSystemUi = true)
@Composable
private fun HomeLoadingPreview() {
    HomePreview(
        selectedTab = HomeTab.ACTIVE,
        activeState = IncidentPreviewData.activeLoadingState,
    )
}

@Preview(name = "Home - Error", showBackground = true, showSystemUi = true)
@Composable
private fun HomeErrorPreview() {
    HomePreview(
        selectedTab = HomeTab.ACTIVE,
        activeState = IncidentPreviewData.activeErrorState,
    )
}

@Composable
private fun HomePreview(
    selectedTab: HomeTab,
    activeState: ActiveIncidentUiState = IncidentPreviewData.activeListState,
    completedState: CompletedIncidentUiState = IncidentPreviewData.completedListState,
    cancelledState: CancelledIncidentUiState = IncidentPreviewData.cancelledListState,
) {
    FieldIncidentReporterTheme(dynamicColor = false) {
        HomeScreenContent(
            selectedTab = selectedTab,
            activeState = activeState,
            completedState = completedState,
            cancelledState = cancelledState,
            onTabSelected = {},
            onCreateReportClick = {},
            onIncidentClick = {},
            onActiveRetry = {},
            onCompletedRetry = {},
            onCancelledRetry = {},
            onActiveCancel = {},
            onActiveRemove = {},
            onCompletedRemove = {},
            onCancelledRemove = {},
        )
    }
}
