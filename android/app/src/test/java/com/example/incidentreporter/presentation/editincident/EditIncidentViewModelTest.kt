package com.example.incidentreporter.presentation.editincident

import android.net.Uri
import com.example.incidentreporter.domain.model.IncidentCategory
import com.example.incidentreporter.domain.model.IncidentStatus
import com.example.incidentreporter.domain.model.Photo
import com.example.incidentreporter.domain.usecase.DeletePhotoUseCase
import com.example.incidentreporter.domain.usecase.GetIncidentByIdUseCase
import com.example.incidentreporter.domain.usecase.UpdateIncidentUseCase
import com.example.incidentreporter.domain.usecase.UploadPhotosUseCase
import com.example.incidentreporter.presentation.createincident.IncidentPhotoManager
import com.example.incidentreporter.presentation.createincident.MAX_PHOTOS
import com.example.incidentreporter.presentation.incidentlist.IncidentLoadError
import com.example.incidentreporter.testing.FakeIncidentRepository
import com.example.incidentreporter.testing.IncidentFixtures
import com.example.incidentreporter.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
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
class EditIncidentViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val photoManager = mockk<IncidentPhotoManager>(relaxUnitFun = true)

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun initialState_isLoading() {
        val viewModel = createViewModel(FakeIncidentRepository())

        assertTrue(viewModel.uiState.value.isLoading)
        assertFalse(viewModel.uiState.value.canSave)
    }

    @Test
    fun loadIncident_blankIdSetsUnexpectedError() {
        val viewModel = createViewModel(FakeIncidentRepository())

        viewModel.loadIncident("   ")

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(IncidentLoadError.Unexpected, state.loadError)
    }

    @Test
    fun loadIncident_successPopulatesForm() = runViewModelTest {
        val repository = FakeIncidentRepository()
        val photos = listOf(Photo(id = "photo-1", url = "https://example.test/photo-1.jpg"))
        val incident = IncidentFixtures.incident(photos = photos)
        repository.incidents[incident.id] = incident
        val viewModel = createViewModel(repository)

        viewModel.loadIncident(incident.id)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.loadError)
        assertEquals(incident.id, state.incidentId)
        assertEquals(incident.title, state.title)
        assertEquals(incident.category, state.category)
        assertEquals(incident.description, state.description)
        assertEquals(incident.location, state.location)
        assertEquals(photos, state.existingPhotos)
        assertTrue(state.canSave)
    }

    @Test
    fun loadIncident_completedIncidentIsNotEditable() = runViewModelTest {
        val repository = FakeIncidentRepository()
        val incident = IncidentFixtures.incident(status = IncidentStatus.COMPLETED)
        repository.incidents[incident.id] = incident
        val viewModel = createViewModel(repository)

        viewModel.loadIncident(incident.id)
        advanceUntilIdle()

        assertEquals(
            IncidentLoadError.Api("This incident can no longer be edited."),
            viewModel.uiState.value.loadError,
        )
        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.title.isEmpty())
    }

    @Test
    fun loadIncident_cancelledIncidentIsNotEditable() = runViewModelTest {
        val repository = FakeIncidentRepository()
        val incident = IncidentFixtures.incident(status = IncidentStatus.CANCELLED)
        repository.incidents[incident.id] = incident
        val viewModel = createViewModel(repository)

        viewModel.loadIncident(incident.id)
        advanceUntilIdle()

        assertEquals(
            IncidentLoadError.Api("This incident can no longer be edited."),
            viewModel.uiState.value.loadError,
        )
    }

    @Test
    fun loadIncident_failureAllowsRetry() = runViewModelTest {
        val repository = FakeIncidentRepository()
        val incident = IncidentFixtures.incident()
        repository.getIncidentResult = Result.failure(IllegalStateException("Network down"))
        val viewModel = createViewModel(repository)

        viewModel.loadIncident(incident.id)
        advanceUntilIdle()

        assertEquals(IncidentLoadError.Unexpected, viewModel.uiState.value.loadError)

        repository.getIncidentResult = null
        repository.incidents[incident.id] = incident
        viewModel.retryLoad()
        advanceUntilIdle()

        assertEquals(incident.title, viewModel.uiState.value.title)
        assertNull(viewModel.uiState.value.loadError)
        assertEquals(2, repository.getIncidentCalls)
    }

    @Test
    fun save_invalidFormSetsFieldErrors() = runViewModelTest {
        val repository = FakeIncidentRepository()
        val incident = IncidentFixtures.incident()
        repository.incidents[incident.id] = incident
        val viewModel = createViewModel(repository)
        viewModel.loadIncident(incident.id)
        advanceUntilIdle()
        viewModel.onTitleChanged(" ")
        viewModel.onDescriptionChanged(" ")
        viewModel.onLocationChanged(" ")
        viewModel.onCategoryChanged(IncidentCategory.OTHER)

        viewModel.save()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Title is required.", state.titleError)
        assertEquals("Please describe the category.", state.otherCategoryDescriptionError)
        assertEquals("Description is required.", state.descriptionError)
        assertEquals("Location is required.", state.locationError)
        assertEquals(0, repository.updateIncidentCalls)
    }

    @Test
    fun save_successNavigatesBack() = runViewModelTest {
        val repository = FakeIncidentRepository()
        val incident = IncidentFixtures.incident()
        repository.incidents[incident.id] = incident
        val viewModel = createViewModel(repository)
        val events = collectEvents(viewModel)
        viewModel.loadIncident(incident.id)
        advanceUntilIdle()
        viewModel.onTitleChanged("Updated title")

        viewModel.save()
        advanceUntilIdle()

        assertEquals(listOf(EditIncidentUiEvent.NavigateBack), events)
        assertEquals("Updated title", repository.lastUpdateIncidentRequest?.title)
        assertEquals(incident.id, repository.lastUpdateIncidentId)
        assertEquals(1, repository.updateIncidentCalls)
    }

    @Test
    fun save_failureSetsSubmitErrorAndPreservesForm() = runViewModelTest {
        val repository = FakeIncidentRepository()
        val incident = IncidentFixtures.incident()
        repository.incidents[incident.id] = incident
        repository.updateIncidentResult = Result.failure(IllegalStateException("Update failed"))
        val viewModel = createViewModel(repository)
        viewModel.loadIncident(incident.id)
        advanceUntilIdle()

        viewModel.save()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSubmitting)
        assertEquals("Update failed", state.submitError)
        assertEquals(incident.title, state.title)
    }

    @Test
    fun save_duplicateSubmissionIsGuarded() = runViewModelTest {
        val repository = FakeIncidentRepository()
        val incident = IncidentFixtures.incident()
        repository.incidents[incident.id] = incident
        val viewModel = createViewModel(repository)
        viewModel.loadIncident(incident.id)
        advanceUntilIdle()

        viewModel.save()
        viewModel.save()
        advanceUntilIdle()

        assertEquals(1, repository.updateIncidentCalls)
    }

    @Test
    fun save_whileLoadingIsNoOp() {
        val repository = FakeIncidentRepository()
        val viewModel = createViewModel(repository)

        viewModel.save()

        assertEquals(0, repository.updateIncidentCalls)
    }

    @Test
    fun save_removedExistingPhotoIsDeletedAfterSuccessfulUpdate() = runViewModelTest {
        val repository = FakeIncidentRepository()
        val photo = Photo(id = "photo-1", url = "https://example.test/photo-1.jpg")
        val incident = IncidentFixtures.incident(photos = listOf(photo))
        repository.incidents[incident.id] = incident
        val viewModel = createViewModel(repository)
        viewModel.loadIncident(incident.id)
        advanceUntilIdle()

        viewModel.onPhotoRemoved(EditPhotoItem.Existing(photo))
        viewModel.save()
        advanceUntilIdle()

        assertEquals(emptyList<Photo>(), repository.lastUpdateIncidentRequest?.photos)
        assertEquals(1, repository.deletePhotoCalls)
        assertEquals("photo-1", repository.lastDeletedPhotoId)
    }

    @Test
    fun onPhotosSelected_overLimitEmitsErrorWithoutCopying() = runViewModelTest {
        val repository = FakeIncidentRepository()
        val incident = IncidentFixtures.incident()
        repository.incidents[incident.id] = incident
        val viewModel = createViewModel(repository)
        val events = collectEvents(viewModel)
        viewModel.loadIncident(incident.id)
        advanceUntilIdle()
        val uris = List(MAX_PHOTOS + 1) { mockk<Uri>(relaxed = true) }

        viewModel.onPhotosSelected(uris)
        advanceUntilIdle()

        assertEquals(
            listOf(
                EditIncidentUiEvent.ShowPhotoError(
                    "A maximum of $MAX_PHOTOS photos can be attached. The selected photos were not added.",
                ),
            ),
            events,
        )
        coVerify(exactly = 0) { photoManager.copyAndValidate(any()) }
    }

    @Test
    fun save_uploadFailureSetsSubmitErrorAndDoesNotUpdate() = runViewModelTest {
        val repository = FakeIncidentRepository()
        val incident = IncidentFixtures.incident()
        repository.incidents[incident.id] = incident
        val uri = mockk<Uri>(relaxed = true)
        val file = File.createTempFile("incident_photo_", ".jpg").apply { deleteOnExit() }
        coEvery { photoManager.copyAndValidate(any()) } returns Result.success(listOf(uri))
        every { photoManager.filesFor(any()) } returns listOf(file)
        repository.uploadPhotosResult = Result.failure(IllegalStateException("Upload failed"))
        val viewModel = createViewModel(repository)
        viewModel.loadIncident(incident.id)
        advanceUntilIdle()
        viewModel.onPhotosSelected(listOf(uri))
        advanceUntilIdle()

        viewModel.save()
        advanceUntilIdle()

        assertEquals("Upload failed", viewModel.uiState.value.submitError)
        assertEquals(0, repository.updateIncidentCalls)
        assertEquals(1, repository.uploadPhotosCalls)
    }

    @Test
    fun onCancel_clearsPhotosAndNavigatesBack() = runViewModelTest {
        val repository = FakeIncidentRepository()
        val incident = IncidentFixtures.incident()
        repository.incidents[incident.id] = incident
        val viewModel = createViewModel(repository)
        val events = collectEvents(viewModel)
        viewModel.loadIncident(incident.id)
        advanceUntilIdle()

        viewModel.onCancel()
        advanceUntilIdle()

        assertEquals(listOf(EditIncidentUiEvent.NavigateBack), events)
        verify { photoManager.clear() }
    }

    private fun createViewModel(
        repository: FakeIncidentRepository,
    ): EditIncidentViewModel {
        return EditIncidentViewModel(
            photoManager = photoManager,
            getIncidentByIdUseCase = GetIncidentByIdUseCase(repository),
            uploadPhotosUseCase = UploadPhotosUseCase(repository),
            updateIncidentUseCase = UpdateIncidentUseCase(repository),
            deletePhotoUseCase = DeletePhotoUseCase(repository),
        )
    }

    private fun TestScope.collectEvents(
        viewModel: EditIncidentViewModel,
    ): MutableList<EditIncidentUiEvent> {
        val events = mutableListOf<EditIncidentUiEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.collect { events.add(it) }
        }
        return events
    }

    private fun runViewModelTest(
        body: suspend TestScope.() -> Unit,
    ) = runTest(mainDispatcherRule.dispatcher, testBody = body)
}
