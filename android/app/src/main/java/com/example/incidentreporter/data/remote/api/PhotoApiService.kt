package com.example.incidentreporter.data.remote.api

import com.example.incidentreporter.data.remote.dto.response.ApiResponse
import com.example.incidentreporter.data.remote.dto.response.DeletePhotoResponse
import com.example.incidentreporter.data.remote.dto.response.IncidentPhotoDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface PhotoApiService {

    @Multipart
    @POST("uploads/photos")
    suspend fun uploadPhotos(
        @Part("incidentId") incidentId: RequestBody,
        @Part photos: List<MultipartBody.Part>,
    ): Response<ApiResponse<List<IncidentPhotoDto>>>

    @DELETE("uploads/photos/{photoId}")
    suspend fun deletePhoto(
        @Path("photoId") photoId: String,
    ): Response<DeletePhotoResponse>
}
