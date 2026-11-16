package com.example.incidentreporter.presentation.incidentlist.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.incidentreporter.R
import com.example.incidentreporter.presentation.incidentlist.home.HomeTab
import com.example.incidentreporter.presentation.theme.HomeNavy
import com.example.incidentreporter.presentation.theme.HomeTabUnselected
import com.example.incidentreporter.presentation.theme.HomeTextPrimary

@Composable
fun IncidentTabBar(
    selectedTab: HomeTab,
    activeCount: Int,
    completedCount: Int,
    cancelledCount: Int,
    onTabSelected: (HomeTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IncidentTab(
            label = tabLabel(HomeTab.ACTIVE, activeCount, selectedTab == HomeTab.ACTIVE),
            selected = selectedTab == HomeTab.ACTIVE,
            onClick = { onTabSelected(HomeTab.ACTIVE) },
            modifier = Modifier.weight(1f),
        )
        IncidentTab(
            label = tabLabel(HomeTab.COMPLETED, completedCount, selectedTab == HomeTab.COMPLETED),
            selected = selectedTab == HomeTab.COMPLETED,
            onClick = { onTabSelected(HomeTab.COMPLETED) },
            modifier = Modifier.weight(1.15f),
        )
        IncidentTab(
            label = tabLabel(HomeTab.CANCELLED, cancelledCount, selectedTab == HomeTab.CANCELLED),
            selected = selectedTab == HomeTab.CANCELLED,
            onClick = { onTabSelected(HomeTab.CANCELLED) },
            modifier = Modifier.weight(1.15f),
        )
    }
}

@Composable
private fun IncidentTab(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(24.dp)
    Box(
        modifier = modifier
            .then(
                if (selected) {
                    Modifier
                } else {
                    Modifier.shadow(2.dp, shape, clip = false)
                },
            )
            .clip(shape)
            .background(if (selected) HomeNavy else HomeTabUnselected)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else HomeTextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
    }
}

@Composable
private fun tabLabel(
    tab: HomeTab,
    count: Int,
    selected: Boolean,
): String {
    val name = when (tab) {
        HomeTab.ACTIVE -> stringResource(R.string.home_tab_active)
        HomeTab.COMPLETED -> stringResource(R.string.home_tab_completed)
        HomeTab.CANCELLED -> stringResource(R.string.home_tab_cancelled)
    }
    return if (!selected && count > 0) {
        stringResource(R.string.home_tab_with_count, name, count)
    } else {
        name
    }
}
