package com.example.incidentreporter.presentation.createincident.components

import android.content.ActivityNotFoundException
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.incidentreporter.R
import com.example.incidentreporter.presentation.createincident.MAX_PHOTOS
import com.example.incidentreporter.presentation.theme.FormFieldBorder
import com.example.incidentreporter.presentation.theme.HomeCard
import com.example.incidentreporter.presentation.theme.HomeNavy
import com.example.incidentreporter.presentation.theme.HomeSkeleton
import com.example.incidentreporter.presentation.theme.HomeSwipeRemove
import com.example.incidentreporter.presentation.theme.HomeTextPrimary
import com.example.incidentreporter.presentation.theme.HomeTextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun PhotoPickerSection(
    photoUris: List<Uri>,
    photosLocked: Boolean,
    enabled: Boolean,
    onPhotosPicked: (List<Uri>) -> Unit,
    onPhotoRemoved: (Uri) -> Unit,
    onPickerError: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var showSourceDialog by remember { mutableStateOf(false) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    var pendingCameraFile by remember { mutableStateOf<File?>(null) }

    val remainingSlots = MAX_PHOTOS - photoUris.size
    val multipleGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(
            remainingSlots.coerceAtLeast(2),
        ),
    ) { uris ->
        if (uris.isNotEmpty()) {
            onPhotosPicked(uris)
        }
    }
    val singleGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            onPhotosPicked(listOf(uri))
        }
    }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { success ->
        val capturedUri = pendingCameraUri
        val capturedFile = pendingCameraFile
        pendingCameraUri = null
        pendingCameraFile = null
        if (success && capturedUri != null) {
            onPhotosPicked(listOf(capturedUri))
        } else {
            capturedFile?.delete()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            pendingCameraFile?.delete()
        }
    }

    val canAddPhotos = enabled && !photosLocked && remainingSlots > 0

    fun launchGallery() {
        val request = PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        try {
            if (remainingSlots <= 1) {
                singleGalleryLauncher.launch(request)
            } else {
                multipleGalleryLauncher.launch(request)
            }
        } catch (_: ActivityNotFoundException) {
            onPickerError(context.getString(R.string.create_gallery_unavailable))
        }
    }

    fun launchCamera() {
        val photosDir = File(context.cacheDir, CAMERA_CACHE_DIR).apply { mkdirs() }
        val photoFile = File.createTempFile(CAMERA_FILE_PREFIX, ".jpg", photosDir)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.$FILE_PROVIDER_SUFFIX",
            photoFile,
        )
        pendingCameraUri = uri
        pendingCameraFile = photoFile
        try {
            cameraLauncher.launch(uri)
        } catch (_: ActivityNotFoundException) {
            pendingCameraUri = null
            pendingCameraFile = null
            photoFile.delete()
            onPickerError(context.getString(R.string.create_camera_unavailable))
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        if (photoUris.isEmpty()) {
            EmptyPhotoPicker(
                enabled = canAddPhotos,
                onClick = { showSourceDialog = true },
            )
        } else {
            PhotoGrid(
                photoUris = photoUris,
                canAddPhotos = canAddPhotos,
                canRemovePhotos = enabled && !photosLocked,
                onAddClick = { showSourceDialog = true },
                onPhotoRemoved = onPhotoRemoved,
            )
        }
    }

    if (showSourceDialog) {
        PhotoSourceDialog(
            onTakePhoto = {
                showSourceDialog = false
                launchCamera()
            },
            onChooseGallery = {
                showSourceDialog = false
                launchGallery()
            },
            onDismiss = { showSourceDialog = false },
        )
    }
}

