package com.example.incidentreporter.testing

import com.example.incidentreporter.data.remote.api.PhotoApiService
import com.example.incidentreporter.data.remote.dto.response.ApiResponse
import com.example.incidentreporter.data.remote.dto.response.DeletePhotoResponse
import com.example.incidentreporter.data.remote.dto.response.UploadPhotosDataDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response

class FakePhotoApiService : PhotoApiService {

    var uploadPhotosCalls = 0
    var deletePhotoCalls = 0

    var lastUploadIncidentIdBody: RequestBody? = null
    var lastUploadPhotoParts: List<MultipartBody.Part>? = null
    var lastDeletePhotoId: String? = null

    var uploadPhotosResponse: Response<ApiResponse<UploadPhotosDataDto>>? = null
    var deletePhotoResponse: Response<DeletePhotoResponse>? = null

    var uploadPhotosException: Throwable? = null
    var deletePhotoException: Throwable? = null

    override suspend fun uploadPhotos(
        incidentId: RequestBody,
        photos: List<MultipartBody.Part>,
    ): Response<ApiResponse<UploadPhotosDataDto>> {
        uploadPhotosCalls += 1
        lastUploadIncidentIdBody = incidentId
        lastUploadPhotoParts = photos
        uploadPhotosException?.let { throw it }
        return uploadPhotosResponse ?: error("No uploadPhotos response configured")
    }

    override suspend fun deletePhoto(
        photoId: String,
    ): Response<DeletePhotoResponse> {
        deletePhotoCalls += 1
        lastDeletePhotoId = photoId
        deletePhotoException?.let { throw it }
        return deletePhotoResponse ?: error("No deletePhoto response configured")
    }
}
