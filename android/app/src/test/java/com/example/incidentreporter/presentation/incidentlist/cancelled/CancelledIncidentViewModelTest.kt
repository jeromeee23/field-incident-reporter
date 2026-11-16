package com.example.incidentreporter.presentation.incidentlist.cancelled

import com.example.incidentreporter.domain.model.IncidentStatus
import com.example.incidentreporter.domain.usecase.DeleteIncidentUseCase
import com.example.incidentreporter.domain.usecase.GetIncidentsUseCase
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
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CancelledIncidentViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun loadIncidents_requestsCancelledStatusOnly() = runViewModelTest {
        val repository = FakeIncidentRepository()
        val cancelled = IncidentFixtures.incident(
            id = "cancelled-1",
            status = IncidentStatus.CANCELLED,
        )
        val open = IncidentFixtures.incident(id = "open-1")
        repository.incidents[cancelled.id] = cancelled
        repository.incidents[open.id] = open
        val viewModel = createViewModel(repository)

        viewModel.loadIncidents()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf(IncidentStatus.CANCELLED), repository.lastGetIncidentsStatuses)
        assertEquals(listOf(cancelled), state.incidents)
        assertEquals(1, state.tabCount)
    }

    @Test
    fun loadIncidents_emptySuccess() = runViewModelTest {
        val viewModel = createViewModel(FakeIncidentRepository())

        viewModel.loadIncidents()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.incidents.isEmpty())
        assertEquals(0, viewModel.uiState.value.tabCount)
    }

    @Test
    fun loadIncidents_failureSetsLoadError() = runViewModelTest {
        val repository = FakeIncidentRepository()
        repository.getIncidentsResult = Result.failure(IllegalStateException("Network down"))
        val viewModel = createViewModel(repository)

        viewModel.loadIncidents()
        advanceUntilIdle()

        assertEquals(IncidentLoadError.Unexpected, viewModel.uiState.value.loadError)
    }

    @Test
    fun deleteIncident_successRemovesRow() = runViewModelTest {
        val repository = FakeIncidentRepository()
        val incident = IncidentFixtures.incident(
            id = "cancelled-1",
            status = IncidentStatus.CANCELLED,
        )
        repository.incidents[incident.id] = incident
        val viewModel = createViewModel(repository)
        viewModel.loadIncidents()
        advanceUntilIdle()

        viewModel.deleteIncident(incident.id)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.incidents.isEmpty())
        assertEquals(1, repository.deleteCalls)
    }

    @Test
    fun deleteIncident_failureKeepsRowAndShowsMessage() = runViewModelTest {
        val repository = FakeIncidentRepository()
        val incident = IncidentFixtures.incident(
            id = "cancelled-1",
            status = IncidentStatus.CANCELLED,
        )
        repository.incidents[incident.id] = incident
        repository.deleteResult = Result.failure(IllegalStateException("Delete failed"))
        val viewModel = createViewModel(repository)
        val events = collectEvents(viewModel)
        viewModel.loadIncidents()
        advanceUntilIdle()

        viewModel.deleteIncident(incident.id)
        advanceUntilIdle()

        assertEquals(listOf(incident), viewModel.uiState.value.incidents)
        assertEquals(listOf(CancelledIncidentUiEvent.ShowMessage("Delete failed")), events)
    }

    private fun createViewModel(
        repository: FakeIncidentRepository,
    ): CancelledIncidentViewModel {
        return CancelledIncidentViewModel(
            getIncidentsUseCase = GetIncidentsUseCase(repository),
            deleteIncidentUseCase = DeleteIncidentUseCase(repository),
        )
    }

    private fun TestScope.collectEvents(
        viewModel: CancelledIncidentViewModel,
    ): MutableList<CancelledIncidentUiEvent> {
        val events = mutableListOf<CancelledIncidentUiEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.collect { events.add(it) }
        }
        return events
    }

    private fun runViewModelTest(
        body: suspend TestScope.() -> Unit,
    ) = runTest(mainDispatcherRule.dispatcher, testBody = body)
}