@Composable
private fun EmptyPhotoPicker(
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .dashedBorder()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_home_image),
            contentDescription = null,
            tint = HomeNavy,
            modifier = Modifier.size(32.dp),
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.create_add_photos),
            color = HomeTextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.create_add_photos_subtitle),
            color = HomeTextSecondary,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun PhotoGrid(
    photoUris: List<Uri>,
    canAddPhotos: Boolean,
    canRemovePhotos: Boolean,
    onAddClick: () -> Unit,
    onPhotoRemoved: (Uri) -> Unit,
) {
    val tiles: List<Uri?> = buildList {
        addAll(photoUris)
        if (canAddPhotos) {
            add(null)
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        tiles.chunked(PHOTO_GRID_COLUMNS).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowItems.forEach { uri ->
                    Box(modifier = Modifier.weight(1f)) {
                        if (uri == null) {
                            AddPhotoTile(onClick = onAddClick)
                        } else {
                            PhotoThumbnail(
                                uri = uri,
                                canRemove = canRemovePhotos,
                                onRemove = { onPhotoRemoved(uri) },
                            )
                        }
                    }
                }
                repeat(PHOTO_GRID_COLUMNS - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun AddPhotoTile(
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .dashedBorder()
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_home_add),
            contentDescription = stringResource(R.string.create_add_photos),
            tint = HomeNavy,
            modifier = Modifier.size(28.dp),
        )
    }
}

@Composable
private fun PhotoThumbnail(
    uri: Uri,
    canRemove: Boolean,
    onRemove: () -> Unit,
) {
    val context = LocalContext.current
    val bitmap by produceState<Bitmap?>(initialValue = null, uri) {
        value = withContext(Dispatchers.IO) {
            runCatching { decodeThumbnail(context, uri, THUMBNAIL_MAX_SIZE_PX) }.getOrNull()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(HomeSkeleton),
    ) {
        val decoded = bitmap
        if (decoded != null) {
            Image(
                bitmap = decoded.asImageBitmap(),
                contentDescription = stringResource(R.string.create_photo_thumbnail),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
        if (canRemove) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(24.dp)
                    .background(HomeSwipeRemove, CircleShape)
                    .clickable(onClick = onRemove),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_home_close),
                    contentDescription = stringResource(R.string.create_photo_remove),
                    tint = HomeCard,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}

@Composable
private fun PhotoSourceDialog(
    onTakePhoto: () -> Unit,
    onChooseGallery: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.create_photo_source_title)) },
        text = {
            Column {
                TextButton(
                    onClick = onTakePhoto,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = stringResource(R.string.create_photo_source_camera),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start,
                    )
                }
                TextButton(
                    onClick = onChooseGallery,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = stringResource(R.string.create_photo_source_gallery),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.create_photo_source_cancel))
            }
        },
    )
}

private fun Modifier.dashedBorder(): Modifier {
    return drawBehind {
        val stroke = Stroke(
            width = 2.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 12f), 0f),
        )
        drawRoundRect(
            color = FormFieldBorder,
            style = stroke,
            cornerRadius = CornerRadius(12.dp.toPx()),
        )
    }
}

private fun decodeThumbnail(
    context: android.content.Context,
    uri: Uri,
    maxSizePx: Int,
): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri)?.use { input ->
        BitmapFactory.decodeStream(input, null, bounds)
    }
    val sampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, maxSizePx)
    val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
    return context.contentResolver.openInputStream(uri)?.use { input ->
        BitmapFactory.decodeStream(input, null, decodeOptions)
    }
}

private fun calculateInSampleSize(
    width: Int,
    height: Int,
    maxSizePx: Int,
): Int {
    var inSampleSize = 1
    if (width > maxSizePx || height > maxSizePx) {
        val halfWidth = width / 2
        val halfHeight = height / 2
        while (halfWidth / inSampleSize >= maxSizePx && halfHeight / inSampleSize >= maxSizePx) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}

private const val CAMERA_CACHE_DIR = "camera"
private const val CAMERA_FILE_PREFIX = "camera_"
private const val FILE_PROVIDER_SUFFIX = "provider"
private const val PHOTO_GRID_COLUMNS = 3
private const val THUMBNAIL_MAX_SIZE_PX = 256
