package com.example.incidentreporter.presentation.incidentdetails.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.incidentreporter.R
import com.example.incidentreporter.domain.model.Photo

@Composable
fun IncidentPhotoViewer(
    photos: List<Photo>,
    initialIndex: Int,
    onClose: () -> Unit,
) {
    if (photos.isEmpty()) {
        return
    }
    val startIndex = initialIndex.coerceIn(0, photos.lastIndex)
    val pagerState = rememberPagerState(
        initialPage = startIndex,
        pageCount = { photos.size },
    )

    BackHandler(onBack = onClose)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            AsyncImage(
                model = photos[page].url,
                contentDescription = stringResource(R.string.details_photo_thumbnail),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
                error = painterResource(R.drawable.ic_home_image),
                placeholder = painterResource(R.drawable.ic_home_image),
            )
        }
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopStart)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(4.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_home_close),
                contentDescription = stringResource(R.string.details_photo_viewer_close),
                tint = Color.White,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}
