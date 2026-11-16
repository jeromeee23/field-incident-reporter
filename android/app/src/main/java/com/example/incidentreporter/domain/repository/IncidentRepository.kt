package com.example.incidentreporter.domain.repository

import com.example.incidentreporter.domain.model.CreateIncidentRequest
import com.example.incidentreporter.domain.model.Incident
import com.example.incidentreporter.domain.model.IncidentStatus
import com.example.incidentreporter.domain.model.Photo
import com.example.incidentreporter.domain.model.UpdateIncidentRequest
import com.example.incidentreporter.domain.model.UpdateIncidentStatusRequest
import java.io.File

interface IncidentRepository {

    suspend fun getIncidents(
        statuses: List<IncidentStatus>,
    ): Result<List<Incident>>

    suspend fun getIncidentById(
        id: String,
    ): Result<Incident>

    suspend fun createIncident(
        request: CreateIncidentRequest,
    ): Result<Incident>

    suspend fun updateIncident(
        id: String,
        request: UpdateIncidentRequest,
    ): Result<Incident>

    suspend fun updateIncidentStatus(
        id: String,
        request: UpdateIncidentStatusRequest,
    ): Result<Incident>

    suspend fun deleteIncident(
        id: String,
    ): Result<Unit>

    suspend fun uploadPhotos(
        incidentId: String,
        photos: List<File>,
    ): Result<List<Photo>>

    suspend fun deletePhoto(
        photoId: String,
    ): Result<Unit>
}
