package com.example.incidentreporter.presentation.incidentdetails

import com.example.incidentreporter.domain.model.IncidentStatus
import com.example.incidentreporter.domain.usecase.CancelIncidentUseCase
import com.example.incidentreporter.domain.usecase.DeleteIncidentUseCase
import com.example.incidentreporter.domain.usecase.GetIncidentByIdUseCase
import com.example.incidentreporter.domain.usecase.UpdateIncidentStatusUseCase
import com.example.incidentreporter.presentation.incidentlist.IncidentLoadError
import com.example.incidentreporter.testing.FakeIncidentRepository
import com.example.incidentreporter.testing.IncidentFixtures
import com.example.incidentreporter.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalCoroutinesApi::class)
class IncidentDetailsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun loadIncident_successReplacesContent() = runDetailsTest {
        val repository = FakeIncidentRepository()
        val incident = IncidentFixtures.incident()
        repository.incidents[incident.id] = incident
        val viewModel = createViewModel(repository)

        viewModel.loadIncident(incident.id)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(incident, state.incident)
        assertNull(state.loadError)
    }

    @Test
    fun loadIncident_errorShowsMessageAndAllowsRetry() = runDetailsTest {
        val repository = FakeIncidentRepository()
        val incident = IncidentFixtures.incident()
        repository.getIncidentResult = Result.failure(IllegalStateException("Network down"))
        val viewModel = createViewModel(repository)

        viewModel.loadIncident(incident.id)
        advanceUntilIdle()

        val errorState = viewModel.uiState.value
        assertFalse(errorState.isLoading)
        assertNull(errorState.incident)
        assertEquals(IncidentLoadError.Unexpected, errorState.loadError)

        repository.getIncidentResult = null
        repository.incidents[incident.id] = incident
        viewModel.retry()
        advanceUntilIdle()

        val retryState = viewModel.uiState.value
        assertFalse(retryState.isLoading)
        assertEquals(incident, retryState.incident)
        assertNull(retryState.loadError)
        assertEquals(2, repository.getIncidentCalls)
    }

    @Test
    fun loadIncident_skipsDuplicateInFlightLoad() = runDetailsTest {
        val repository = FakeIncidentRepository()
        val incident = IncidentFixtures.incident()
        repository.incidents[incident.id] = incident
        val viewModel = createViewModel(repository)

        viewModel.loadIncident(incident.id)
        viewModel.loadIncident(incident.id)
        advanceUntilIdle()

        assertEquals(1, repository.getIncidentCalls)
        assertEquals(incident, viewModel.uiState.value.incident)
    }

    @Test
    fun updateStatus_illegalTransitionIsNoOp() = runDetailsTest {
        val repository = FakeIncidentRepository()
        val incident = IncidentFixtures.incident(status = IncidentStatus.OPEN)
        repository.incidents[incident.id] = incident
        val viewModel = createViewModel(repository)
        viewModel.loadIncident(incident.id)
        advanceUntilIdle()

        viewModel.updateStatus(IncidentStatus.COMPLETED)
        viewModel.updateStatus(IncidentStatus.OPEN)
        advanceUntilIdle()

        assertEquals(0, repository.updateStatusCalls)
        assertEquals(incident, viewModel.uiState.value.incident)
        assertFalse(viewModel.uiState.value.isActionInProgress)
    }

    @Test
    fun updateStatus_duplicateActionIsGuarded() = runDetailsTest {
        val repository = FakeIncidentRepository()
        val incident = IncidentFixtures.incident(status = IncidentStatus.OPEN)
        repository.incidents[incident.id] = incident
        val viewModel = createViewModel(repository)
        viewModel.loadIncident(incident.id)
        advanceUntilIdle()

        viewModel.updateStatus(IncidentStatus.IN_PROGRESS)
        viewModel.updateStatus(IncidentStatus.IN_PROGRESS)
        advanceUntilIdle()

        assertEquals(1, repository.updateStatusCalls)
        assertEquals(IncidentStatus.IN_PROGRESS, viewModel.uiState.value.incident?.status)
    }

    @Test
    fun loadIncident_cancellationExceptionIsRethrownAndNotMappedToError() = runDetailsTest {
        val repository = FakeIncidentRepository()
        repository.getIncidentException = CancellationException("cancelled")
        val viewModel = createViewModel(repository)

        viewModel.loadIncident("incident-1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isLoading)
        assertNull(state.incident)
        assertNull(state.loadError)
    }

    @Test
    fun updateStatus_successReplacesIncidentAndShowsMessage() = runDetailsTest {
        val repository = FakeIncidentRepository()
        val incident = IncidentFixtures.incident(status = IncidentStatus.OPEN)
        repository.incidents[incident.id] = incident
        val viewModel = createViewModel(repository)
        val events = collectEvents(viewModel)
        viewModel.loadIncident(incident.id)
        advanceUntilIdle()

        viewModel.updateStatus(IncidentStatus.IN_PROGRESS)
        advanceUntilIdle()

        assertEquals(IncidentStatus.IN_PROGRESS, viewModel.uiState.value.incident?.status)
        assertEquals(
            listOf(IncidentDetailsUiEvent.ShowMessage("Status updated.")),
            events,
        )
        assertFalse(viewModel.uiState.value.isUpdatingStatus)
    }

    @Test
    fun cancelIncident_successUpdatesIncidentAndShowsMessage() = runDetailsTest {
        val repository = FakeIncidentRepository()
        val incident = IncidentFixtures.incident(status = IncidentStatus.IN_PROGRESS)
        repository.incidents[incident.id] = incident
        val viewModel = createViewModel(repository)
        val events = collectEvents(viewModel)
        viewModel.loadIncident(incident.id)
        advanceUntilIdle()

        viewModel.cancelIncident()
        advanceUntilIdle()

        assertEquals(IncidentStatus.CANCELLED, viewModel.uiState.value.incident?.status)
        assertEquals(
            listOf(IncidentDetailsUiEvent.ShowMessage("Report cancelled.")),
            events,
        )
    }

    @Test
    fun actionFailure_preservesIncidentAndShowsMessage() = runDetailsTest {
        val repository = FakeIncidentRepository()
        val incident = IncidentFixtures.incident(status = IncidentStatus.OPEN)
        repository.incidents[incident.id] = incident
        repository.updateStatusResult = Result.failure(IllegalStateException("Update failed"))
        val viewModel = createViewModel(repository)
        val events = collectEvents(viewModel)
        viewModel.loadIncident(incident.id)
        advanceUntilIdle()

        viewModel.updateStatus(IncidentStatus.IN_PROGRESS)
        advanceUntilIdle()

        assertEquals(incident, viewModel.uiState.value.incident)
        assertEquals(
            listOf(IncidentDetailsUiEvent.ShowMessage("Update failed")),
            events,
        )
        assertFalse(viewModel.uiState.value.isActionInProgress)
    }

    @Test
    fun deleteIncident_successEmitsNavigateBack() = runDetailsTest {
        val repository = FakeIncidentRepository()
        val incident = IncidentFixtures.incident()
        repository.incidents[incident.id] = incident
        val viewModel = createViewModel(repository)
        val events = collectEvents(viewModel)
        viewModel.loadIncident(incident.id)
        advanceUntilIdle()

        viewModel.deleteIncident()
        advanceUntilIdle()

        assertEquals(listOf(IncidentDetailsUiEvent.NavigateBack), events)
        assertEquals(1, repository.deleteCalls)
    }

    private fun TestScope.collectEvents(
        viewModel: IncidentDetailsViewModel,
    ): MutableList<IncidentDetailsUiEvent> {
        val events = mutableListOf<IncidentDetailsUiEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.collect { events.add(it) }
        }
        return events
    }

    private fun createViewModel(
        repository: FakeIncidentRepository,
    ): IncidentDetailsViewModel {
        return IncidentDetailsViewModel(
            getIncidentByIdUseCase = GetIncidentByIdUseCase(repository),
            updateIncidentStatusUseCase = UpdateIncidentStatusUseCase(repository),
            cancelIncidentUseCase = CancelIncidentUseCase(repository),
            deleteIncidentUseCase = DeleteIncidentUseCase(repository),
        )
    }

    private fun runDetailsTest(
        body: suspend TestScope.() -> Unit,
    ) = runTest(mainDispatcherRule.dispatcher, testBody = body)
}
