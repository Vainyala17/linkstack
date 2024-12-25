package com.hp77.linkstash.presentation.addlink

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hp77.linkstash.domain.model.Link
import com.hp77.linkstash.domain.model.Tag
import com.hp77.linkstash.domain.usecase.link.AddLinkUseCase
import com.hp77.linkstash.domain.usecase.link.UpdateLinkUseCase
import com.hp77.linkstash.domain.usecase.tag.ManageTagsUseCase
import com.hp77.linkstash.domain.usecase.tag.TagFilter
import com.hp77.linkstash.domain.usecase.tag.TagOperation
import com.hp77.linkstash.domain.repository.LinkRepository
import com.hp77.linkstash.util.DateUtils
import com.hp77.linkstash.util.ReminderManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddEditLinkViewModel @Inject constructor(
    private val addLinkUseCase: AddLinkUseCase,
    private val updateLinkUseCase: UpdateLinkUseCase,
    private val manageTagsUseCase: ManageTagsUseCase,
    private val linkRepository: LinkRepository,
    private val reminderManager: ReminderManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(AddEditLinkScreenState())
    val state: StateFlow<AddEditLinkScreenState> = _state.asStateFlow()
    
    private val _navigateBack = MutableStateFlow(false)
    val navigateBack: StateFlow<Boolean> = _navigateBack.asStateFlow()

    init {
        loadTags()
        // Check if we're in edit mode by looking for linkId parameter
        savedStateHandle.get<String>("linkId")?.let { linkId ->
            onEvent(AddEditLinkScreenEvent.OnInitializeEdit(linkId))
        }
    }

    private fun loadTags() {
        viewModelScope.launch {
            manageTagsUseCase.getTags(TagFilter.All).collect { tags ->
                _state.update { it.copy(availableTags = tags.map { tag -> tag.name }) }
            }
        }
    }

    fun onEvent(event: AddEditLinkScreenEvent) {
        when (event) {
            is AddEditLinkScreenEvent.OnUrlChange -> {
                _state.update { it.copy(
                    url = event.url,
                    isUrlError = false
                ) }
            }
            is AddEditLinkScreenEvent.OnTitleChange -> {
                _state.update { it.copy(title = event.title) }
            }
            is AddEditLinkScreenEvent.OnDescriptionChange -> {
                _state.update { it.copy(description = event.description) }
            }
            is AddEditLinkScreenEvent.OnTagSelect -> {
                _state.update { it.copy(
                    selectedTags = it.selectedTags + event.tag
                ) }
            }
            is AddEditLinkScreenEvent.OnTagDeselect -> {
                _state.update { it.copy(
                    selectedTags = it.selectedTags - event.tag
                ) }
            }
            is AddEditLinkScreenEvent.OnNewTagNameChange -> {
                _state.update { it.copy(newTagName = event.name) }
            }
            is AddEditLinkScreenEvent.OnTagAdd -> {
                viewModelScope.launch {
                    try {
                        val tag = manageTagsUseCase(TagOperation.GetOrCreate(event.tag))
                        if (tag == null) {
                            _state.update { it.copy(
                                error = "Failed to create tag"
                            ) }
                            return@launch
                        }
                        _state.update { it.copy(
                            availableTags = it.availableTags + event.tag,
                            selectedTags = it.selectedTags + event.tag
                        ) }
                    } catch (e: Exception) {
                        _state.update { it.copy(
                            error = "Failed to create tag: ${e.message}"
                        ) }
                    }
                }
            }
            is AddEditLinkScreenEvent.OnInitializeEdit -> {
                viewModelScope.launch {
                    _state.update { it.copy(isLoading = true) }
                    try {
                        val link = linkRepository.getLinkById(event.linkId)
                        if (link != null) {
                            _state.update { it.copy(
                                isEditMode = true,
                                linkId = link.id,
                                url = link.url,
                                title = link.title,
                                description = link.description,
                                selectedTags = link.tags.map { tag -> tag.name },
                                createdAt = link.createdAt,
                                isFavorite = link.isFavorite,
                                isArchived = link.isArchived,
                                reminderTime = link.reminderTime
                            ) }
                        } else {
                            _state.update { it.copy(error = "Link not found") }
                        }
                    } catch (e: Exception) {
                        _state.update { it.copy(error = "Failed to load link: ${e.message}") }
                    } finally {
                        _state.update { it.copy(isLoading = false) }
                    }
                }
            }
            AddEditLinkScreenEvent.OnToggleFavorite -> {
                _state.update { it.copy(isFavorite = !it.isFavorite) }
            }
            AddEditLinkScreenEvent.OnToggleArchive -> {
                _state.update { it.copy(isArchived = !it.isArchived) }
            }
            AddEditLinkScreenEvent.OnSave -> saveLink()
            AddEditLinkScreenEvent.OnErrorDismiss -> {
                _state.update { it.copy(error = null) }
            }
            AddEditLinkScreenEvent.OnNavigateBack -> {
                // Handled by UI
            }
            is AddEditLinkScreenEvent.OnSetReminder -> {
                Log.d("AddEditLinkViewModel", "Setting reminder for timestamp: ${event.timestamp}")
                _state.update { it.copy(reminderTime = event.timestamp) }
            }
            AddEditLinkScreenEvent.OnRemoveReminder -> {
                Log.d("AddEditLinkViewModel", "Removing reminder")
                _state.update { it.copy(reminderTime = null) }
                state.value.linkId?.let { linkId ->
                    Log.d("AddEditLinkViewModel", "Cancelling reminder for link: $linkId")
                    reminderManager.cancelReminder(linkId)
                }
            }
        }
    }

    private fun saveLink() {
        val currentState = state.value

        if (currentState.url.isBlank()) {
            _state.update { it.copy(
                isUrlError = true,
                error = "URL cannot be empty"
            ) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            try {
                // Create and get all tags first
                val tags = mutableListOf<Tag>()
                for (tagName in currentState.selectedTags) {
                    val tag = manageTagsUseCase(TagOperation.GetOrCreate(tagName))
                    if (tag != null) {
                        tags.add(tag)
                    }
                }

                // Create or update link with the collected tags
                val link = Link(
                    id = currentState.linkId ?: UUID.randomUUID().toString(),
                    url = currentState.url,
                    title = currentState.title?.takeIf { it.isNotBlank() },
                    description = currentState.description?.takeIf { it.isNotBlank() },
                    previewImageUrl = null,
                    createdAt = currentState.createdAt,
                    reminderTime = currentState.reminderTime,
                    isArchived = currentState.isArchived,
                    isFavorite = currentState.isFavorite,
                    tags = tags
                )
                
                // Cancel any existing reminder first
                currentState.linkId?.let { linkId ->
                    reminderManager.cancelReminder(linkId)
                }

                if (currentState.isEditMode) {
                    updateLinkUseCase(link)
                } else {
                    addLinkUseCase(link)
                }

            // Schedule new reminder if set
            if (link.reminderTime != null) {
                Log.d("AddEditLinkViewModel", "Scheduling reminder for link: ${link.id}, time: ${link.reminderTime}")
                reminderManager.scheduleReminder(link)
                
                // Observe reminder status
                viewModelScope.launch {
                    reminderManager.observeReminderStatus(link.id)
                        .collect { state ->
                            Log.d("AddEditLinkViewModel", """
                                Reminder status update for link ${link.id}:
                                - State: $state
                                - Scheduled time: ${DateUtils.formatDateTime(link.reminderTime)}
                            """.trimIndent())
                        }
                }
            }
                _navigateBack.value = true
            } catch (e: Exception) {
                _state.update { it.copy(
                    error = e.message ?: "Failed to save link"
                ) }
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
}
