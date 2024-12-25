package com.hp77.linkstash.presentation.addlink

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hp77.linkstash.domain.model.Link
import com.hp77.linkstash.domain.model.Tag
import com.hp77.linkstash.domain.usecase.link.AddLinkUseCase
import com.hp77.linkstash.domain.usecase.link.UpdateLinkUseCase
import com.hp77.linkstash.domain.repository.LinkRepository
import com.hp77.linkstash.domain.repository.TagRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditLinkViewModel @Inject constructor(
    private val addLinkUseCase: AddLinkUseCase,
    private val updateLinkUseCase: UpdateLinkUseCase,
    private val linkRepository: LinkRepository,
    private val tagRepository: TagRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(AddEditLinkScreenState())
    val state: StateFlow<AddEditLinkScreenState> = _state

    init {
        // Load available tags
        viewModelScope.launch {
            val allTags = tagRepository.getAllTags().first()
            _state.update { it.copy(availableTags = allTags) }
        }

        // Initialize edit mode if linkId is provided
        savedStateHandle.get<String>("linkId")?.let { linkId ->
            viewModelScope.launch {
                linkRepository.getLinkById(linkId)?.let { link ->
                    _state.update { it.copy(
                        url = link.url,
                        title = link.title,
                        description = link.description,
                        notes = link.notes,
                        reminderTime = link.reminderTime,
                        selectedTags = link.tags,
                        availableTags = _state.value.availableTags.filter { availableTag ->
                            !link.tags.any { it.id == availableTag.id }
                        },
                        isEditMode = true,
                        linkId = linkId
                    ) }
                }
            }
        }
    }

    fun onEvent(event: AddEditLinkScreenEvent) {
        when (event) {
            is AddEditLinkScreenEvent.OnUrlChange -> {
                _state.update { it.copy(url = event.url) }
            }
            is AddEditLinkScreenEvent.OnTitleChange -> {
                _state.update { it.copy(title = event.title.ifEmpty { null }) }
            }
            is AddEditLinkScreenEvent.OnDescriptionChange -> {
                _state.update { it.copy(description = event.description.ifEmpty { null }) }
            }
            is AddEditLinkScreenEvent.OnNotesChange -> {
                _state.update { it.copy(notes = event.notes.ifEmpty { null }) }
            }
            is AddEditLinkScreenEvent.OnReminderTimeChange -> {
                _state.update { it.copy(reminderTime = event.time) }
            }
            is AddEditLinkScreenEvent.OnSetReminder -> {
                _state.update { it.copy(reminderTime = event.timestamp) }
            }
            is AddEditLinkScreenEvent.OnRemoveReminder -> {
                _state.update { it.copy(reminderTime = null) }
            }
            is AddEditLinkScreenEvent.OnTagSelect -> {
                val selectedTag = _state.value.availableTags.find { it.name == event.tagName }
                selectedTag?.let { tag ->
                    _state.update { currentState ->
                        currentState.copy(
                            selectedTags = currentState.selectedTags + tag,
                            availableTags = currentState.availableTags - tag
                        )
                    }
                }
            }
            is AddEditLinkScreenEvent.OnTagDeselect -> {
                val deselectedTag = _state.value.selectedTags.find { it.name == event.tagName }
                deselectedTag?.let { tag ->
                    _state.update { currentState ->
                        currentState.copy(
                            selectedTags = currentState.selectedTags - tag,
                            availableTags = currentState.availableTags + tag
                        )
                    }
                }
            }
            is AddEditLinkScreenEvent.OnTagAdd -> {
                if (_state.value.newTagName.isNotEmpty()) {
                    viewModelScope.launch {
                        val newTag = tagRepository.getOrCreateTag(_state.value.newTagName)
                        _state.update { currentState ->
                            currentState.copy(
                                selectedTags = currentState.selectedTags + newTag,
                                newTagName = "" // Reset the input field
                            )
                        }
                    }
                }
            }
            is AddEditLinkScreenEvent.OnNewTagNameChange -> {
                _state.update { it.copy(newTagName = event.name) }
            }
            is AddEditLinkScreenEvent.OnInitializeEdit -> {
                viewModelScope.launch {
                    linkRepository.getLinkById(event.linkId)?.let { link ->
                        _state.update { currentState ->
                            currentState.copy(
                                url = link.url,
                                title = link.title,
                                description = link.description,
                                notes = link.notes,
                                reminderTime = link.reminderTime,
                                selectedTags = link.tags,
                                availableTags = currentState.availableTags.filter { availableTag ->
                                    !link.tags.any { it.id == availableTag.id }
                                },
                                isEditMode = true,
                                linkId = event.linkId
                            )
                        }
                    }
                }
            }
            is AddEditLinkScreenEvent.OnToggleFavorite -> {
                // TODO: Implement favorite toggle
            }
            is AddEditLinkScreenEvent.OnToggleArchive -> {
                // TODO: Implement archive toggle
            }
            is AddEditLinkScreenEvent.OnErrorDismiss -> {
                _state.update { it.copy(error = null) }
            }
            is AddEditLinkScreenEvent.OnNavigateBack -> {
                // Handled by the UI
            }
            is AddEditLinkScreenEvent.OnSave -> {
                viewModelScope.launch {
                    try {
                        if (state.value.isEditMode) {
                            val result = updateLinkUseCase(
                                Link(
                                    id = state.value.linkId!!,
                                    url = state.value.url,
                                    title = state.value.title,
                                    description = state.value.description,
                                    previewImageUrl = null,
                                    createdAt = System.currentTimeMillis(),
                                    reminderTime = state.value.reminderTime,
                                    isArchived = false,
                                    isFavorite = false,
                                    notes = state.value.notes,
                                    tags = state.value.selectedTags
                                )
                            )
                            if (result.isFailure) {
                                throw result.exceptionOrNull() ?: Exception("Unknown error")
                            }
                        } else {
                            val result = addLinkUseCase(
                                Link(
                                    id = "",
                                    url = state.value.url,
                                    title = state.value.title,
                                    description = state.value.description,
                                    previewImageUrl = null,
                                    createdAt = System.currentTimeMillis(),
                                    reminderTime = state.value.reminderTime,
                                    isArchived = false,
                                    isFavorite = false,
                                    notes = state.value.notes,
                                    tags = state.value.selectedTags
                                )
                            )
                            if (result.isFailure) {
                                throw result.exceptionOrNull() ?: Exception("Unknown error")
                            }
                        }
                        _state.update { it.copy(saved = true) }
                    } catch (e: Exception) {
                        _state.update { it.copy(error = e.message) }
                    }
                }
            }
        }
    }
}
