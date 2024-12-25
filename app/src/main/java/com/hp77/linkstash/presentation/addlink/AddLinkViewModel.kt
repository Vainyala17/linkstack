package com.hp77.linkstash.presentation.addlink

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hp77.linkstash.domain.model.Link
import com.hp77.linkstash.domain.model.Tag
import com.hp77.linkstash.domain.usecase.link.AddLinkUseCase
import com.hp77.linkstash.domain.usecase.tag.ManageTagsUseCase
import com.hp77.linkstash.domain.usecase.tag.TagFilter
import com.hp77.linkstash.domain.usecase.tag.TagOperation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddLinkViewModel @Inject constructor(
    private val addLinkUseCase: AddLinkUseCase,
    private val manageTagsUseCase: ManageTagsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AddLinkScreenState())
    val state: StateFlow<AddLinkScreenState> = _state.asStateFlow()
    
    private val _navigateBack = MutableStateFlow(false)
    val navigateBack: StateFlow<Boolean> = _navigateBack.asStateFlow()

    init {
        loadTags()
    }

    private fun loadTags() {
        viewModelScope.launch {
            manageTagsUseCase.getTags(TagFilter.All).collect { tags ->
                _state.update { it.copy(availableTags = tags.map { tag -> tag.name }) }
            }
        }
    }

    fun onEvent(event: AddLinkScreenEvent) {
        when (event) {
            is AddLinkScreenEvent.OnUrlChange -> {
                _state.update { it.copy(
                    url = event.url,
                    isUrlError = false
                ) }
            }
            is AddLinkScreenEvent.OnTitleChange -> {
                _state.update { it.copy(title = event.title) }
            }
            is AddLinkScreenEvent.OnDescriptionChange -> {
                _state.update { it.copy(description = event.description) }
            }
            is AddLinkScreenEvent.OnTagSelect -> {
                _state.update { it.copy(
                    selectedTags = it.selectedTags + event.tag
                ) }
            }
            is AddLinkScreenEvent.OnTagDeselect -> {
                _state.update { it.copy(
                    selectedTags = it.selectedTags - event.tag
                ) }
            }
            is AddLinkScreenEvent.OnNewTagNameChange -> {
                _state.update { it.copy(newTagName = event.name) }
            }
            is AddLinkScreenEvent.OnTagAdd -> {
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
            AddLinkScreenEvent.OnSave -> saveLink()
            AddLinkScreenEvent.OnErrorDismiss -> {
                _state.update { it.copy(error = null) }
            }
            AddLinkScreenEvent.OnNavigateBack -> {
                // Handled by UI
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

                // Create link with the collected tags
                val link = Link(
                    id = UUID.randomUUID().toString(),
                    url = currentState.url,
                    title = currentState.title?.takeIf { it.isNotBlank() },
                    description = currentState.description?.takeIf { it.isNotBlank() },
                    previewImageUrl = null,
                    createdAt = System.currentTimeMillis(),
                    reminderTime = null,
                    isArchived = false,
                    isFavorite = false,
                    tags = tags
                )
                
                addLinkUseCase(link)
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
