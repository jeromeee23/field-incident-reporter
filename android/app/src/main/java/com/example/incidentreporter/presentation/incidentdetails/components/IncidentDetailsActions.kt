package com.example.incidentreporter.presentation.incidentdetails.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.incidentreporter.R
import com.example.incidentreporter.presentation.theme.HomeFabBlue
import com.example.incidentreporter.presentation.theme.HomeSwipeRemove

@Composable
fun IncidentDetailsActions(
    canEdit: Boolean,
    canUpdateStatus: Boolean,
    canCancel: Boolean,
    canRemove: Boolean,
    isActionInProgress: Boolean,
    onEditClick: () -> Unit,
    onUpdateStatusClick: () -> Unit,
    onCancelClick: () -> Unit,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val actions = buildList {
        if (canEdit) {
            add(
                DetailsAction(
                    label = stringResource(R.string.details_action_edit),
                    iconRes = R.drawable.ic_details_edit,
                    color = HomeFabBlue,
                    onClick = onEditClick,
                ),
            )
        }
        if (canUpdateStatus) {
            add(
                DetailsAction(
                    label = stringResource(R.string.details_action_update_status),
                    iconRes = R.drawable.ic_details_refresh,
                    color = HomeFabBlue,
                    onClick = onUpdateStatusClick,
                ),
            )
        }
        if (canCancel) {
            add(
                DetailsAction(
                    label = stringResource(R.string.details_action_cancel),
                    iconRes = R.drawable.ic_details_cancel,
                    color = HomeSwipeRemove,
                    onClick = onCancelClick,
                ),
            )
        }
        if (canRemove) {
            add(
                DetailsAction(
                    label = stringResource(R.string.details_action_remove),
                    iconRes = R.drawable.ic_home_delete,
                    color = HomeSwipeRemove,
                    onClick = onRemoveClick,
                ),
            )
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        actions.chunked(2).forEach { rowActions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                rowActions.forEach { action ->
                    DetailsActionButton(
                        action = action,
                        enabled = !isActionInProgress,
                        modifier = Modifier.weight(1f),
                    )
                }
                if (rowActions.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun DetailsActionButton(
    action: DetailsAction,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val color = action.color.copy(alpha = if (enabled) 1f else 0.5f)
    OutlinedButton(
        onClick = action.onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 48.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, color),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = action.color,
            disabledContentColor = action.color.copy(alpha = 0.5f),
        ),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
    ) {
        Icon(
            painter = painterResource(action.iconRes),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = action.label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private data class DetailsAction(
    val label: String,
    val iconRes: Int,
    val color: Color,
    val onClick: () -> Unit,
)
