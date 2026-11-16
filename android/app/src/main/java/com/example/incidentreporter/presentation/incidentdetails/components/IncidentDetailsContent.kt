package com.example.incidentreporter.presentation.incidentdetails.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.incidentreporter.R
import com.example.incidentreporter.domain.model.Incident
import com.example.incidentreporter.domain.model.IncidentCategory
import com.example.incidentreporter.presentation.incidentlist.components.IncidentStatusChip
import com.example.incidentreporter.presentation.theme.HomeNavy
import com.example.incidentreporter.presentation.theme.HomeTextMuted
import com.example.incidentreporter.presentation.theme.HomeTextPrimary
import com.example.incidentreporter.presentation.theme.HomeTextSecondary

@Composable
fun IncidentDetailsContent(
    incident: Incident,
    canEdit: Boolean,
    canUpdateStatus: Boolean,
    canCancel: Boolean,
    canRemove: Boolean,
    isUpdatingStatus: Boolean,
    isCancelling: Boolean,
    isDeleting: Boolean,
    isActionInProgress: Boolean,
    onPhotoClick: (Int) -> Unit,
    onEditClick: () -> Unit,
    onUpdateStatusClick: () -> Unit,
    onCancelClick: () -> Unit,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = incident.title,
            color = HomeTextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = incident.incidentNumber,
                modifier = Modifier.weight(1f),
                color = HomeTextMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            )
            IncidentStatusChip(status = incident.status)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = incident.description,
            color = HomeTextSecondary,
            fontSize = 14.sp,
        )

        Spacer(modifier = Modifier.height(24.dp))
        DetailsMetaRow(
            iconRes = R.drawable.ic_home_clipboard,
            label = stringResource(R.string.details_field_category),
            value = stringResource(incident.category.labelRes()),
        )
        if (incident.category == IncidentCategory.OTHER) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.details_field_other_category),
                modifier = Modifier.padding(start = 34.dp),
                color = HomeTextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = incident.otherCategoryDescription.orEmpty(),
                modifier = Modifier.padding(start = 34.dp),
                color = HomeTextSecondary,
                fontSize = 14.sp,
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        DetailsMetaRow(
            iconRes = R.drawable.ic_details_location,
            label = stringResource(R.string.details_field_location),
            value = incident.location,
        )

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.details_photos_title, incident.photos.size),
            color = HomeTextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(12.dp))
        IncidentDetailsPhotos(
            photos = incident.photos,
            onPhotoClick = onPhotoClick,
        )

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.details_timeline_title),
            color = HomeTextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(12.dp))
        IncidentDetailsTimeline(incident = incident)

        Spacer(modifier = Modifier.height(24.dp))
        IncidentDetailsActions(
            canEdit = canEdit,
            canUpdateStatus = canUpdateStatus,
            canCancel = canCancel,
            canRemove = canRemove,
            isUpdatingStatus = isUpdatingStatus,
            isCancelling = isCancelling,
            isDeleting = isDeleting,
            isActionInProgress = isActionInProgress,
            onEditClick = onEditClick,
            onUpdateStatusClick = onUpdateStatusClick,
            onCancelClick = onCancelClick,
            onRemoveClick = onRemoveClick,
        )
    }
}

@Composable
private fun DetailsMetaRow(
    iconRes: Int,
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = HomeNavy,
            modifier = Modifier.size(22.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                color = HomeTextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                color = HomeTextSecondary,
                fontSize = 14.sp,
            )
        }
    }
}

private fun IncidentCategory.labelRes(): Int {
    return when (this) {
        IncidentCategory.EQUIPMENT_DAMAGE -> R.string.create_category_equipment_damage
        IncidentCategory.ROAD_HAZARD -> R.string.create_category_road_hazard
        IncidentCategory.SAFETY_ISSUE -> R.string.create_category_safety_issue
        IncidentCategory.INFRASTRUCTURE -> R.string.create_category_infrastructure
        IncidentCategory.OTHER -> R.string.create_category_other
    }
}
