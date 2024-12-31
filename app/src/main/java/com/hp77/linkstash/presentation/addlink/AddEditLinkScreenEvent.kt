package com.hp77.linkstash.presentation.addlink

sealed class AddEditLinkScreenEvent {
    data class OnUrlChange(val url: String) : AddEditLinkScreenEvent()
    data class OnTitleChange(val title: String) : AddEditLinkScreenEvent()
    data class OnDescriptionChange(val description: String) : AddEditLinkScreenEvent()
    data class OnNotesChange(val notes: String) : AddEditLinkScreenEvent()
    data class OnTagSelect(val tagName: String) : AddEditLinkScreenEvent()
    data class OnTagDeselect(val tagName: String) : AddEditLinkScreenEvent()
    data class OnInitiateTagDelete(val tagName: String) : AddEditLinkScreenEvent()
    object OnConfirmTagDelete : AddEditLinkScreenEvent()
    object OnDismissTagDeleteDialog : AddEditLinkScreenEvent()
    object OnTagAdd : AddEditLinkScreenEvent()
    data class OnNewTagNameChange(val name: String) : AddEditLinkScreenEvent()
    data class OnInitializeEdit(val linkId: String) : AddEditLinkScreenEvent()
    data class OnReminderTimeChange(val time: Long?) : AddEditLinkScreenEvent()
    data class OnSetReminder(val timestamp: Long) : AddEditLinkScreenEvent()
    object OnRemoveReminder : AddEditLinkScreenEvent()
    object OnToggleFavorite : AddEditLinkScreenEvent()
    object OnToggleArchive : AddEditLinkScreenEvent()
    object OnSave : AddEditLinkScreenEvent()
    object OnErrorDismiss : AddEditLinkScreenEvent()
    object OnNavigateBack : AddEditLinkScreenEvent()
}
