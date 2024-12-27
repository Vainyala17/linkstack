package com.hp77.linkstash.presentation.addlink

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hp77.linkstash.presentation.components.ReminderSection
import com.hp77.linkstash.presentation.components.TagChips
import com.hp77.linkstash.domain.model.Tag
import androidx.compose.ui.Alignment
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import com.hp77.linkstash.util.Logger

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditLinkScreen(
    onNavigateBack: () -> Unit,
    linkId: String? = null,
    viewModel: AddEditLinkViewModel = hiltViewModel()
) {
    Logger.d("AddEditLinkScreen", "Screen initialized with linkId: $linkId")
    
    LaunchedEffect(linkId) {
        linkId?.let { id ->
            Logger.d("AddEditLinkScreen", "Initializing edit mode for linkId: $id")
            viewModel.onEvent(AddEditLinkScreenEvent.OnInitializeEdit(id))
        }
    }
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.error) {
        state.error?.let { error ->
            Logger.e("AddEditLinkScreen", "Error occurred: $error")
            // Show error snackbar
        }
    }

    LaunchedEffect(state.saved) {
        if (state.saved) {
            Logger.d("AddEditLinkScreen", "Link saved successfully, navigating back")
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text(if (state.isEditMode) "Edit Link" else "Add Link") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            Logger.d("AddEditLinkScreen", "Back button clicked")
                            onNavigateBack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = state.url,
                onValueChange = { viewModel.onEvent(AddEditLinkScreenEvent.OnUrlChange(it)) },
                label = { Text("URL") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.title ?: "",
                onValueChange = { viewModel.onEvent(AddEditLinkScreenEvent.OnTitleChange(it)) },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.description ?: "",
                onValueChange = { viewModel.onEvent(AddEditLinkScreenEvent.OnDescriptionChange(it)) },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            OutlinedTextField(
                value = state.notes ?: "",
                onValueChange = { viewModel.onEvent(AddEditLinkScreenEvent.OnNotesChange(it)) },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            ReminderSection(
                reminderTime = state.reminderTime,
                onSetReminder = { viewModel.onEvent(AddEditLinkScreenEvent.OnSetReminder(it)) },
                onRemoveReminder = { viewModel.onEvent(AddEditLinkScreenEvent.OnRemoveReminder) }
            )

            // Tags Section
            Column {
                Text(
                    text = "Tags",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // Selected Tags
                if (state.selectedTags.isNotEmpty()) {
                    TagChips(
                        tags = state.selectedTags,
                        onTagClick = { tag -> viewModel.onEvent(AddEditLinkScreenEvent.OnTagDeselect(tagName = tag.name)) },
                        onTagRemove = { tag -> viewModel.onEvent(AddEditLinkScreenEvent.OnTagDeselect(tagName = tag.name)) },
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                
                // Available Tags
                if (state.availableTags.isNotEmpty()) {
                    TagChips(
                        tags = state.availableTags,
                        selectedTags = state.selectedTags,
                        onTagClick = { tag -> viewModel.onEvent(AddEditLinkScreenEvent.OnTagSelect(tagName = tag.name)) }
                    )
                }
                
                // Add New Tag
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = state.newTagName,
                        onValueChange = { viewModel.onEvent(AddEditLinkScreenEvent.OnNewTagNameChange(it)) },
                        label = { Text("New Tag") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    )
                    Button(
                        onClick = { 
                            viewModel.onEvent(AddEditLinkScreenEvent.OnTagAdd)
                        },
                        enabled = state.newTagName.isNotEmpty()
                    ) {
                        Text("Add")
                    }
                }
            }

            Button(
                onClick = { 
                    Logger.d("AddEditLinkScreen", "Save button clicked")
                    viewModel.onEvent(AddEditLinkScreenEvent.OnSave) 
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        }
    }
}
