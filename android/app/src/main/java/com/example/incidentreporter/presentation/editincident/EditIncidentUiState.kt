package com.example.incidentreporter.presentation.editincident

import android.net.Uri
import com.example.incidentreporter.domain.model.IncidentCategory
import com.example.incidentreporter.domain.model.Photo
import com.example.incidentreporter.presentation.createincident.MAX_DESCRIPTION_LENGTH
import com.example.incidentreporter.presentation.incidentlist.IncidentLoadError

data class EditIncidentUiState(
    val isLoading: Boolean = true,
    val loadError: IncidentLoadError? = null,
    val incidentId: String = "",
    val title: String = "",
    val category: IncidentCategory? = null,
    val otherCategoryDescription: String = "",
    val description: String = "",
    val location: String = "",
    val existingPhotos: List<Photo> = emptyList(),
    val newPhotoUris: List<Uri> = emptyList(),
    val uploadedNewPhotos: List<Photo> = emptyList(),
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
    val photoItems: List<EditPhotoItem>
        get() = buildList {
            existingPhotos.forEach { photo -> add(EditPhotoItem.Existing(photo)) }
            if (photosLocked) {
                uploadedNewPhotos.forEach { photo -> add(EditPhotoItem.Uploaded(photo)) }
            } else {
                newPhotoUris.forEach { uri -> add(EditPhotoItem.Local(uri)) }
            }
        }

    val visiblePhotoCount: Int
        get() = photoItems.size

    val canSave: Boolean
        get() = !isLoading &&
            loadError == null &&
            title.trim().isNotBlank() &&
            category != null &&
            (category != IncidentCategory.OTHER || otherCategoryDescription.trim().isNotBlank()) &&
            description.trim().isNotBlank() &&
            description.length <= MAX_DESCRIPTION_LENGTH &&
            location.trim().isNotBlank() &&
            !isSubmitting &&
            !isProcessingPhotos
}

sealed interface EditPhotoItem {
    data class Existing(val photo: Photo) : EditPhotoItem
    data class Local(val uri: Uri) : EditPhotoItem
    data class Uploaded(val photo: Photo) : EditPhotoItem
}
