package com.example.incidentreporter.presentation.editincident

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.incidentreporter.R
import com.example.incidentreporter.domain.model.IncidentCategory
import com.example.incidentreporter.presentation.components.BlockingLoadingOverlay
import com.example.incidentreporter.presentation.createincident.MAX_DESCRIPTION_LENGTH
import com.example.incidentreporter.presentation.createincident.MAX_PHOTOS
import com.example.incidentreporter.presentation.editincident.components.EditPhotoPickerSection
import com.example.incidentreporter.presentation.incidentlist.IncidentLoadError
import com.example.incidentreporter.presentation.incidentlist.components.ActionResultDialog
import com.example.incidentreporter.presentation.theme.FieldIncidentReporterTheme
import com.example.incidentreporter.presentation.theme.FormFieldBorder
import com.example.incidentreporter.presentation.theme.FormRequiredAsterisk
import com.example.incidentreporter.presentation.theme.FormSubmitDisabled
import com.example.incidentreporter.presentation.theme.HomeBackground
import com.example.incidentreporter.presentation.theme.HomeCard
import com.example.incidentreporter.presentation.theme.HomeFabBlue
import com.example.incidentreporter.presentation.theme.HomeNavy
import com.example.incidentreporter.presentation.theme.HomeSkeleton
import com.example.incidentreporter.presentation.theme.HomeSwipeRemove
import com.example.incidentreporter.presentation.theme.HomeTextMuted
import com.example.incidentreporter.presentation.theme.HomeTextPrimary
import com.example.incidentreporter.presentation.theme.HomeTextSecondary
import kotlinx.coroutines.launch

@Composable
fun EditIncidentScreen(
    incidentId: String,
    onBack: () -> Unit,
    viewModel: EditIncidentViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarScope = rememberCoroutineScope()

    LaunchedEffect(incidentId) {
        viewModel.loadIncident(incidentId)
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                EditIncidentUiEvent.NavigateBack -> onBack()
                is EditIncidentUiEvent.ShowPhotoError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is EditIncidentUiEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    EditIncidentScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onBack = viewModel::onCancel,
        onRetry = viewModel::retryLoad,
        onTitleChanged = viewModel::onTitleChanged,
        onCategoryChanged = viewModel::onCategoryChanged,
        onOtherCategoryDescriptionChanged = viewModel::onOtherCategoryDescriptionChanged,
        onDescriptionChanged = viewModel::onDescriptionChanged,
        onLocationChanged = viewModel::onLocationChanged,
        onPhotosPicked = viewModel::onPhotosSelected,
        onPhotoRemoved = viewModel::onPhotoRemoved,
        onPhotoPickerError = { message ->
            snackbarScope.launch {
                snackbarHostState.showSnackbar(message)
            }
        },
        onDismissSubmitError = viewModel::dismissSubmitError,
        onSave = viewModel::save,
        onCancel = viewModel::onCancel,
    )
}

@Composable
internal fun EditIncidentScreenContent(
    uiState: EditIncidentUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onTitleChanged: (String) -> Unit,
    onCategoryChanged: (IncidentCategory) -> Unit,
    onOtherCategoryDescriptionChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onLocationChanged: (String) -> Unit,
    onPhotosPicked: (List<Uri>) -> Unit,
    onPhotoRemoved: (EditPhotoItem) -> Unit,
    onPhotoPickerError: (String) -> Unit,
    onDismissSubmitError: () -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val loadError = uiState.loadError

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = HomeBackground,
            contentWindowInsets = WindowInsets.navigationBars,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                EditIncidentTopBar(
                    onBack = onBack,
                    enabled = !uiState.isSubmitting && !uiState.isProcessingPhotos,
                )
            },
        ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when {
                uiState.isLoading -> {
                    EditIncidentLoading(modifier = Modifier.fillMaxSize())
                }

                loadError != null -> {
                    EditIncidentError(
                        loadError = loadError,
                        onRetry = onRetry,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                else -> {
                    EditIncidentForm(
                        uiState = uiState,
                        onTitleChanged = onTitleChanged,
                        onCategoryChanged = onCategoryChanged,
                        onOtherCategoryDescriptionChanged = onOtherCategoryDescriptionChanged,
                        onDescriptionChanged = onDescriptionChanged,
                        onLocationChanged = onLocationChanged,
                        onPhotosPicked = onPhotosPicked,
                        onPhotoRemoved = onPhotoRemoved,
                        onPhotoPickerError = onPhotoPickerError,
                        onSave = onSave,
                        onCancel = onCancel,
                    )
                }
            }
        }
        }

        BlockingLoadingOverlay(
            visible = uiState.isSubmitting,
            contentDescription = stringResource(R.string.edit_submitting),
        )
    }

    uiState.submitError?.let { submitError ->
        ActionResultDialog(
            icon = null,
            title = stringResource(R.string.edit_failed_title),
            message = submitError,
            dismissLabel = stringResource(R.string.edit_failed_close),
            confirmLabel = stringResource(R.string.edit_failed_retry),
            confirmColor = HomeFabBlue,
            onDismiss = onDismissSubmitError,
            onConfirm = onSave,
        )
    }
}

