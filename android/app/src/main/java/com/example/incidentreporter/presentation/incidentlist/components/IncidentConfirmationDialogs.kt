package com.example.incidentreporter.presentation.incidentlist.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.incidentreporter.presentation.theme.FieldIncidentReporterTheme
import com.example.incidentreporter.R
import com.example.incidentreporter.presentation.theme.HomeSwipeCancel
import com.example.incidentreporter.presentation.theme.HomeSwipeRemove

@Composable
fun CancelReportDialog(
    onKeepReport: () -> Unit,
    onCancelReport: () -> Unit,
) {
    ActionResultDialog(
        icon = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(HomeSwipeCancel.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_home_warning),
                    contentDescription = null,
                    tint = HomeSwipeCancel,
                    modifier = Modifier.size(28.dp),
                )
            }
        },
        title = stringResource(R.string.home_cancel_dialog_title),
        message = stringResource(R.string.home_cancel_dialog_message),
        dismissLabel = stringResource(R.string.home_cancel_dialog_keep),
        confirmLabel = stringResource(R.string.home_cancel_dialog_confirm),
        confirmColor = HomeSwipeCancel,
        onDismiss = onKeepReport,
        onConfirm = onCancelReport,
    )
}

@Composable
fun RemoveReportDialog(
    onClose: () -> Unit,
    onRemove: () -> Unit,
) {
    ActionResultDialog(
        icon = null,
        title = stringResource(R.string.home_remove_dialog_title),
        message = stringResource(R.string.home_remove_dialog_message),
        dismissLabel = stringResource(R.string.home_remove_dialog_close),
        confirmLabel = stringResource(R.string.home_remove_dialog_confirm),
        confirmColor = HomeSwipeRemove,
        onDismiss = onClose,
        onConfirm = onRemove,
    )
}

@Preview(name = "Cancel Report Dialog", showBackground = true)
@Composable
private fun CancelReportDialogPreview() {
    FieldIncidentReporterTheme(dynamicColor = false) {
        CancelReportDialog(
            onKeepReport = {},
            onCancelReport = {},
        )
    }
}

@Preview(name = "Remove Report Dialog", showBackground = true)
@Composable
private fun RemoveReportDialogPreview() {
    FieldIncidentReporterTheme(dynamicColor = false) {
        RemoveReportDialog(
            onClose = {},
            onRemove = {},
        )
    }
}
