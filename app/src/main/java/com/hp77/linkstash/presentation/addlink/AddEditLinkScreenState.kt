package com.hp77.linkstash.presentation.addlink

data class AddEditLinkScreenState(
    val url: String = "",
    val title: String? = null,
    val description: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isUrlError: Boolean = false,
    val selectedTags: List<String> = emptyList(),
    val availableTags: List<String> = emptyList(),
    val newTagName: String = "",
    val isEditMode: Boolean = false,
    val linkId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val isArchived: Boolean = false,
    val reminderTime: Long? = null
)
