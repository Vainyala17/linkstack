package com.hp77.linkstash.presentation.addlink

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import com.hp77.linkstash.presentation.components.CustomTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hp77.linkstash.domain.model.Tag
import com.hp77.linkstash.presentation.components.ReminderSection
import com.hp77.linkstash.presentation.components.TagChips

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditLinkScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddEditLinkViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val navigateBack by viewModel.navigateBack.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    LaunchedEffect(navigateBack) {
        if (navigateBack) {
            onNavigateBack()
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.onEvent(AddEditLinkScreenEvent.OnErrorDismiss)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditMode) "Edit Link" else "Add Link") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.onEvent(AddEditLinkScreenEvent.OnNavigateBack)
                        onNavigateBack()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                },
                actions = {
                    if (state.isEditMode) {
                        IconButton(
                            onClick = { viewModel.onEvent(AddEditLinkScreenEvent.OnToggleFavorite) }
                        ) {
                            Icon(
                                imageVector = if (state.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = if (state.isFavorite) "Remove from favorites" else "Add to favorites"
                            )
                        }
                        IconButton(
                            onClick = { viewModel.onEvent(AddEditLinkScreenEvent.OnToggleArchive) }
                        ) {
                            Icon(
                                imageVector = if (state.isArchived) Icons.Default.Unarchive else Icons.Default.Archive,
                                contentDescription = if (state.isArchived) "Unarchive" else "Archive"
                            )
                        }
                    }
                    IconButton(
                        onClick = { viewModel.onEvent(AddEditLinkScreenEvent.OnSave) },
                        enabled = !state.isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Save link"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.padding(16.dp)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            ) {
                val clipboardManager = LocalClipboardManager.current
                CustomTextField(
                    value = state.url,
                    onValueChange = { viewModel.onEvent(AddEditLinkScreenEvent.OnUrlChange(it)) },
                    label = { Text("URL") },
                    isError = state.isUrlError,
                    supportingText = if (state.isUrlError) {
                        { Text("URL cannot be empty") }
                    } else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onKeyEvent { event ->
                            if (event.type == KeyEventType.KeyDown &&
                                event.key == Key.V &&
                                (event.isCtrlPressed || event.isMetaPressed)
                            ) {
                                clipboardManager.getText()?.text?.let { pastedText ->
                                    viewModel.onEvent(AddEditLinkScreenEvent.OnUrlChange(pastedText))
                                }
                                true
                            } else false
                        },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = androidx.compose.ui.text.input.ImeAction.Done
                    ),
                    shape = MaterialTheme.shapes.medium
                )

                Spacer(modifier = Modifier.height(16.dp))

                CustomTextField(
                    value = state.title ?: "",
                    onValueChange = { viewModel.onEvent(AddEditLinkScreenEvent.OnTitleChange(it)) },
                    label = { Text("Title (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )

                Spacer(modifier = Modifier.height(16.dp))

                CustomTextField(
                    value = state.description ?: "",
                    onValueChange = { viewModel.onEvent(AddEditLinkScreenEvent.OnDescriptionChange(it)) },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    shape = MaterialTheme.shapes.medium
                )

                Spacer(modifier = Modifier.height(16.dp))

                ReminderSection(
                    reminderTime = state.reminderTime,
                    onSetReminder = { timestamp ->
                        viewModel.onEvent(AddEditLinkScreenEvent.OnSetReminder(timestamp))
                    },
                    onRemoveReminder = {
                        viewModel.onEvent(AddEditLinkScreenEvent.OnRemoveReminder)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Tags",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                CustomTextField(
                    value = state.newTagName,
                    onValueChange = { viewModel.onEvent(AddEditLinkScreenEvent.OnNewTagNameChange(it)) },
                    label = { Text("New Tag") },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                state.newTagName.takeIf { it.isNotBlank() }?.let { tagName ->
                                    viewModel.onEvent(AddEditLinkScreenEvent.OnTagAdd(tagName))
                                    viewModel.onEvent(AddEditLinkScreenEvent.OnNewTagNameChange(""))
                                }
                            },
                            enabled = state.newTagName.isNotBlank()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add tag"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )

                Spacer(modifier = Modifier.height(16.dp))

                TagChips(
                    tags = state.availableTags.map { tagName -> 
                        Tag(
                            id = tagName, // Using tag name as ID for simplicity
                            name = tagName,
                            color = null,
                            createdAt = 0L
                        )
                    },
                    selectedTags = state.selectedTags.map { tagName ->
                        Tag(
                            id = tagName,
                            name = tagName,
                            color = null,
                            createdAt = 0L
                        )
                    },
                    onTagClick = { tag ->
                        if (tag.name in state.selectedTags) {
                            viewModel.onEvent(AddEditLinkScreenEvent.OnTagDeselect(tag.name))
                        } else {
                            viewModel.onEvent(AddEditLinkScreenEvent.OnTagSelect(tag.name))
                        }
                    }
                )
            }
        }
    }
}
