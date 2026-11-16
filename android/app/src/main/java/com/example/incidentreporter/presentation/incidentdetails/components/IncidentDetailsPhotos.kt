package com.example.incidentreporter.presentation.incidentdetails.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.example.incidentreporter.R
import com.example.incidentreporter.domain.model.Photo
import com.example.incidentreporter.presentation.theme.HomeSkeleton
import com.example.incidentreporter.presentation.theme.HomeTextMuted

private const val PHOTO_COLUMNS = 3
private val ThumbnailShape = RoundedCornerShape(12.dp)

@Composable
fun IncidentDetailsPhotos(
    photos: List<Photo>,
    onPhotoClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (photos.isEmpty()) {
            Text(
                text = stringResource(R.string.details_photos_empty),
                color = HomeTextMuted,
                fontSize = 14.sp,
            )
        } else {
            val visiblePhotos = photos.take(PHOTO_COLUMNS)
            val overflowCount = (photos.size - PHOTO_COLUMNS).coerceAtLeast(0)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                visiblePhotos.forEachIndexed { index, photo ->
                    val isOverflowCell = overflowCount > 0 && index == visiblePhotos.lastIndex
                    IncidentPhotoThumbnail(
                        photo = photo,
                        overflowCount = if (isOverflowCell) overflowCount else 0,
                        onClick = { onPhotoClick(index) },
                        modifier = Modifier.weight(1f),
                    )
                }
                repeat(PHOTO_COLUMNS - visiblePhotos.size) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun IncidentPhotoThumbnail(
    photo: Photo,
    overflowCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val painter = rememberAsyncImagePainter(model = photo.url)
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(ThumbnailShape)
            .background(HomeSkeleton)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        when (painter.state) {
            is AsyncImagePainter.State.Success -> {
                Image(
                    painter = painter,
                    contentDescription = stringResource(R.string.details_photo_thumbnail),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
            is AsyncImagePainter.State.Error -> {
                Icon(
                    painter = painterResource(R.drawable.ic_home_image),
                    contentDescription = stringResource(R.string.details_photo_thumbnail),
                    tint = HomeTextMuted,
                    modifier = Modifier.size(28.dp),
                )
            }
            else -> Unit
        }
        if (overflowCount > 0) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.details_photos_overflow, overflowCount),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
