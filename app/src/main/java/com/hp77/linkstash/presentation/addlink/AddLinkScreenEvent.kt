package com.hp77.linkstash.presentation.addlink

sealed class AddLinkScreenEvent {
    data class OnUrlChange(val url: String) : AddLinkScreenEvent()
    data class OnTitleChange(val title: String) : AddLinkScreenEvent()
    data class OnDescriptionChange(val description: String) : AddLinkScreenEvent()
    data class OnTagSelect(val tag: String) : AddLinkScreenEvent()
    data class OnTagDeselect(val tag: String) : AddLinkScreenEvent()
    data class OnTagAdd(val tag: String) : AddLinkScreenEvent()
    data class OnNewTagNameChange(val name: String) : AddLinkScreenEvent()
    object OnSave : AddLinkScreenEvent()
    object OnErrorDismiss : AddLinkScreenEvent()
    object OnNavigateBack : AddLinkScreenEvent()
}
