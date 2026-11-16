package com.example.incidentreporter.presentation.incidentlist.cancelled

import androidx.lifecycle.viewModelScope
import com.example.incidentreporter.domain.model.IncidentStatus
import com.example.incidentreporter.domain.usecase.DeleteIncidentUseCase
import com.example.incidentreporter.domain.usecase.GetIncidentsUseCase
import com.example.incidentreporter.presentation.base.BaseViewModel
import com.example.incidentreporter.presentation.incidentlist.toIncidentErrorMessage
import com.example.incidentreporter.presentation.incidentlist.toIncidentLoadError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class CancelledIncidentViewModel @Inject constructor(
    private val getIncidentsUseCase: GetIncidentsUseCase,
    private val deleteIncidentUseCase: DeleteIncidentUseCase,
) : BaseViewModel<CancelledIncidentUiState, CancelledIncidentUiEvent>(CancelledIncidentUiState()) {

    private var loadJob: Job? = null
    private val actionJobs = mutableMapOf<String, Job>()

    fun loadIncidents() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            updateState {
                it.copy(
                    isLoading = true,
                    incidents = emptyList(),
                    loadError = null,
                )
            }
            try {
                getIncidentsUseCase(CANCELLED_STATUSES)
                    .onSuccess { incidents ->
                        updateState {
                            it.copy(
                                isLoading = false,
                                incidents = incidents,
                                loadError = null,
                                tabCount = incidents.size,
                            )
                        }
                    }
                    .onFailure { error ->
                        if (error is CancellationException) throw error
                        updateState {
                            it.copy(
                                isLoading = false,
                                incidents = emptyList(),
                                loadError = error.toIncidentLoadError(),
                            )
                        }
                    }
            } catch (error: CancellationException) {
                throw error
            }
        }
    }

    fun cancelLoading() {
        loadJob?.cancel()
        loadJob = null
    }

    fun deleteIncident(incidentId: String) {
        if (incidentId in uiState.value.performingActionIncidentIds) {
            return
        }
        if (actionJobs[incidentId]?.isActive == true) {
            return
        }
        updateState {
            it.copy(performingActionIncidentIds = it.performingActionIncidentIds + incidentId)
        }
        actionJobs[incidentId] = viewModelScope.launch {
            try {
                deleteIncidentUseCase(incidentId)
                    .onSuccess {
                        updateState { state ->
                            applySuccessfulAction(state, incidentId)
                        }
                    }
                    .onFailure { error ->
                        if (error is CancellationException) throw error
                        updateState { state ->
                            state.copy(
                                performingActionIncidentIds = state.performingActionIncidentIds - incidentId,
                            )
                        }
                        emitEvent(
                            CancelledIncidentUiEvent.ShowMessage(error.toIncidentErrorMessage()),
                        )
                    }
            } catch (error: CancellationException) {
                throw error
            } finally {
                actionJobs.remove(incidentId)
            }
        }
    }

    private companion object {
        val CANCELLED_STATUSES = listOf(IncidentStatus.CANCELLED)
    }
}

private fun applySuccessfulAction(
    state: CancelledIncidentUiState,
    incidentId: String,
): CancelledIncidentUiState {
    val remaining = state.incidents.filter { incident -> incident.id != incidentId }
    val hadIncident = remaining.size != state.incidents.size
    return state.copy(
        incidents = remaining,
        performingActionIncidentIds = state.performingActionIncidentIds - incidentId,
        tabCount = if (hadIncident) remaining.size else state.tabCount,
    )
}
