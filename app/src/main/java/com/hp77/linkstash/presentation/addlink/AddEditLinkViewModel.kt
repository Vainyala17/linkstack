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
import com.hp77.linkstash.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
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
        Logger.d("AddEditLinkVM", "Initializing ViewModel")
        // Load available tags
        viewModelScope.launch {
            val allTags = tagRepository.getAllTags().first()
            Logger.d("AddEditLinkVM", "Loaded ${allTags.size} available tags")
            // Filter out duplicate tags (case-insensitive)
            val uniqueTags = allTags.distinctBy { it.name.lowercase() }
            Logger.d("AddEditLinkVM", "Filtered to ${uniqueTags.size} unique tags")
            _state.update { it.copy(availableTags = uniqueTags) }
        }

        // Initialize edit mode if linkId is provided
        savedStateHandle.get<String>("linkId")?.let { linkId ->
            Logger.d("AddEditLinkVM", "Initializing edit mode with linkId: $linkId")
            viewModelScope.launch {
                val link = linkRepository.getLinkById(linkId)
                if (link != null) {
                    Logger.d("AddEditLinkVM", "Found link to edit: ${link.url}")
                    _state.update { it.copy(
                        url = link.url,
                        title = link.title,
                        description = link.description,
                        notes = link.notes,
                        reminderTime = link.reminderTime,
                        selectedTags = link.tags,
                        isEditMode = true,
                        linkId = linkId
                    ) }
                } else {
                    Logger.e("AddEditLinkVM", "Link not found for id: $linkId")
                }
            }
        }
    }

    fun onEvent(event: AddEditLinkScreenEvent) {
        Logger.d("AddEditLinkVM", "Received event: ${event::class.simpleName}")
        when (event) {
            is AddEditLinkScreenEvent.OnUrlChange -> {
                viewModelScope.launch {
                    val url = event.url
                    _state.update { it.copy(url = url) }
                    
                    // Only check for duplicates if URL is not empty and we're not in edit mode
                    // or if we're in edit mode but the URL has changed from the original
                    if (url.isNotEmpty() && (!state.value.isEditMode || url != linkRepository.getLinkById(state.value.linkId!!)?.url)) {
                        val existingLink = linkRepository.getLinkByUrl(url)
                        if (existingLink != null) {
                            _state.update { it.copy(
                                isUrlError = true,
                                error = "This URL already exists in your links"
                            ) }
                        } else {
                            _state.update { it.copy(
                                isUrlError = false,
                                error = null
                            ) }
                        }
                    }
                }
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
                    // Check if tag already exists in selected tags (case-insensitive)
                    val tagAlreadySelected = _state.value.selectedTags.any { 
                        it.name.equals(tag.name, ignoreCase = true) 
                    }
                    if (!tagAlreadySelected) {
                        _state.update { currentState ->
                            currentState.copy(
                                selectedTags = currentState.selectedTags + tag
                            )
                        }
                    }
                }
            }
            is AddEditLinkScreenEvent.OnTagDeselect -> {
                val deselectedTag = _state.value.selectedTags.find { it.name == event.tagName }
                deselectedTag?.let { tag ->
                    _state.update { currentState ->
                        currentState.copy(
                            selectedTags = currentState.selectedTags - tag
                        )
                    }
                }
            }
            is AddEditLinkScreenEvent.OnTagAdd -> {
                if (_state.value.newTagName.isNotEmpty()) {
                    viewModelScope.launch {
                        // Check if tag with same name (case-insensitive) already exists in selected tags
                        val tagAlreadySelected = _state.value.selectedTags.any { 
                            it.name.equals(_state.value.newTagName.trim(), ignoreCase = true) 
                        }
                        
                        if (!tagAlreadySelected) {
                            val newTag = tagRepository.getOrCreateTag(_state.value.newTagName)
                            _state.update { currentState ->
                                currentState.copy(
                                    selectedTags = currentState.selectedTags + newTag,
                                    availableTags = currentState.availableTags + newTag,
                                    newTagName = "" // Reset the input field
                                )
                            }
                        } else {
                            // Just clear the input field if tag already exists
                            _state.update { it.copy(newTagName = "") }
                        }
                    }
                }
            }
            is AddEditLinkScreenEvent.OnNewTagNameChange -> {
                _state.update { it.copy(newTagName = event.name) }
            }
            is AddEditLinkScreenEvent.OnInitializeEdit -> {
                Logger.d("AddEditLinkVM", "Initializing edit for linkId: ${event.linkId}")
                viewModelScope.launch {
                    val link = linkRepository.getLinkById(event.linkId)
                    if (link != null) {
                        Logger.d("AddEditLinkVM", "Successfully loaded link for editing")
                        _state.update { currentState ->
                            currentState.copy(
                                url = link.url,
                                title = link.title,
                                description = link.description,
                                notes = link.notes,
                                reminderTime = link.reminderTime,
                                selectedTags = link.tags,
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
            is AddEditLinkScreenEvent.OnInitiateTagDelete -> {
                viewModelScope.launch {
                    val tagToDelete = _state.value.availableTags.find { it.name == event.tagName }
                    tagToDelete?.let { tag ->
                        try {
                            // Get the number of links using this tag
                            val affectedLinks = tagRepository.getLinkedLinksCount(tag.id)
                            
                            _state.update { currentState ->
                                currentState.copy(
                                    showDeleteTagDialog = true,
                                    tagToDelete = tag,
                                    tagDeleteAffectedLinks = affectedLinks
                                )
                            }
                            Logger.d("AddEditLinkVM", "Showing delete dialog for tag: ${tag.name}, affects $affectedLinks links")
                        } catch (e: Exception) {
                            Logger.e("AddEditLinkVM", "Error checking tag links: ${e.message}", e)
                            _state.update { it.copy(error = "Failed to check tag usage: ${e.message}") }
                        }
                    }
                }
            }
            is AddEditLinkScreenEvent.OnConfirmTagDelete -> {
                viewModelScope.launch {
                    _state.value.tagToDelete?.let { tag ->
                        try {
                            tagRepository.deleteTag(tag)
                            Logger.d("AddEditLinkVM", "Successfully deleted tag: ${tag.name}")
                            
                            // Remove from available tags and selected tags
                            _state.update { currentState ->
                                currentState.copy(
                                    availableTags = currentState.availableTags.filter { it.id != tag.id },
                                    selectedTags = currentState.selectedTags.filter { it.id != tag.id },
                                    showDeleteTagDialog = false,
                                    tagToDelete = null,
                                    tagDeleteAffectedLinks = 0
                                )
                            }
                        } catch (e: Exception) {
                            Logger.e("AddEditLinkVM", "Error deleting tag: ${e.message}", e)
                            _state.update { it.copy(
                                error = "Failed to delete tag: ${e.message}",
                                showDeleteTagDialog = false,
                                tagToDelete = null,
                                tagDeleteAffectedLinks = 0
                            ) }
                        }
                    }
                }
            }
            is AddEditLinkScreenEvent.OnDismissTagDeleteDialog -> {
                _state.update { it.copy(
                    showDeleteTagDialog = false,
                    tagToDelete = null,
                    tagDeleteAffectedLinks = 0
                ) }
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
                Logger.d("AddEditLinkVM", "Attempting to save link")
                viewModelScope.launch {
                    try {
                        // Check for duplicate URL before saving
                        val existingLink = linkRepository.getLinkByUrl(state.value.url)
                        if (existingLink != null && (!state.value.isEditMode || existingLink.id != state.value.linkId)) {
                            _state.update { it.copy(
                                error = "This URL already exists in your links",
                                isUrlError = true
                            ) }
                            return@launch
                        }

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
                                val error = result.exceptionOrNull() ?: Exception("Unknown error")
                                Logger.e("AddEditLinkVM", "Failed to update link", error)
                                throw error
                            }
                            Logger.d("AddEditLinkVM", "Successfully updated link")
                        } else {
                            val newId = UUID.randomUUID().toString()
                            Logger.d("AddEditLinkVM", "Generated new UUID for link: $newId")
                            val result = addLinkUseCase(
                                Link(
                                    id = newId,
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
                                val error = result.exceptionOrNull() ?: Exception("Unknown error")
                                Logger.e("AddEditLinkVM", "Failed to add link", error)
                                throw error
                            }
                            Logger.d("AddEditLinkVM", "Successfully added new link")
                        }
                        _state.update { it.copy(saved = true) }
                        Logger.d("AddEditLinkVM", "Updated state: saved=true")
                    } catch (e: Exception) {
                        Logger.e("AddEditLinkVM", "Error saving link: ${e.message}", e)
                        _state.update { it.copy(error = e.message) }
                    }
                }
            }
        }
    }
}
