package com.example.incidentreporter.presentation.createincident

import android.net.Uri
import com.example.incidentreporter.domain.model.IncidentCategory
import com.example.incidentreporter.domain.model.Photo
import com.example.incidentreporter.domain.usecase.CreateIncidentUseCase
import com.example.incidentreporter.domain.usecase.UploadPhotosUseCase
import com.example.incidentreporter.testing.FakeIncidentRepository
import com.example.incidentreporter.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class CreateIncidentViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val photoManager = mockk<IncidentPhotoManager>(relaxUnitFun = true)

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun initialState_isIdleAndNotSubmittable() {
        val viewModel = createViewModel(FakeIncidentRepository())

        val state = viewModel.uiState.value
        assertFalse(state.isSubmitting)
        assertFalse(state.isProcessingPhotos)
        assertFalse(state.canSubmit)
        assertNull(state.submitError)
        assertTrue(state.selectedPhotoUris.isEmpty())
    }

    @Test
    fun submit_invalidFormSetsFieldErrors() = runViewModelTest {
        val repository = FakeIncidentRepository()
        val viewModel = createViewModel(repository)

        viewModel.submit()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Title is required.", state.titleError)
        assertEquals("Please select a category.", state.categoryError)
        assertEquals("Description is required.", state.descriptionError)
        assertEquals("Location is required.", state.locationError)
        assertFalse(state.isSubmitting)
        assertEquals(0, repository.createIncidentCalls)
    }

    @Test
    fun submit_otherCategoryRequiresDescription() = runViewModelTest {
        val repository = FakeIncidentRepository()
        val viewModel = createViewModel(repository)
        viewModel.fillValidForm()
        viewModel.onCategoryChanged(IncidentCategory.OTHER)

        viewModel.submit()
        advanceUntilIdle()

        assertEquals(
            "Please describe the category.",
            viewModel.uiState.value.otherCategoryDescriptionError,
        )
        assertEquals(0, repository.createIncidentCalls)
    }

    @Test
    fun onDescriptionChanged_capsLengthAt500() {
        val viewModel = createViewModel(FakeIncidentRepository())

        viewModel.onDescriptionChanged("a".repeat(MAX_DESCRIPTION_LENGTH + 25))

        assertEquals(MAX_DESCRIPTION_LENGTH, viewModel.uiState.value.description.length)
    }

    @Test
    fun submit_successNavigatesAndResetsForm() = runViewModelTest {
        val repository = FakeIncidentRepository()
        val viewModel = createViewModel(repository)
        val events = collectEvents(viewModel)
        viewModel.fillValidForm()

        viewModel.submit()
        advanceUntilIdle()

        val createdId = repository.lastCreateIncidentRequest?.id
        assertEquals("Broken Air Conditioning Unit", repository.lastCreateIncidentRequest?.title)
        assertEquals(IncidentCategory.EQUIPMENT_DAMAGE, repository.lastCreateIncidentRequest?.category)
        assertEquals(emptyList<Photo>(), repository.lastCreateIncidentRequest?.photos)
        assertEquals(
            listOf(CreateIncidentUiEvent.NavigateToIncidentCreated(requireNotNull(createdId))),
            events,
        )
        val reset = viewModel.uiState.value
        assertEquals(CreateIncidentUiState(), reset)
        assertFalse(reset.isSubmitting)
    }

    @Test
    fun submit_trimsFieldsAndOmitsOtherDescriptionWhenNotOther() = runViewModelTest {
        val repository = FakeIncidentRepository()
        val viewModel = createViewModel(repository)
        viewModel.onTitleChanged("  Title  ")
        viewModel.onCategoryChanged(IncidentCategory.SAFETY_ISSUE)
        viewModel.onOtherCategoryDescriptionChanged(" ignored ")
        viewModel.onDescriptionChanged("  Description  ")
        viewModel.onLocationChanged("  Location  ")

        viewModel.submit()
        advanceUntilIdle()

        val request = repository.lastCreateIncidentRequest
        assertEquals("Title", request?.title)
        assertEquals("Description", request?.description)
        assertEquals("Location", request?.location)
        assertEquals(null, request?.otherCategoryDescription)
    }

    @Test
    fun submit_createFailureSetsSubmitError() = runViewModelTest {
        val repository = FakeIncidentRepository()
        repository.createIncidentResult = Result.failure(IllegalStateException("Create failed"))
        val viewModel = createViewModel(repository)
        viewModel.fillValidForm()

        viewModel.submit()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSubmitting)
        assertEquals("Create failed", state.submitError)
        assertEquals("Broken Air Conditioning Unit", state.title)
    }

    @Test
    fun submit_duplicateSubmissionIsGuarded() = runViewModelTest {
        val repository = FakeIncidentRepository()
        val viewModel = createViewModel(repository)
        viewModel.fillValidForm()

        viewModel.submit()
        viewModel.submit()
        advanceUntilIdle()

        assertEquals(1, repository.createIncidentCalls)
    }

    @Test
    fun onPhotosSelected_overLimitEmitsErrorWithoutCopying() = runViewModelTest {
        val viewModel = createViewModel(FakeIncidentRepository())
        val events = collectEvents(viewModel)
        val uris = List(MAX_PHOTOS + 1) { mockk<Uri>(relaxed = true) }

        viewModel.onPhotosSelected(uris)
        advanceUntilIdle()

        assertEquals(
            listOf(
                CreateIncidentUiEvent.ShowPhotoError(
                    "A maximum of $MAX_PHOTOS photos can be attached. The selected photos were not added.",
                ),
            ),
            events,
        )
        assertTrue(viewModel.uiState.value.selectedPhotoUris.isEmpty())
        coVerify(exactly = 0) { photoManager.copyAndValidate(any()) }
    }

    @Test
    fun onPhotosSelected_successAddsCopiedUris() = runViewModelTest {
        val uri = mockk<Uri>(relaxed = true)
        coEvery { photoManager.copyAndValidate(listOf(uri)) } returns Result.success(listOf(uri))
        val viewModel = createViewModel(FakeIncidentRepository())

        viewModel.onPhotosSelected(listOf(uri))
        advanceUntilIdle()

        assertEquals(listOf(uri), viewModel.uiState.value.selectedPhotoUris)
        assertFalse(viewModel.uiState.value.isProcessingPhotos)
    }

    @Test
    fun submit_uploadFailureSetsSubmitErrorAndDoesNotCreate() = runViewModelTest {
        val repository = FakeIncidentRepository()
        val uri = mockk<Uri>(relaxed = true)
        val file = File.createTempFile("incident_photo_", ".jpg").apply { deleteOnExit() }
        coEvery { photoManager.copyAndValidate(any()) } returns Result.success(listOf(uri))
        every { photoManager.filesFor(any()) } returns listOf(file)
        repository.uploadPhotosResult = Result.failure(IllegalStateException("Upload failed"))
        val viewModel = createViewModel(repository)
        viewModel.fillValidForm()
        viewModel.onPhotosSelected(listOf(uri))
        advanceUntilIdle()

        viewModel.submit()
        advanceUntilIdle()

        assertEquals("Upload failed", viewModel.uiState.value.submitError)
        assertFalse(viewModel.uiState.value.isSubmitting)
        assertFalse(viewModel.uiState.value.photosLocked)
        assertEquals(0, repository.createIncidentCalls)
        assertEquals(1, repository.uploadPhotosCalls)
    }

    @Test
    fun submit_emptyUploadResponseSetsSubmitError() = runViewModelTest {
        val repository = FakeIncidentRepository()
        val uri = mockk<Uri>(relaxed = true)
        val file = File.createTempFile("incident_photo_", ".jpg").apply { deleteOnExit() }
        coEvery { photoManager.copyAndValidate(any()) } returns Result.success(listOf(uri))
        every { photoManager.filesFor(any()) } returns listOf(file)
        repository.uploadPhotosResult = Result.success(emptyList())
        val viewModel = createViewModel(repository)
        viewModel.fillValidForm()
        viewModel.onPhotosSelected(listOf(uri))
        advanceUntilIdle()

        viewModel.submit()
        advanceUntilIdle()

        assertEquals(
            "Photo upload did not return any photos. Please try again.",
            viewModel.uiState.value.submitError,
        )
        assertEquals(0, repository.createIncidentCalls)
    }

    @Test
    fun dismissSubmitError_clearsMessage() = runViewModelTest {
        val repository = FakeIncidentRepository()
        repository.createIncidentResult = Result.failure(IllegalStateException("Create failed"))
        val viewModel = createViewModel(repository)
        viewModel.fillValidForm()
        viewModel.submit()
        advanceUntilIdle()

        viewModel.dismissSubmitError()

        assertNull(viewModel.uiState.value.submitError)
    }

    private fun CreateIncidentViewModel.fillValidForm() {
        onTitleChanged("Broken Air Conditioning Unit")
        onCategoryChanged(IncidentCategory.EQUIPMENT_DAMAGE)
        onDescriptionChanged("Water leaking from the indoor unit near the reception area.")
        onLocationChanged("Building A - Reception")
    }

    private fun createViewModel(
        repository: FakeIncidentRepository,
    ): CreateIncidentViewModel {
        return CreateIncidentViewModel(
            photoManager = photoManager,
            createIncidentUseCase = CreateIncidentUseCase(repository),
            uploadPhotosUseCase = UploadPhotosUseCase(repository),
        )
    }

    private fun TestScope.collectEvents(
        viewModel: CreateIncidentViewModel,
    ): MutableList<CreateIncidentUiEvent> {
        val events = mutableListOf<CreateIncidentUiEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.collect { events.add(it) }
        }
        return events
    }

    private fun runViewModelTest(
        body: suspend TestScope.() -> Unit,
    ) = runTest(mainDispatcherRule.dispatcher, testBody = body)
}
