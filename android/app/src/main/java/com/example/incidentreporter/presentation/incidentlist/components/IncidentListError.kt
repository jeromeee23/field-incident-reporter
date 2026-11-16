package com.example.incidentreporter.presentation.incidentlist.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.incidentreporter.R
import com.example.incidentreporter.presentation.incidentlist.IncidentLoadError
import com.example.incidentreporter.presentation.theme.HomeFabBlue
import com.example.incidentreporter.presentation.theme.HomeSkeleton
import com.example.incidentreporter.presentation.theme.HomeTextMuted
import com.example.incidentreporter.presentation.theme.HomeTextPrimary
import com.example.incidentreporter.presentation.theme.HomeTextSecondary

@Composable
fun IncidentListError(
    loadError: IncidentLoadError,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val illustrationRes = when (loadError) {
        IncidentLoadError.Network -> R.drawable.ic_home_wifi_off
        is IncidentLoadError.Transport,
        is IncidentLoadError.Api,
        IncidentLoadError.Unexpected,
        -> R.drawable.ic_home_warning
    }
    val message = when (loadError) {
        IncidentLoadError.Network -> stringResource(R.string.home_error_message)
        is IncidentLoadError.Transport -> if (loadError.timedOut) {
            stringResource(R.string.details_error_timeout_message)
        } else {
            stringResource(R.string.details_error_transport_message)
        }
        is IncidentLoadError.Api -> loadError.message.ifBlank {
            stringResource(R.string.details_error_api_fallback)
        }
        IncidentLoadError.Unexpected -> {
            stringResource(R.string.details_error_unexpected_message)
        }
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(HomeSkeleton),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(illustrationRes),
                contentDescription = null,
                tint = HomeTextMuted,
                modifier = Modifier.size(32.dp),
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.home_error_title),
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
        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = HomeFabBlue,
                contentColor = Color.White,
            ),
        ) {
            Text(
                text = stringResource(R.string.home_retry),
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
