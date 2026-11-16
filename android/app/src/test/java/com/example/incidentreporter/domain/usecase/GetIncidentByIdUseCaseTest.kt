package com.example.incidentreporter.domain.usecase

import com.example.incidentreporter.domain.model.Incident
import com.example.incidentreporter.testing.FakeIncidentRepository
import com.example.incidentreporter.testing.IncidentFixtures
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetIncidentByIdUseCaseTest {

    @Test
    fun invoke_passesIncidentIdToRepositoryAndReturnsResult() = runTest {
        val repository = FakeIncidentRepository()
        val incident = IncidentFixtures.incident()
        repository.incidents[incident.id] = incident
        val useCase = GetIncidentByIdUseCase(repository)

        val result = useCase(incident.id)

        assertEquals(1, repository.getIncidentCalls)
        assertEquals(Result.success(incident), result)
    }

    @Test
    fun invoke_passesFailureThrough() = runTest {
        val repository = FakeIncidentRepository()
        val failure = Result.failure<Incident>(IllegalStateException("Not found"))
        repository.getIncidentResult = failure
        val useCase = GetIncidentByIdUseCase(repository)

        val result = useCase("missing-id")

        assertEquals(1, repository.getIncidentCalls)
        assertTrue(result.isFailure)
        assertEquals("Not found", result.exceptionOrNull()?.message)
    }
}
