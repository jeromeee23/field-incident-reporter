package com.example.incidentreporter.presentation.incidentlist.components

import androidx.compose.foundation.border
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.incidentreporter.presentation.theme.HomeCard
import com.example.incidentreporter.presentation.theme.HomeTextMuted
import com.example.incidentreporter.presentation.theme.HomeTextPrimary
import com.example.incidentreporter.presentation.theme.HomeTextSecondary

@Composable
internal fun ActionResultDialog(
    icon: (@Composable () -> Unit)?,
    title: String,
    message: String,
    dismissLabel: String,
    confirmLabel: String,
    confirmColor: Color,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
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
                icon?.let { iconContent ->
                    iconContent()
                    Spacer(modifier = Modifier.height(16.dp))
                }
                Text(
                    text = title,
                    color = HomeTextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message,
                    color = HomeTextSecondary,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(24.dp))
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
                            text = dismissLabel,
                            color = HomeTextPrimary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = confirmColor,
                            contentColor = Color.White,
                        ),
                    ) {
                        Text(
                            text = confirmLabel,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}
