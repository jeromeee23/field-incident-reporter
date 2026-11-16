package com.example.incidentreporter.presentation.incidentlist.active

import androidx.lifecycle.viewModelScope
import com.example.incidentreporter.domain.model.IncidentStatus
import com.example.incidentreporter.domain.usecase.CancelIncidentUseCase
import com.example.incidentreporter.domain.usecase.DeleteIncidentUseCase
import com.example.incidentreporter.domain.usecase.GetIncidentsUseCase
import com.example.incidentreporter.presentation.base.BaseViewModel
import com.example.incidentreporter.presentation.incidentlist.toIncidentErrorMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class ActiveIncidentViewModel @Inject constructor(
    private val getIncidentsUseCase: GetIncidentsUseCase,
    private val cancelIncidentUseCase: CancelIncidentUseCase,
    private val deleteIncidentUseCase: DeleteIncidentUseCase,
) : BaseViewModel<ActiveIncidentUiState, ActiveIncidentUiEvent>(ActiveIncidentUiState()) {

    private var loadJob: Job? = null
    private val actionJobs = mutableMapOf<String, Job>()

    fun loadIncidents() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            updateState {
                it.copy(
                    isLoading = true,
                    incidents = emptyList(),
                    errorMessage = null,
                )
            }
            try {
                getIncidentsUseCase(ACTIVE_STATUSES)
                    .onSuccess { incidents ->
                        updateState {
                            it.copy(
                                isLoading = false,
                                incidents = incidents,
                                errorMessage = null,
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
                                errorMessage = error.toIncidentErrorMessage(),
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

    fun cancelIncident(incidentId: String) {
        startAction(incidentId) {
            cancelIncidentUseCase(incidentId)
        }
    }

    fun deleteIncident(incidentId: String) {
        startAction(incidentId) {
            deleteIncidentUseCase(incidentId)
        }
    }

    private fun startAction(
        incidentId: String,
        action: suspend () -> Result<*>,
    ) {
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
                action()
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
                            ActiveIncidentUiEvent.ShowMessage(error.toIncidentErrorMessage()),
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
        val ACTIVE_STATUSES = listOf(
            IncidentStatus.OPEN,
            IncidentStatus.IN_PROGRESS,
        )
    }
}

private fun applySuccessfulAction(
    state: ActiveIncidentUiState,
    incidentId: String,
): ActiveIncidentUiState {
    val remaining = state.incidents.filter { incident -> incident.id != incidentId }
    val hadIncident = remaining.size != state.incidents.size
    return state.copy(
        incidents = remaining,
        performingActionIncidentIds = state.performingActionIncidentIds - incidentId,
        tabCount = if (hadIncident) remaining.size else state.tabCount,
    )
}
