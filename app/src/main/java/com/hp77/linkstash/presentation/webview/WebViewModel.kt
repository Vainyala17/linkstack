package com.hp77.linkstash.presentation.webview

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hp77.linkstash.data.local.dao.LinkDao
import com.hp77.linkstash.data.mapper.toLink
import com.hp77.linkstash.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WebViewModel @Inject constructor(
    private val linkDao: LinkDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _state = MutableStateFlow(WebViewScreenState())
    val state: StateFlow<WebViewScreenState> = _state

    init {
        savedStateHandle.get<String>("linkId")?.let { linkId ->
            viewModelScope.launch {
                try {
                    linkDao.getLinkWithTags(linkId)?.let { linkWithTags ->
                        _state.update { it.copy(
                            link = linkWithTags.toLink(),
                            isLoading = false
                        ) }
                    } ?: run {
                        _state.update { it.copy(
                            error = "Link not found",
                            isLoading = false
                        ) }
                    }
                } catch (e: Exception) {
                    Logger.e("WebViewModel", "Error loading link", e)
                    _state.update { it.copy(
                        error = e.message ?: "Unknown error",
                        isLoading = false
                    ) }
                }
            }
        }
    }

    fun onEvent(event: WebViewScreenEvent) {
        when (event) {
            is WebViewScreenEvent.OnScrollPositionChanged -> {
                viewModelScope.launch {
                    _state.value.link?.id?.let { linkId ->
                        try {
                            linkDao.updateScrollPosition(linkId, event.position)
                        } catch (e: Exception) {
                            Logger.e("WebViewModel", "Error updating scroll position", e)
                            _state.update { it.copy(error = e.message) }
                        }
                    }
                }
            }
            is WebViewScreenEvent.OnErrorDismiss -> {
                _state.update { it.copy(error = null) }
            }
        }
    }
}
