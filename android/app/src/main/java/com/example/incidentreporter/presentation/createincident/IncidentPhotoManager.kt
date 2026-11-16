package com.example.incidentreporter.presentation.createincident

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class IncidentPhotoManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val photoFiles = linkedMapOf<Uri, File>()

    suspend fun copyAndValidate(uris: List<Uri>): Result<List<Uri>> {
        return withContext(Dispatchers.IO) {
            val copied = mutableListOf<CopiedPhoto>()
            try {
                uris.forEach { uri ->
                    val mimeType = context.contentResolver.getType(uri)
                    val extension = extensionFor(mimeType, uri)
                    validateFormat(mimeType, extension).getOrElse { error ->
                        deleteCopied(copied)
                        return@withContext Result.failure(error)
                    }
                    val resolvedExtension = checkNotNull(extension) {
                        "Only JPEG, PNG, and WebP photos are supported. The selected photos were not added."
                    }
                    val file = copyUriToTempFile(uri, resolvedExtension)
                    val sizeError = validateFileSize(file)
                    if (sizeError != null) {
                        file.delete()
                        deleteCopied(copied)
                        return@withContext Result.failure(sizeError)
                    }
                    copied += CopiedPhoto(
                        displayUri = uriForFile(file),
                        file = file,
                    )
                }
                copied.forEach { item -> photoFiles[item.displayUri] = item.file }
                Result.success(copied.map { it.displayUri })
            } catch (error: CancellationException) {
                deleteCopied(copied)
                throw error
            } catch (error: Exception) {
                deleteCopied(copied)
                Result.failure(
                    IllegalArgumentException(
                        "Unable to read the selected photos. The selected photos were not added.",
                        error,
                    ),
                )
            }
        }
    }

    fun filesFor(uris: List<Uri>): List<File>? {
        val files = uris.mapNotNull { uri -> photoFiles[uri] }
        return files.takeIf { it.size == uris.size }
    }

    fun remove(uri: Uri) {
        photoFiles.remove(uri)?.delete()
    }

    fun clear() {
        photoFiles.values.forEach { file -> file.delete() }
        photoFiles.clear()
    }

    private fun copyUriToTempFile(uri: Uri, extension: String): File {
        val directory = File(context.cacheDir, PHOTO_CACHE_DIR).apply { mkdirs() }
        val destination = File.createTempFile(PHOTO_FILE_PREFIX, ".$extension", directory)
        val input = context.contentResolver.openInputStream(uri)
            ?: error("Unable to read the selected photos. The selected photos were not added.")
        input.use { stream ->
            destination.outputStream().use { output -> stream.copyTo(output) }
        }
        return destination
    }

    private fun validateFormat(mimeType: String?, extension: String?): Result<Unit> {
        if (mimeType == null || mimeType.lowercase() !in SUPPORTED_MIME_TYPES) {
            return Result.failure(unsupportedFormatError())
        }
        if (extension == null || extension !in SUPPORTED_EXTENSIONS) {
            return Result.failure(unsupportedFormatError())
        }
        return Result.success(Unit)
    }

    private fun validateFileSize(file: File): Throwable? {
        if (file.length() <= 0L) {
            return IllegalArgumentException(
                "Unable to read the selected photos. The selected photos were not added.",
            )
        }
        if (file.length() > MAX_PHOTO_SIZE_BYTES) {
            return IllegalArgumentException(
                "Photo exceeds the 5 MB size limit. The selected photos were not added.",
            )
        }
        return null
    }

    private fun extensionFor(mimeType: String?, uri: Uri): String? {
        mimeType?.lowercase()?.let { mime ->
            MIME_TO_EXTENSION[mime]?.let { return it }
        }
        return uri.lastPathSegment
            ?.substringAfterLast('.', missingDelimiterValue = "")
            ?.lowercase()
            ?.takeIf { it in SUPPORTED_EXTENSIONS }
    }

    private fun uriForFile(file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.$FILE_PROVIDER_SUFFIX",
            file,
        )
    }

    private fun deleteCopied(copied: List<CopiedPhoto>) {
        copied.forEach { it.file.delete() }
    }

    private fun unsupportedFormatError(): IllegalArgumentException {
        return IllegalArgumentException(
            "Only JPEG, PNG, and WebP photos are supported. The selected photos were not added.",
        )
    }

    private data class CopiedPhoto(
        val displayUri: Uri,
        val file: File,
    )

    private companion object {
        const val PHOTO_CACHE_DIR = "incident_photos"
        const val PHOTO_FILE_PREFIX = "incident_photo_"
        const val FILE_PROVIDER_SUFFIX = "provider"
        val SUPPORTED_MIME_TYPES = setOf("image/jpeg", "image/jpg", "image/png", "image/webp")
        val SUPPORTED_EXTENSIONS = setOf("jpg", "jpeg", "png", "webp")
        val MIME_TO_EXTENSION = mapOf(
            "image/jpeg" to "jpg",
            "image/jpg" to "jpg",
            "image/png" to "png",
            "image/webp" to "webp",
        )
    }
}
