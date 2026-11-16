package com.example.incidentreporter.presentation.incidentdetails

import com.example.incidentreporter.domain.model.IncidentStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IncidentStatusTransitionsTest {

    @Test
    fun nextAllowedStatuses_openCanMoveToInProgress() {
        assertEquals(
            listOf(IncidentStatus.IN_PROGRESS),
            IncidentStatus.OPEN.nextAllowedStatuses(),
        )
    }

    @Test
    fun nextAllowedStatuses_inProgressCanMoveToCompleted() {
        assertEquals(
            listOf(IncidentStatus.COMPLETED),
            IncidentStatus.IN_PROGRESS.nextAllowedStatuses(),
        )
    }

    @Test
    fun nextAllowedStatuses_completedAndCancelledHaveNoTransitions() {
        assertEquals(emptyList<IncidentStatus>(), IncidentStatus.COMPLETED.nextAllowedStatuses())
        assertEquals(emptyList<IncidentStatus>(), IncidentStatus.CANCELLED.nextAllowedStatuses())
    }

    @Test
    fun allowsLifecycleActions_onlyOpenAndInProgress() {
        assertTrue(IncidentStatus.OPEN.allowsLifecycleActions())
        assertTrue(IncidentStatus.IN_PROGRESS.allowsLifecycleActions())
        assertFalse(IncidentStatus.COMPLETED.allowsLifecycleActions())
        assertFalse(IncidentStatus.CANCELLED.allowsLifecycleActions())
        assertFalse(null.allowsLifecycleActions())
    }
}
