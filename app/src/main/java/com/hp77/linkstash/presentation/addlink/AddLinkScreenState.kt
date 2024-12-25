package com.hp77.linkstash.presentation.addlink

data class AddLinkScreenState(
    val url: String = "",
    val title: String? = null,
    val description: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isUrlError: Boolean = false,
    val selectedTags: List<String> = emptyList(),
    val availableTags: List<String> = emptyList(),
    val newTagName: String = ""
)
