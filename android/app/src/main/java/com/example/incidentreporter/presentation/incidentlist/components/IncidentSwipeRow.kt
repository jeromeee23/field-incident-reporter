package com.example.incidentreporter.presentation.incidentlist.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.incidentreporter.R
import com.example.incidentreporter.presentation.theme.HomeSwipeCancel
import com.example.incidentreporter.presentation.theme.HomeSwipeRemove
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private val ActionPanelWidth = 84.dp

@Composable
fun IncidentSwipeRow(
    showCancelAction: Boolean,
    isActionInProgress: Boolean,
    onCancel: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val actionWidthPx = with(density) { ActionPanelWidth.toPx() }
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(isActionInProgress) {
        if (isActionInProgress) {
            offsetX.animateTo(0f, tween(180))
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp), clip = false)
            .clip(RoundedCornerShape(16.dp)),
    ) {
        Box(
            modifier = Modifier.matchParentSize(),
            contentAlignment = Alignment.CenterEnd,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(ActionPanelWidth),
            ) {
                if (showCancelAction) {
                    SwipeActionButton(
                        label = stringResource(R.string.home_swipe_cancel),
                        iconRes = R.drawable.ic_home_close,
                        color = HomeSwipeCancel,
                        onClick = {
                            scope.launch { offsetX.animateTo(0f, tween(180)) }
                            onCancel()
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
                SwipeActionButton(
                    label = stringResource(R.string.home_swipe_remove),
                    iconRes = R.drawable.ic_home_delete,
                    color = HomeSwipeRemove,
                    onClick = {
                        scope.launch { offsetX.animateTo(0f, tween(180)) }
                        onRemove()
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .pointerInput(isActionInProgress, actionWidthPx) {
                    if (isActionInProgress) return@pointerInput
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { _, dragAmount ->
                            scope.launch {
                                val next = (offsetX.value + dragAmount).coerceIn(-actionWidthPx, 0f)
                                offsetX.snapTo(next)
                            }
                        },
                        onDragEnd = {
                            scope.launch {
                                val target = if (offsetX.value < -actionWidthPx / 2f) {
                                    -actionWidthPx
                                } else {
                                    0f
                                }
                                offsetX.animateTo(target, tween(180))
                            }
                        },
                    )
                },
        ) {
            content()
        }
    }
}

@Composable
private fun SwipeActionButton(
    label: String,
    iconRes: Int,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(color)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            painter = painterResource(iconRes),
            contentDescription = label,
            tint = Color.White,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.weight(1f))
    }
}
