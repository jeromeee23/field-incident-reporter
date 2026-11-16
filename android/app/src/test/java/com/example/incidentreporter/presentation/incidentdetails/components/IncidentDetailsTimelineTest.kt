package com.example.incidentreporter.presentation.incidentdetails.components

import com.example.incidentreporter.domain.model.IncidentStatus
import com.example.incidentreporter.testing.IncidentFixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IncidentDetailsTimelineTest {

    @Test
    fun openIncident_createdIsCurrent() {
        val incident = IncidentFixtures.incident(status = IncidentStatus.OPEN)

        val entries = reportTimelineEntries(incident)

        assertEquals(listOf(TimelineKind.Created, TimelineKind.Updated), entries.map { it.kind })
        assertEquals(TimelineKind.Created, entries.single { it.isCurrent }.kind)
        assertFalse(entries.any { it.kind == TimelineKind.Completed })
        assertFalse(entries.any { it.kind == TimelineKind.Cancelled })
    }

    @Test
    fun inProgressIncident_updatedIsCurrent() {
        val incident = IncidentFixtures.incident(status = IncidentStatus.IN_PROGRESS).copy(
            updatedAt = "2026-11-12T14:18:00Z",
        )

        val entries = reportTimelineEntries(incident)

        assertEquals(listOf(TimelineKind.Created, TimelineKind.Updated), entries.map { it.kind })
        assertEquals(TimelineKind.Updated, entries.single { it.isCurrent }.kind)
    }

    @Test
    fun completedIncident_completedIsCurrent() {
        val incident = IncidentFixtures.incident(
            status = IncidentStatus.COMPLETED,
            completedAt = "2026-11-12T15:00:00Z",
        ).copy(updatedAt = "2026-11-12T15:00:00Z")

        val entries = reportTimelineEntries(incident)

        assertEquals(
            listOf(TimelineKind.Created, TimelineKind.Updated, TimelineKind.Completed),
            entries.map { it.kind },
        )
        assertEquals(TimelineKind.Completed, entries.single { it.isCurrent }.kind)
        assertFalse(entries.any { it.kind == TimelineKind.Cancelled })
    }

    @Test
    fun cancelledIncident_cancelledIsCurrentAndCompletedIsOmitted() {
        val incident = IncidentFixtures.incident(
            status = IncidentStatus.CANCELLED,
            completedAt = "2026-11-12T15:00:00Z",
            cancelledAt = "2026-11-12T16:00:00Z",
        ).copy(updatedAt = "2026-11-12T16:00:00Z")

        val entries = reportTimelineEntries(incident)

        assertEquals(
            listOf(TimelineKind.Created, TimelineKind.Updated, TimelineKind.Cancelled),
            entries.map { it.kind },
        )
        assertEquals(TimelineKind.Cancelled, entries.single { it.isCurrent }.kind)
        assertFalse(entries.any { it.kind == TimelineKind.Completed })
    }

    @Test
    fun cancelledIncident_missingCancelledAt_usesUpdatedAt() {
        val incident = IncidentFixtures.incident(
            status = IncidentStatus.CANCELLED,
            cancelledAt = null,
        ).copy(updatedAt = "2026-11-12T14:18:00Z")

        val entries = reportTimelineEntries(incident)
        val cancelled = entries.single { it.kind == TimelineKind.Cancelled }

        assertEquals("2026-11-12T14:18:00Z", cancelled.timestamp)
        assertTrue(cancelled.isCurrent)
        assertFalse(entries.any { it.kind == TimelineKind.Completed })
    }

    @Test
    fun currentTimelineKind_mapsEachStatus() {
        assertEquals(TimelineKind.Created, currentTimelineKind(IncidentStatus.OPEN))
        assertEquals(TimelineKind.Updated, currentTimelineKind(IncidentStatus.IN_PROGRESS))
        assertEquals(TimelineKind.Completed, currentTimelineKind(IncidentStatus.COMPLETED))
        assertEquals(TimelineKind.Cancelled, currentTimelineKind(IncidentStatus.CANCELLED))
    }
}
