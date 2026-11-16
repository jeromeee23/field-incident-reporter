package com.example.incidentreporter.presentation.incidentlist.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.incidentreporter.R
import com.example.incidentreporter.domain.model.IncidentStatus
import com.example.incidentreporter.presentation.theme.HomeCard
import com.example.incidentreporter.presentation.theme.HomeStatusCancelled
import com.example.incidentreporter.presentation.theme.HomeStatusCompleted
import com.example.incidentreporter.presentation.theme.HomeStatusInProgress
import com.example.incidentreporter.presentation.theme.HomeStatusOpen

@Composable
fun IncidentStatusChip(
    status: IncidentStatus,
    modifier: Modifier = Modifier,
) {
    val color = status.chipColor()
    Surface(
        modifier = modifier.border(1.dp, color, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = HomeCard,
    ) {
        Text(
            text = stringResource(status.labelRes()),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

internal fun IncidentStatus.chipColor(): Color {
    return when (this) {
        IncidentStatus.OPEN -> HomeStatusOpen
        IncidentStatus.IN_PROGRESS -> HomeStatusInProgress
        IncidentStatus.COMPLETED -> HomeStatusCompleted
        IncidentStatus.CANCELLED -> HomeStatusCancelled
    }
}

internal fun IncidentStatus.labelRes(): Int {
    return when (this) {
        IncidentStatus.OPEN -> R.string.home_status_open
        IncidentStatus.IN_PROGRESS -> R.string.home_status_in_progress
        IncidentStatus.COMPLETED -> R.string.home_status_completed
        IncidentStatus.CANCELLED -> R.string.home_status_cancelled
    }
}
