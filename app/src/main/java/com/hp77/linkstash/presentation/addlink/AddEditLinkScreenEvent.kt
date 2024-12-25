package com.hp77.linkstash.presentation.addlink

sealed class AddEditLinkScreenEvent {
    data class OnUrlChange(val url: String) : AddEditLinkScreenEvent()
    data class OnTitleChange(val title: String) : AddEditLinkScreenEvent()
    data class OnDescriptionChange(val description: String) : AddEditLinkScreenEvent()
    data class OnTagSelect(val tag: String) : AddEditLinkScreenEvent()
    data class OnTagDeselect(val tag: String) : AddEditLinkScreenEvent()
    data class OnTagAdd(val tag: String) : AddEditLinkScreenEvent()
    data class OnNewTagNameChange(val name: String) : AddEditLinkScreenEvent()
    data class OnInitializeEdit(val linkId: String) : AddEditLinkScreenEvent()
    data class OnSetReminder(val timestamp: Long) : AddEditLinkScreenEvent()
    object OnRemoveReminder : AddEditLinkScreenEvent()
    object OnToggleFavorite : AddEditLinkScreenEvent()
    object OnToggleArchive : AddEditLinkScreenEvent()
    object OnSave : AddEditLinkScreenEvent()
    object OnErrorDismiss : AddEditLinkScreenEvent()
    object OnNavigateBack : AddEditLinkScreenEvent()
}
