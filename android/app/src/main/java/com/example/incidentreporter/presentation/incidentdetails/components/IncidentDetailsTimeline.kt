package com.example.incidentreporter.presentation.incidentdetails.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.incidentreporter.R
import com.example.incidentreporter.domain.model.Incident
import com.example.incidentreporter.domain.model.IncidentStatus
import com.example.incidentreporter.presentation.incidentlist.formatIncidentCreatedAt
import com.example.incidentreporter.presentation.incidentlist.incidentTimestampMillis
import com.example.incidentreporter.presentation.theme.HomeFabBlue
import com.example.incidentreporter.presentation.theme.HomeSkeleton
import com.example.incidentreporter.presentation.theme.HomeTextMuted
import com.example.incidentreporter.presentation.theme.HomeTextPrimary

@Composable
fun IncidentDetailsTimeline(
    incident: Incident,
    modifier: Modifier = Modifier,
) {
    val entries = remember(
        incident.status,
        incident.createdAt,
        incident.updatedAt,
        incident.completedAt,
        incident.cancelledAt,
    ) {
        reportTimelineEntries(incident)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        entries.forEachIndexed { index, entry ->
            TimelineRow(
                entry = entry,
                isLast = index == entries.lastIndex,
            )
        }
    }
}

@Composable
private fun TimelineRow(
    entry: TimelineEntry,
    isLast: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
    ) {
        Column(
            modifier = Modifier
                .width(12.dp)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .then(
                        if (entry.isCurrent) {
                            Modifier
                                .clip(CircleShape)
                                .background(HomeFabBlue)
                        } else {
                            Modifier.border(2.dp, HomeTextMuted, CircleShape)
                        },
                    ),
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f)
                        .background(HomeSkeleton),
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else 16.dp),
        ) {
            Text(
                text = stringResource(entry.labelRes),
                color = HomeTextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = formatIncidentCreatedAt(entry.timestamp),
                color = HomeTextMuted,
                fontSize = 12.sp,
            )
        }
    }
}

internal enum class TimelineKind {
    Created,
    Updated,
    Completed,
    Cancelled,
}

internal data class TimelineEntry(
    val kind: TimelineKind,
    val labelRes: Int,
    val timestamp: String,
    val isCurrent: Boolean = false,
)

internal fun reportTimelineEntries(incident: Incident): List<TimelineEntry> {
    val currentKind = currentTimelineKind(incident.status)
    return buildList {
        add(
            TimelineEntry(
                kind = TimelineKind.Created,
                labelRes = R.string.details_timeline_created,
                timestamp = incident.createdAt,
            ),
        )
        add(
            TimelineEntry(
                kind = TimelineKind.Updated,
                labelRes = R.string.details_timeline_updated,
                timestamp = incident.updatedAt,
            ),
        )
        if (incident.status != IncidentStatus.CANCELLED) {
            incident.completedAt?.let { completedAt ->
                add(
                    TimelineEntry(
                        kind = TimelineKind.Completed,
                        labelRes = R.string.details_timeline_completed,
                        timestamp = completedAt,
                    ),
                )
            }
        }
        if (incident.status == IncidentStatus.CANCELLED) {
            add(
                TimelineEntry(
                    kind = TimelineKind.Cancelled,
                    labelRes = R.string.details_timeline_cancelled,
                    timestamp = incident.cancelledAt ?: incident.updatedAt,
                ),
            )
        } else {
            incident.cancelledAt?.let { cancelledAt ->
                add(
                    TimelineEntry(
                        kind = TimelineKind.Cancelled,
                        labelRes = R.string.details_timeline_cancelled,
                        timestamp = cancelledAt,
                    ),
                )
            }
        }
    }
        .sortedWith(compareBy(nullsLast()) { incidentTimestampMillis(it.timestamp) })
        .map { entry -> entry.copy(isCurrent = entry.kind == currentKind) }
}

internal fun currentTimelineKind(status: IncidentStatus): TimelineKind {
    return when (status) {
        IncidentStatus.OPEN -> TimelineKind.Created
        IncidentStatus.IN_PROGRESS -> TimelineKind.Updated
        IncidentStatus.COMPLETED -> TimelineKind.Completed
        IncidentStatus.CANCELLED -> TimelineKind.Cancelled
    }
}
