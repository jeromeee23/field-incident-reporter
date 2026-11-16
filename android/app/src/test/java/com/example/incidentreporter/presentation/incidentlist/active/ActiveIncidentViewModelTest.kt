package com.example.incidentreporter.presentation.incidentlist.active

import com.example.incidentreporter.domain.model.IncidentStatus
import com.example.incidentreporter.domain.usecase.CancelIncidentUseCase
import com.example.incidentreporter.domain.usecase.DeleteIncidentUseCase
import com.example.incidentreporter.domain.usecase.GetIncidentsUseCase
import com.example.incidentreporter.presentation.incidentlist.IncidentLoadError
import com.example.incidentreporter.testing.FakeIncidentRepository
import com.example.incidentreporter.testing.IncidentFixtures
import com.example.incidentreporter.testing.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ActiveIncidentViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun initialState_isIdleAndEmpty() {
        val viewModel = createViewModel(FakeIncidentRepository())

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.incidents.isEmpty())
        assertNull(state.loadError)
        assertEquals(0, state.tabCount)
    }

    @Test
    fun loadIncidents_successShowsActiveIncidentsAndTabCount() = runViewModelTest {
        val repository = FakeIncidentRepository()
        val open = IncidentFixtures.incident(id = "open-1", status = IncidentStatus.OPEN)
        val inProgress = IncidentFixtures.incident(
            id = "in-progress-1",
            status = IncidentStatus.IN_PROGRESS,
        )
        val completed = IncidentFixtures.incident(
            id = "completed-1",
            status = IncidentStatus.COMPLETED,
        )
        repository.incidents[open.id] = open
        repository.incidents[inProgress.id] = inProgress
        repository.incidents[completed.id] = completed
        val viewModel = createViewModel(repository)

        viewModel.loadIncidents()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(setOf(open, inProgress), state.incidents.toSet())
        assertNull(state.loadError)
        assertEquals(2, state.tabCount)
        assertEquals(
            listOf(IncidentStatus.OPEN, IncidentStatus.IN_PROGRESS),
            repository.lastGetIncidentsStatuses,
        )
    }

    @Test
    fun loadIncidents_emptySuccessClearsErrorAndTabCount() = runViewModelTest {
        val repository = FakeIncidentRepository()
        val viewModel = createViewModel(repository)

        viewModel.loadIncidents()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.incidents.isEmpty())
        assertNull(state.loadError)
        assertEquals(0, state.tabCount)
    }

    @Test
    fun loadIncidents_failureSetsLoadErrorAndClearsList() = runViewModelTest {
        val repository = FakeIncidentRepository()
        repository.incidents["open-1"] = IncidentFixtures.incident()
        repository.getIncidentsResult = Result.failure(IllegalStateException("Network down"))
        val viewModel = createViewModel(repository)

        viewModel.loadIncidents()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.incidents.isEmpty())
        assertEquals(IncidentLoadError.Unexpected, state.loadError)
    }

    @Test
    fun loadIncidents_clearsExistingContentWhileLoading() = runViewModelTest {
        val repository = FakeIncidentRepository()
        val incident = IncidentFixtures.incident()
        repository.incidents[incident.id] = incident
        val viewModel = createViewModel(repository)
        viewModel.loadIncidents()
        advanceUntilIdle()

        repository.getIncidentsGate = CompletableDeferred()
        viewModel.loadIncidents()
        runCurrent()

        val loadingState = viewModel.uiState.value
        assertTrue(loadingState.isLoading)
        assertTrue(loadingState.incidents.isEmpty())
        assertNull(loadingState.loadError)

        repository.getIncidentsGate!!.complete(Unit)
        advanceUntilIdle()
        assertEquals(listOf(incident), viewModel.uiState.value.incidents)
    }

    @Test
    fun cancelIncident_successRemovesRowAndUpdatesTabCount() = runViewModelTest {
        val repository = FakeIncidentRepository()
        val first = IncidentFixtures.incident(id = "open-1")
        val second = IncidentFixtures.incident(id = "open-2")
        repository.incidents[first.id] = first
        repository.incidents[second.id] = second
        val viewModel = createViewModel(repository)
        viewModel.loadIncidents()
        advanceUntilIdle()

        viewModel.cancelIncident(first.id)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf(second), state.incidents)
        assertEquals(1, state.tabCount)
        assertTrue(state.performingActionIncidentIds.isEmpty())
        assertEquals(IncidentStatus.CANCELLED, repository.incidents[first.id]?.status)
    }

    @Test
    fun deleteIncident_successRemovesRowAndUpdatesTabCount() = runViewModelTest {
        val repository = FakeIncidentRepository()
        val incident = IncidentFixtures.incident()
        repository.incidents[incident.id] = incident
        val viewModel = createViewModel(repository)
        viewModel.loadIncidents()
        advanceUntilIdle()

        viewModel.deleteIncident(incident.id)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.incidents.isEmpty())
        assertEquals(0, state.tabCount)
        assertEquals(1, repository.deleteCalls)
        assertFalse(repository.incidents.containsKey(incident.id))
    }

    @Test
    fun cancelIncident_duplicateActionIsGuarded() = runViewModelTest {
        val repository = FakeIncidentRepository()
        val incident = IncidentFixtures.incident()
        repository.incidents[incident.id] = incident
        val viewModel = createViewModel(repository)
        viewModel.loadIncidents()
        advanceUntilIdle()

        viewModel.cancelIncident(incident.id)
        viewModel.cancelIncident(incident.id)
        advanceUntilIdle()

        assertEquals(1, repository.updateStatusCalls)
    }

    @Test
    fun cancelIncident_failureKeepsRowAndShowsMessage() = runViewModelTest {
        val repository = FakeIncidentRepository()
        val incident = IncidentFixtures.incident()
        repository.incidents[incident.id] = incident
        repository.updateStatusResult = Result.failure(IllegalStateException("Cancel failed"))
        val viewModel = createViewModel(repository)
        val events = collectEvents(viewModel)
        viewModel.loadIncidents()
        advanceUntilIdle()

        viewModel.cancelIncident(incident.id)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf(incident), state.incidents)
        assertEquals(1, state.tabCount)
        assertTrue(state.performingActionIncidentIds.isEmpty())
        assertEquals(listOf(ActiveIncidentUiEvent.ShowMessage("Cancel failed")), events)
    }

    @Test
    fun cancelLoading_stopsInFlightLoadWithoutMappingError() = runViewModelTest {
        val repository = FakeIncidentRepository()
        repository.getIncidentsGate = CompletableDeferred()
        val viewModel = createViewModel(repository)

        viewModel.loadIncidents()
        runCurrent()
        viewModel.cancelLoading()
        repository.getIncidentsGate!!.complete(Unit)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isLoading)
        assertTrue(state.incidents.isEmpty())
        assertNull(state.loadError)
        assertEquals(1, repository.getIncidentsCalls)
    }

    private fun createViewModel(
        repository: FakeIncidentRepository,
    ): ActiveIncidentViewModel {
        return ActiveIncidentViewModel(
            getIncidentsUseCase = GetIncidentsUseCase(repository),
            cancelIncidentUseCase = CancelIncidentUseCase(repository),
            deleteIncidentUseCase = DeleteIncidentUseCase(repository),
        )
    }

    private fun TestScope.collectEvents(
        viewModel: ActiveIncidentViewModel,
    ): MutableList<ActiveIncidentUiEvent> {
        val events = mutableListOf<ActiveIncidentUiEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.collect { events.add(it) }
        }
        return events
    }

    private fun runViewModelTest(
        body: suspend TestScope.() -> Unit,
    ) = runTest(mainDispatcherRule.dispatcher, testBody = body)
}
