package com.hp77.linkstash.presentation.addlink

import com.hp77.linkstash.domain.model.Tag

data class AddEditLinkScreenState(
    val url: String = "",
    val title: String? = null,
    val description: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isUrlError: Boolean = false,
    val selectedTags: List<Tag> = emptyList(),
    val availableTags: List<Tag> = emptyList(),
    val newTagName: String = "",
    val isEditMode: Boolean = false,
    val linkId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val isArchived: Boolean = false,
    val reminderTime: Long? = null,
    val saved: Boolean = false,
    val notes: String? = null,
    val showDeleteTagDialog: Boolean = false,
    val tagToDelete: Tag? = null,
    val tagDeleteAffectedLinks: Int = 0
)
