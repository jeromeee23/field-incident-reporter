package com.example.incidentreporter.presentation.createincident

import android.net.Uri
import com.example.incidentreporter.domain.model.IncidentCategory
import com.example.incidentreporter.domain.model.Photo

internal const val MAX_DESCRIPTION_LENGTH = 500
internal const val MAX_PHOTOS = 5
internal const val MAX_PHOTO_SIZE_BYTES = 5L * 1024 * 1024

data class CreateIncidentUiState(
    val title: String = "",
    val category: IncidentCategory? = null,
    val otherCategoryDescription: String = "",
    val description: String = "",
    val location: String = "",
    val selectedPhotoUris: List<Uri> = emptyList(),
    val uploadedPhotos: List<Photo> = emptyList(),
    val photosLocked: Boolean = false,
    val titleError: String? = null,
    val categoryError: String? = null,
    val otherCategoryDescriptionError: String? = null,
    val descriptionError: String? = null,
    val locationError: String? = null,
    val isProcessingPhotos: Boolean = false,
    val isSubmitting: Boolean = false,
    val submitError: String? = null,
) {
    val photoCount: Int
        get() = selectedPhotoUris.size

    val canSubmit: Boolean
        get() = title.trim().isNotBlank() &&
            category != null &&
            (category != IncidentCategory.OTHER || otherCategoryDescription.trim().isNotBlank()) &&
            description.trim().isNotBlank() &&
            description.length <= MAX_DESCRIPTION_LENGTH &&
            location.trim().isNotBlank() &&
            !isSubmitting &&
            !isProcessingPhotos
}
