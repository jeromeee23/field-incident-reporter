package com.example.incidentreporter.domain.usecase

import com.example.incidentreporter.domain.model.Incident
import com.example.incidentreporter.domain.model.IncidentStatus
import com.example.incidentreporter.domain.model.UpdateIncidentStatusRequest
import com.example.incidentreporter.testing.FakeIncidentRepository
import com.example.incidentreporter.testing.IncidentFixtures
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CancelIncidentUseCaseTest {

    @Test
    fun invoke_updatesStatusToCancelled() = runTest {
        val repository = FakeIncidentRepository()
        val incident = IncidentFixtures.incident(status = IncidentStatus.OPEN)
        repository.incidents[incident.id] = incident
        val useCase = CancelIncidentUseCase(repository)

        val result = useCase(incident.id)

        assertEquals(1, repository.updateStatusCalls)
        assertEquals(
            UpdateIncidentStatusRequest(status = IncidentStatus.CANCELLED),
            repository.lastUpdateStatusRequest,
        )
        assertEquals(IncidentStatus.CANCELLED, result.getOrThrow().status)
        assertEquals(incident.id, result.getOrThrow().id)
    }

    @Test
    fun invoke_passesFailureThrough() = runTest {
        val repository = FakeIncidentRepository()
        val failure = Result.failure<Incident>(IllegalStateException("Cannot cancel"))
        repository.updateStatusResult = failure
        val useCase = CancelIncidentUseCase(repository)

        val result = useCase("incident-1")

        assertEquals(1, repository.updateStatusCalls)
        assertEquals(
            UpdateIncidentStatusRequest(status = IncidentStatus.CANCELLED),
            repository.lastUpdateStatusRequest,
        )
        assertTrue(result.isFailure)
        assertEquals("Cannot cancel", result.exceptionOrNull()?.message)
    }
}
