package com.example.incidentreporter.data.remote.api

import com.example.incidentreporter.data.remote.dto.response.DeletePhotoResponse
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.Path

interface PhotoApiService {

    @DELETE("uploads/photos/{photoId}")
    suspend fun deletePhoto(
        @Path("photoId") photoId: String,
    ): Response<DeletePhotoResponse>
}
