package com.example.incidentreporter.presentation.createincident

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.example.incidentreporter.domain.model.CreateIncidentRequest
import com.example.incidentreporter.domain.model.IncidentCategory
import com.example.incidentreporter.domain.model.Photo
import com.example.incidentreporter.domain.usecase.CreateIncidentUseCase
import com.example.incidentreporter.domain.usecase.UploadPhotosUseCase
import com.example.incidentreporter.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class CreateIncidentViewModel @Inject constructor(
    private val photoManager: IncidentPhotoManager,
    private val createIncidentUseCase: CreateIncidentUseCase,
    private val uploadPhotosUseCase: UploadPhotosUseCase,
) : BaseViewModel<CreateIncidentUiState, CreateIncidentUiEvent>(CreateIncidentUiState()) {

    private var pendingIncidentId: String = UUID.randomUUID().toString()

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

    fun onPhotoRemoved(uri: Uri) {
        if (uiState.value.photosLocked || uiState.value.isSubmitting) {
            return
        }
        photoManager.remove(uri)
        updateState {
            it.copy(selectedPhotoUris = it.selectedPhotoUris.filterNot { selected -> selected == uri })
        }
    }

    fun dismissSubmitError() {
        updateState { it.copy(submitError = null) }
    }

    fun submit() {
        val snapshot = uiState.value
        if (snapshot.isSubmitting || snapshot.isProcessingPhotos) {
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
                val photos = ensurePhotosUploaded() ?: return@launch
                val form = uiState.value
                val request = CreateIncidentRequest(
                    id = pendingIncidentId,
                    title = form.title.trim(),
                    description = form.description.trim(),
                    category = requireNotNull(form.category),
                    otherCategoryDescription = form.otherCategoryDescription
                        .trim()
                        .takeIf { form.category == IncidentCategory.OTHER && it.isNotEmpty() },
                    location = form.location.trim(),
                    photos = photos,
                )
                createIncidentUseCase(request)
                    .onSuccess { incident ->
                        emitEvent(CreateIncidentUiEvent.NavigateToIncidentCreated(incident.id))
                        resetAfterSuccess()
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
            }
        }
    }

    override fun onCleared() {
        photoManager.clear()
        super.onCleared()
    }

    private suspend fun acceptPhotoBatch(uris: List<Uri>) {
        val currentCount = uiState.value.selectedPhotoUris.size
        if (currentCount + uris.size > MAX_PHOTOS) {
            emitEvent(
                CreateIncidentUiEvent.ShowPhotoError(
                    "A maximum of $MAX_PHOTOS photos can be attached. The selected photos were not added.",
                ),
            )
            return
        }
        photoManager.copyAndValidate(uris)
            .onSuccess { copiedUris ->
                updateState { it.copy(selectedPhotoUris = it.selectedPhotoUris + copiedUris) }
            }
            .onFailure { error ->
                if (error is CancellationException) throw error
                emitEvent(CreateIncidentUiEvent.ShowPhotoError(error.toUserMessage()))
            }
    }

    private suspend fun ensurePhotosUploaded(): List<Photo>? {
        val state = uiState.value
        if (state.photosLocked) {
            return state.uploadedPhotos
        }
        if (state.selectedPhotoUris.isEmpty()) {
            return emptyList()
        }
        val files = photoManager.filesFor(state.selectedPhotoUris)
        if (files == null) {
            updateState {
                it.copy(
                    isSubmitting = false,
                    submitError = "Unable to read the selected photos. Please add them again.",
                )
            }
            return null
        }
        return uploadPhotosUseCase(pendingIncidentId, files).fold(
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
                    updateState { it.copy(uploadedPhotos = photos, photosLocked = true) }
                    photos
                }
            },
            onFailure = { error ->
                if (error is CancellationException) throw error
                updateState {
                    it.copy(
                        isSubmitting = false,
                        submitError = error.toUserMessage(),
                        uploadedPhotos = emptyList(),
                        photosLocked = false,
                    )
                }
                null
            },
        )
    }

    private fun resetAfterSuccess() {
        photoManager.clear()
        pendingIncidentId = UUID.randomUUID().toString()
        updateState { CreateIncidentUiState() }
    }

    private fun validateForm(state: CreateIncidentUiState): FormErrors {
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
}
