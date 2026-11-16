package com.example.incidentreporter.presentation.editincident

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.example.incidentreporter.domain.model.Incident
import com.example.incidentreporter.domain.model.IncidentCategory
import com.example.incidentreporter.domain.model.IncidentStatus
import com.example.incidentreporter.domain.model.Photo
import com.example.incidentreporter.domain.model.UpdateIncidentRequest
import com.example.incidentreporter.domain.usecase.DeletePhotoUseCase
import com.example.incidentreporter.domain.usecase.GetIncidentByIdUseCase
import com.example.incidentreporter.domain.usecase.UpdateIncidentUseCase
import com.example.incidentreporter.domain.usecase.UploadPhotosUseCase
import com.example.incidentreporter.presentation.base.BaseViewModel
import com.example.incidentreporter.presentation.createincident.IncidentPhotoManager
import com.example.incidentreporter.presentation.createincident.MAX_DESCRIPTION_LENGTH
import com.example.incidentreporter.presentation.createincident.MAX_PHOTOS
import com.example.incidentreporter.presentation.incidentlist.IncidentLoadError
import com.example.incidentreporter.presentation.incidentlist.toIncidentLoadError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class EditIncidentViewModel @Inject constructor(
    private val photoManager: IncidentPhotoManager,
    private val getIncidentByIdUseCase: GetIncidentByIdUseCase,
    private val uploadPhotosUseCase: UploadPhotosUseCase,
    private val updateIncidentUseCase: UpdateIncidentUseCase,
    private val deletePhotoUseCase: DeletePhotoUseCase,
) : BaseViewModel<EditIncidentUiState, EditIncidentUiEvent>(EditIncidentUiState()) {

    private val pendingRemovedPhotoIds = linkedSetOf<String>()
    private val pendingAbandonedUploadIds = linkedSetOf<String>()
    private var loadJob: Job? = null
    private var isLeaving = false

    fun loadIncident(incidentId: String) {
        if (incidentId.isBlank()) {
            updateState {
                it.copy(
                    isLoading = false,
                    loadError = IncidentLoadError.Unexpected,
                )
            }
            return
        }
        if (loadJob?.isActive == true && uiState.value.incidentId == incidentId) {
            return
        }
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            updateState {
                it.copy(
                    isLoading = true,
                    loadError = null,
                    incidentId = incidentId,
                )
            }
            try {
                getIncidentByIdUseCase(incidentId)
                    .onSuccess { incident ->
                        applyLoadedIncident(incident)
                    }
                    .onFailure { error ->
                        if (error is CancellationException) throw error
                        updateState {
                            it.copy(
                                isLoading = false,
                                loadError = error.toIncidentLoadError(),
                            )
                        }
                    }
            } catch (error: CancellationException) {
                throw error
            }
        }
    }

    fun retryLoad() {
        val incidentId = uiState.value.incidentId
        if (incidentId.isBlank()) {
            return
        }
        loadIncident(incidentId)
    }

    fun onTitleChanged(value: String) {
        updateState { it.copy(title = value, titleError = null, submitError = null) }
    }

    fun onCategoryChanged(category: IncidentCategory) {
        updateState {
            it.copy(
                category = category,
                categoryError = null,
                otherCategoryDescriptionError = if (category == IncidentCategory.OTHER) {
                    it.otherCategoryDescriptionError
                } else {
                    null
                },
                submitError = null,
            )
        }
    }

    fun onOtherCategoryDescriptionChanged(value: String) {
        updateState {
            it.copy(
                otherCategoryDescription = value,
                otherCategoryDescriptionError = null,
                submitError = null,
            )
        }
    }

    fun onDescriptionChanged(value: String) {
        val limited = value.take(MAX_DESCRIPTION_LENGTH)
        updateState {
            it.copy(
                description = limited,
                descriptionError = null,
                submitError = null,
            )
        }
    }

    fun onLocationChanged(value: String) {
        updateState { it.copy(location = value, locationError = null, submitError = null) }
    }

    fun onPhotosSelected(uris: List<Uri>) {
        if (uris.isEmpty() || uiState.value.photosLocked || uiState.value.isProcessingPhotos) {
            return
        }
        updateState { it.copy(isProcessingPhotos = true) }
        viewModelScope.launch {
            try {
                acceptPhotoBatch(uris)
            } catch (error: CancellationException) {
                throw error
            } finally {
                updateState { it.copy(isProcessingPhotos = false) }
            }
        }
    }

    fun onPhotoRemoved(item: EditPhotoItem) {
        if (uiState.value.isSubmitting) {
            return
        }
        when (item) {
            is EditPhotoItem.Existing -> {
                pendingRemovedPhotoIds += item.photo.id
                updateState {
                    it.copy(
                        existingPhotos = it.existingPhotos.filterNot { photo ->
                            photo.id == item.photo.id
                        },
                    )
                }
            }
            is EditPhotoItem.Local -> {
                if (uiState.value.photosLocked) {
                    return
                }
                photoManager.remove(item.uri)
                updateState {
                    it.copy(
                        newPhotoUris = it.newPhotoUris.filterNot { uri -> uri == item.uri },
                    )
                }
            }
            is EditPhotoItem.Uploaded -> {
                pendingAbandonedUploadIds += item.photo.id
                updateState {
                    val remaining = it.uploadedNewPhotos.filterNot { photo ->
                        photo.id == item.photo.id
                    }
                    it.copy(
                        uploadedNewPhotos = remaining,
                        photosLocked = remaining.isNotEmpty(),
                    )
                }
            }
        }
    }

    fun dismissSubmitError() {
        val hasUploadedNew = uiState.value.uploadedNewPhotos.isNotEmpty() ||
            pendingAbandonedUploadIds.isNotEmpty()
        if (hasUploadedNew) {
            onCancel()
        } else {
            updateState { it.copy(submitError = null) }
        }
    }

    fun onCancel() {
        if (uiState.value.isSubmitting || uiState.value.isProcessingPhotos || isLeaving) {
            return
        }
        isLeaving = true
        viewModelScope.launch {
            try {
                cleanupAbandonedUploads()
            } catch (error: CancellationException) {
                isLeaving = false
                throw error
            } catch (_: Exception) {
                // Ordinary cleanup failures must not block Cancel/Close.
            }
            photoManager.clear()
            emitEvent(EditIncidentUiEvent.NavigateBack)
        }
    }

    fun save() {
        val snapshot = uiState.value
        if (snapshot.isSubmitting || snapshot.isProcessingPhotos || snapshot.isLoading) {
            return
        }
        val errors = validateForm(snapshot)
        if (!errors.isValid) {
            updateState {
                it.copy(
                    titleError = errors.titleError,
                    categoryError = errors.categoryError,
                    otherCategoryDescriptionError = errors.otherCategoryDescriptionError,
                    descriptionError = errors.descriptionError,
                    locationError = errors.locationError,
                )
            }
            return
        }
        updateState { it.copy(isSubmitting = true, submitError = null) }
        viewModelScope.launch {
            try {
                val uploadedNew = ensurePhotosUploaded() ?: return@launch
                val form = uiState.value
                val request = UpdateIncidentRequest(
                    title = form.title.trim(),
                    description = form.description.trim(),
                    category = requireNotNull(form.category),
                    otherCategoryDescription = form.otherCategoryDescription
                        .trim()
                        .takeIf { form.category == IncidentCategory.OTHER && it.isNotEmpty() },
                    location = form.location.trim(),
                    photos = form.existingPhotos + uploadedNew,
                )
                updateIncidentUseCase(form.incidentId, request)
                    .onSuccess {
                        try {
                            cleanupRemovedPhotos()
                        } catch (error: CancellationException) {
                            throw error
                        } catch (_: Exception) {
                            // Best-effort cleanup must not convert a successful update into a save failure.
                        }
                        photoManager.clear()
                        emitEvent(EditIncidentUiEvent.NavigateBack)
                    }
                    .onFailure { error ->
                        if (error is CancellationException) throw error
                        updateState {
                            it.copy(
                                isSubmitting = false,
                                submitError = error.toUserMessage(),
                            )
                        }
                    }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                updateState {
                    it.copy(
                        isSubmitting = false,
                        submitError = error.toUserMessage(),
                    )
                }
            }
        }
    }

    override fun onCleared() {
        photoManager.clear()
        super.onCleared()
    }

    private fun applyLoadedIncident(incident: Incident) {
        if (!incident.status.isEditable()) {
            updateState {
                it.copy(
                    isLoading = false,
                    loadError = IncidentLoadError.Api(NOT_EDITABLE_MESSAGE),
                )
            }
            return
        }
        pendingRemovedPhotoIds.clear()
        pendingAbandonedUploadIds.clear()
        photoManager.clear()
        updateState {
            it.copy(
                isLoading = false,
                loadError = null,
                incidentId = incident.id,
                title = incident.title,
                category = incident.category,
                otherCategoryDescription = incident.otherCategoryDescription.orEmpty(),
                description = incident.description,
                location = incident.location,
                existingPhotos = incident.photos,
                newPhotoUris = emptyList(),
                uploadedNewPhotos = emptyList(),
                photosLocked = false,
                titleError = null,
                categoryError = null,
                otherCategoryDescriptionError = null,
                descriptionError = null,
                locationError = null,
                isProcessingPhotos = false,
                isSubmitting = false,
                submitError = null,
            )
        }
    }

    private suspend fun acceptPhotoBatch(uris: List<Uri>) {
        val state = uiState.value
        val currentCount = state.existingPhotos.size + state.newPhotoUris.size
        if (currentCount + uris.size > MAX_PHOTOS) {
            emitEvent(
                EditIncidentUiEvent.ShowPhotoError(
                    "A maximum of $MAX_PHOTOS photos can be attached. The selected photos were not added.",
                ),
            )
            return
        }
        photoManager.copyAndValidate(uris)
            .onSuccess { copiedUris ->
                updateState { it.copy(newPhotoUris = it.newPhotoUris + copiedUris) }
            }
            .onFailure { error ->
                if (error is CancellationException) throw error
                emitEvent(EditIncidentUiEvent.ShowPhotoError(error.toUserMessage()))
            }
    }

    private suspend fun ensurePhotosUploaded(): List<Photo>? {
        val state = uiState.value
        if (state.photosLocked) {
            return state.uploadedNewPhotos
        }
        if (state.newPhotoUris.isEmpty()) {
            return emptyList()
        }
        val files = photoManager.filesFor(state.newPhotoUris)
        if (files == null) {
            updateState {
                it.copy(
                    isSubmitting = false,
                    submitError = "Unable to read the selected photos. Please add them again.",
                )
            }
            return null
        }
        return uploadPhotosUseCase(state.incidentId, files).fold(
            onSuccess = { photos ->
                if (photos.isEmpty()) {
                    updateState {
                        it.copy(
                            isSubmitting = false,
                            submitError = "Photo upload did not return any photos. Please try again.",
                            photosLocked = false,
                        )
                    }
                    null
                } else {
                    photoManager.clear()
                    updateState {
                        it.copy(
                            uploadedNewPhotos = photos,
                            newPhotoUris = emptyList(),
                            photosLocked = true,
                        )
                    }
                    photos
                }
            },
            onFailure = { error ->
                if (error is CancellationException) throw error
                updateState {
                    it.copy(
                        isSubmitting = false,
                        submitError = error.toUserMessage(),
                        uploadedNewPhotos = emptyList(),
                        photosLocked = false,
                    )
                }
                null
            },
        )
    }

    private suspend fun cleanupRemovedPhotos() {
        deletePhotosBestEffort(
            pendingRemovedPhotoIds.toList() + pendingAbandonedUploadIds.toList(),
        )
    }

    private suspend fun cleanupAbandonedUploads() {
        val ids = uiState.value.uploadedNewPhotos.map { it.id } + pendingAbandonedUploadIds
        deletePhotosBestEffort(ids)
    }

    private suspend fun deletePhotosBestEffort(photoIds: Collection<String>) {
        photoIds.forEach { photoId ->
            try {
                deletePhotoUseCase(photoId).onFailure { error ->
                    if (error is CancellationException) throw error
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                // Continue with remaining IDs. Cleanup is best-effort.
            }
        }
    }

    private fun validateForm(state: EditIncidentUiState): FormErrors {
        return FormErrors(
            titleError = if (state.title.trim().isBlank()) "Title is required." else null,
            categoryError = if (state.category == null) "Please select a category." else null,
            otherCategoryDescriptionError = if (
                state.category == IncidentCategory.OTHER &&
                state.otherCategoryDescription.trim().isBlank()
            ) {
                "Please describe the category."
            } else {
                null
            },
            descriptionError = when {
                state.description.trim().isBlank() -> "Description is required."
                state.description.length > MAX_DESCRIPTION_LENGTH ->
                    "Description must be 500 characters or fewer."
                else -> null
            },
            locationError = if (state.location.trim().isBlank()) "Location is required." else null,
        )
    }

    private fun Throwable.toUserMessage(): String {
        return message?.takeIf { it.isNotBlank() } ?: "An unexpected error occurred."
    }

    private fun IncidentStatus.isEditable(): Boolean {
        return this == IncidentStatus.OPEN || this == IncidentStatus.IN_PROGRESS
    }

    private data class FormErrors(
        val titleError: String?,
        val categoryError: String?,
        val otherCategoryDescriptionError: String?,
        val descriptionError: String?,
        val locationError: String?,
    ) {
        val isValid: Boolean
            get() = titleError == null &&
                categoryError == null &&
                otherCategoryDescriptionError == null &&
                descriptionError == null &&
                locationError == null
    }

    private companion object {
        const val NOT_EDITABLE_MESSAGE = "This incident can no longer be edited."
    }
}
