package com.example.incidentreporter.presentation.incidentdetails.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.incidentreporter.R
import com.example.incidentreporter.domain.model.IncidentStatus
import com.example.incidentreporter.presentation.incidentlist.components.labelRes
import com.example.incidentreporter.presentation.theme.HomeCard
import com.example.incidentreporter.presentation.theme.HomeFabBlue
import com.example.incidentreporter.presentation.theme.HomeTextMuted
import com.example.incidentreporter.presentation.theme.HomeTextPrimary
import com.example.incidentreporter.presentation.theme.HomeTextSecondary

@Composable
fun UpdateStatusDialog(
    availableStatuses: List<IncidentStatus>,
    onDismiss: () -> Unit,
    onConfirm: (IncidentStatus) -> Unit,
) {
    if (availableStatuses.isEmpty()) {
        return
    }
    var selectedStatus by remember {
        mutableStateOf(availableStatuses.first())
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = HomeCard,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.details_update_status_title),
                    color = HomeTextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.details_update_status_message),
                    color = HomeTextSecondary,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(16.dp))
                availableStatuses.forEach { status ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedStatus = status }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = selectedStatus == status,
                            onClick = { selectedStatus = status },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = HomeFabBlue,
                            ),
                        )
                        Text(
                            text = stringResource(status.labelRes()),
                            color = HomeTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, HomeTextMuted.copy(alpha = 0.35f), RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        contentPadding = PaddingValues(vertical = 10.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.details_update_status_dismiss),
                            color = HomeTextPrimary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Button(
                        onClick = { onConfirm(selectedStatus) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HomeFabBlue,
                            contentColor = Color.White,
                        ),
                    ) {
                        Text(
                            text = stringResource(R.string.details_update_status_confirm),
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}