@Composable
private fun EditIncidentForm(
    uiState: EditIncidentUiState,
    onTitleChanged: (String) -> Unit,
    onCategoryChanged: (IncidentCategory) -> Unit,
    onOtherCategoryDescriptionChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onLocationChanged: (String) -> Unit,
    onPhotosPicked: (List<Uri>) -> Unit,
    onPhotoRemoved: (EditPhotoItem) -> Unit,
    onPhotoPickerError: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    val fieldsEnabled = !uiState.isSubmitting
    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(HomeCard)
                .padding(horizontal = 16.dp, vertical = 20.dp),
        ) {
            RequiredFieldLabel(text = stringResource(R.string.create_field_title))
            Spacer(modifier = Modifier.height(8.dp))
            EditTextField(
                value = uiState.title,
                onValueChange = onTitleChanged,
                placeholder = stringResource(R.string.create_field_title_placeholder),
                error = uiState.titleError,
                enabled = fieldsEnabled,
            )

            Spacer(modifier = Modifier.height(16.dp))
            RequiredFieldLabel(text = stringResource(R.string.create_field_category))
            Spacer(modifier = Modifier.height(8.dp))
            CategoryDropdown(
                selectedCategory = uiState.category,
                error = uiState.categoryError,
                enabled = fieldsEnabled,
                onCategorySelected = onCategoryChanged,
            )

            if (uiState.category == IncidentCategory.OTHER) {
                Spacer(modifier = Modifier.height(16.dp))
                RequiredFieldLabel(text = stringResource(R.string.create_field_other_category))
                Spacer(modifier = Modifier.height(8.dp))
                EditTextField(
                    value = uiState.otherCategoryDescription,
                    onValueChange = onOtherCategoryDescriptionChanged,
                    placeholder = stringResource(R.string.create_field_other_category_placeholder),
                    error = uiState.otherCategoryDescriptionError,
                    enabled = fieldsEnabled,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            RequiredFieldLabel(text = stringResource(R.string.create_field_description))
            Spacer(modifier = Modifier.height(8.dp))
            EditTextField(
                value = uiState.description,
                onValueChange = onDescriptionChanged,
                placeholder = stringResource(R.string.create_field_description_placeholder),
                error = null,
                enabled = fieldsEnabled,
                singleLine = false,
                minLines = 5,
                isError = uiState.descriptionError != null,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val descriptionError = uiState.descriptionError
                if (descriptionError != null) {
                    Text(
                        text = descriptionError,
                        modifier = Modifier.weight(1f),
                        color = HomeSwipeRemove,
                        fontSize = 12.sp,
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
                Text(
                    text = stringResource(
                        R.string.create_description_counter,
                        uiState.description.length,
                        MAX_DESCRIPTION_LENGTH,
                    ),
                    color = HomeTextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.End,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            RequiredFieldLabel(text = stringResource(R.string.create_field_location))
            Spacer(modifier = Modifier.height(8.dp))
            EditTextField(
                value = uiState.location,
                onValueChange = onLocationChanged,
                placeholder = stringResource(R.string.create_field_location_placeholder),
                error = uiState.locationError,
                enabled = fieldsEnabled,
            )

            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.create_field_photos),
                    modifier = Modifier.weight(1f),
                    color = HomeTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringResource(
                        R.string.create_photos_counter,
                        uiState.visiblePhotoCount,
                        MAX_PHOTOS,
                    ),
                    color = HomeTextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.End,
                )
            }
            val remainingPhotoSlots = (MAX_PHOTOS - uiState.visiblePhotoCount).coerceAtLeast(0)
            if (remainingPhotoSlots > 0) {
                Text(
                    text = stringResource(
                        R.string.create_photos_remaining,
                        remainingPhotoSlots,
                    ),
                    modifier = Modifier.padding(top = 4.dp),
                    color = HomeTextMuted,
                    fontSize = 12.sp,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            EditPhotoPickerSection(
                photoItems = uiState.photoItems,
                photosLocked = uiState.photosLocked,
                enabled = fieldsEnabled && !uiState.isProcessingPhotos,
                onPhotosPicked = onPhotosPicked,
                onPhotoRemoved = onPhotoRemoved,
                onPickerError = onPhotoPickerError,
            )
        }

        Button(
            onClick = onSave,
            enabled = uiState.canSave,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp)
                .height(48.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = HomeFabBlue,
                disabledContainerColor = FormSubmitDisabled,
                contentColor = Color.White,
                disabledContentColor = Color.White,
            ),
        ) {
            Text(
                text = stringResource(R.string.edit_save),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Button(
            onClick = onCancel,
            enabled = !uiState.isSubmitting && !uiState.isProcessingPhotos,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .height(48.dp),
            shape = RoundedCornerShape(10.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = FormSubmitDisabled,
                disabledContainerColor = FormSubmitDisabled.copy(alpha = 0.6f),
                contentColor = HomeFabBlue,
                disabledContentColor = HomeFabBlue.copy(alpha = 0.5f),
            ),
        ) {
            Text(
                text = stringResource(R.string.edit_cancel),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun EditIncidentTopBar(
    onBack: () -> Unit,
    enabled: Boolean = true,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(HomeNavy)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack, enabled = enabled) {
            Icon(
                painter = painterResource(R.drawable.ic_create_back),
                contentDescription = stringResource(R.string.create_back),
                tint = Color.White,
            )
        }
        Text(
            text = stringResource(R.string.edit_title),
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun EditIncidentLoading(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = HomeFabBlue)
    }
}

@Composable
private fun EditIncidentError(
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
        IncidentLoadError.Network -> stringResource(R.string.details_error_message)
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
        modifier = modifier.padding(horizontal = 32.dp),
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
            text = stringResource(R.string.edit_error_title),
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
                text = stringResource(R.string.details_retry),
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun RequiredFieldLabel(
    text: String,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = text,
            color = HomeTextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = " *",
            color = FormRequiredAsterisk,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun EditTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    error: String?,
    enabled: Boolean,
    singleLine: Boolean = true,
    minLines: Int = 1,
    isError: Boolean = error != null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        placeholder = {
            Text(
                text = placeholder,
                color = HomeTextMuted,
                fontSize = 14.sp,
            )
        },
        supportingText = error?.let {
            {
                Text(text = it)
            }
        },
        isError = isError,
        singleLine = singleLine,
        minLines = minLines,
        shape = RoundedCornerShape(10.dp),
        colors = editTextFieldColors(),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(
    selectedCategory: IncidentCategory?,
    error: String?,
    enabled: Boolean,
    onCategorySelected: (IncidentCategory) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = selectedCategory?.let { categoryLabel(it) }.orEmpty()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = it },
    ) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled),
            enabled = enabled,
            readOnly = true,
            placeholder = {
                Text(
                    text = stringResource(R.string.create_field_category_placeholder),
                    color = HomeTextMuted,
                    fontSize = 14.sp,
                )
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            supportingText = error?.let {
                {
                    Text(text = it)
                }
            },
            isError = error != null,
            shape = RoundedCornerShape(10.dp),
            colors = editTextFieldColors(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            IncidentCategory.entries.forEach { category ->
                DropdownMenuItem(
                    text = { Text(text = categoryLabel(category)) },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun categoryLabel(category: IncidentCategory): String {
    return stringResource(
        when (category) {
            IncidentCategory.EQUIPMENT_DAMAGE -> R.string.create_category_equipment_damage
            IncidentCategory.ROAD_HAZARD -> R.string.create_category_road_hazard
            IncidentCategory.SAFETY_ISSUE -> R.string.create_category_safety_issue
            IncidentCategory.INFRASTRUCTURE -> R.string.create_category_infrastructure
            IncidentCategory.OTHER -> R.string.create_category_other
        },
    )
}

@Composable
private fun editTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = HomeNavy,
    unfocusedBorderColor = FormFieldBorder,
    disabledBorderColor = FormFieldBorder,
    errorBorderColor = HomeSwipeRemove,
    focusedTextColor = HomeTextPrimary,
    unfocusedTextColor = HomeTextPrimary,
    cursorColor = HomeNavy,
    focusedContainerColor = HomeCard,
    unfocusedContainerColor = HomeCard,
    disabledContainerColor = HomeCard,
)

@Preview(name = "Edit Incident", showBackground = true, showSystemUi = true)
@Composable
private fun EditIncidentPreview() {
    FieldIncidentReporterTheme(dynamicColor = false) {
        EditIncidentScreenContent(
            uiState = EditIncidentUiState(
                isLoading = false,
                title = "Broken Air Conditioning Unit",
                category = IncidentCategory.EQUIPMENT_DAMAGE,
                description = "Water leaking from the indoor unit near the reception area.",
                location = "Building A – Reception",
            ),
            onBack = {},
            onRetry = {},
            onTitleChanged = {},
            onCategoryChanged = {},
            onOtherCategoryDescriptionChanged = {},
            onDescriptionChanged = {},
            onLocationChanged = {},
            onPhotosPicked = {},
            onPhotoRemoved = {},
            onPhotoPickerError = {},
            onDismissSubmitError = {},
            onSave = {},
            onCancel = {},
        )
    }
}
