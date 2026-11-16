package com.example.incidentreporter.presentation.incidentdetails

import androidx.lifecycle.viewModelScope
import com.example.incidentreporter.domain.model.Incident
import com.example.incidentreporter.domain.model.IncidentStatus
import com.example.incidentreporter.domain.model.UpdateIncidentStatusRequest
import com.example.incidentreporter.domain.usecase.CancelIncidentUseCase
import com.example.incidentreporter.domain.usecase.DeleteIncidentUseCase
import com.example.incidentreporter.domain.usecase.GetIncidentByIdUseCase
import com.example.incidentreporter.domain.usecase.UpdateIncidentStatusUseCase
import com.example.incidentreporter.presentation.base.BaseViewModel
import com.example.incidentreporter.presentation.incidentlist.toIncidentErrorMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class IncidentDetailsViewModel @Inject constructor(
    private val getIncidentByIdUseCase: GetIncidentByIdUseCase,
    private val updateIncidentStatusUseCase: UpdateIncidentStatusUseCase,
    private val cancelIncidentUseCase: CancelIncidentUseCase,
    private val deleteIncidentUseCase: DeleteIncidentUseCase,
) : BaseViewModel<IncidentDetailsUiState, IncidentDetailsUiEvent>(IncidentDetailsUiState()) {

    private var loadedIncidentId: String? = null
    private var loadJob: Job? = null
    private var actionJob: Job? = null

    fun loadIncident(incidentId: String) {
        if (incidentId.isBlank()) {
            updateState {
                it.copy(
                    isLoading = false,
                    incident = null,
                    errorMessage = "An unexpected error occurred.",
                )
            }
            return
        }
        if (loadJob?.isActive == true && loadedIncidentId == incidentId) {
            return
        }
        loadedIncidentId = incidentId
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            updateState {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                )
            }
            try {
                getIncidentByIdUseCase(incidentId)
                    .onSuccess { incident ->
                        updateState {
                            it.copy(
                                isLoading = false,
                                incident = incident,
                                errorMessage = null,
                            )
                        }
                    }
                    .onFailure { error ->
                        if (error is CancellationException) throw error
                        updateState {
                            it.copy(
                                isLoading = false,
                                errorMessage = error.toIncidentErrorMessage(),
                            )
                        }
                    }
            } catch (error: CancellationException) {
                throw error
            }
        }
    }

    fun retry() {
        val incidentId = loadedIncidentId ?: return
        loadIncident(incidentId)
    }

    fun updateStatus(status: IncidentStatus) {
        val incident = uiState.value.incident ?: return
        if (status !in incident.status.nextAllowedStatuses()) {
            return
        }
        startAction(markInProgress = { it.copy(isUpdatingStatus = true) }) {
            updateIncidentStatusUseCase(
                incidentId = incident.id,
                request = UpdateIncidentStatusRequest(status = status),
            )
                .onSuccess { updated ->
                    applySuccessfulMutation(updated)
                    emitEvent(IncidentDetailsUiEvent.ShowMessage(STATUS_UPDATED_MESSAGE))
                }
                .onFailure { error ->
                    handleActionFailure(error)
                }
        }
    }

    fun cancelIncident() {
        val incident = uiState.value.incident ?: return
        if (!incident.status.allowsLifecycleActions()) {
            return
        }
        startAction(markInProgress = { it.copy(isCancelling = true) }) {
            cancelIncidentUseCase(incident.id)
                .onSuccess { updated ->
                    applySuccessfulMutation(updated)
                    emitEvent(IncidentDetailsUiEvent.ShowMessage(REPORT_CANCELLED_MESSAGE))
                }
                .onFailure { error ->
                    handleActionFailure(error)
                }
        }
    }

    fun deleteIncident() {
        val incident = uiState.value.incident ?: return
        startAction(markInProgress = { it.copy(isDeleting = true) }) {
            deleteIncidentUseCase(incident.id)
                .onSuccess {
                    emitEvent(IncidentDetailsUiEvent.NavigateBack)
                }
                .onFailure { error ->
                    handleActionFailure(error)
                }
        }
    }

    private fun startAction(
        markInProgress: (IncidentDetailsUiState) -> IncidentDetailsUiState,
        action: suspend () -> Unit,
    ) {
        if (uiState.value.isActionInProgress) {
            return
        }
        if (actionJob?.isActive == true) {
            return
        }
        updateState(markInProgress)
        actionJob = viewModelScope.launch {
            try {
                action()
            } catch (error: CancellationException) {
                throw error
            } finally {
                actionJob = null
                updateState {
                    it.copy(
                        isUpdatingStatus = false,
                        isCancelling = false,
                        isDeleting = false,
                    )
                }
            }
        }
    }

    private fun applySuccessfulMutation(incident: Incident) {
        updateState {
            it.copy(incident = incident)
        }
    }

    private suspend fun handleActionFailure(error: Throwable) {
        if (error is CancellationException) throw error
        emitEvent(IncidentDetailsUiEvent.ShowMessage(error.toIncidentErrorMessage()))
    }

    private companion object {
        const val STATUS_UPDATED_MESSAGE = "Status updated."
        const val REPORT_CANCELLED_MESSAGE = "Report cancelled."
    }
}
