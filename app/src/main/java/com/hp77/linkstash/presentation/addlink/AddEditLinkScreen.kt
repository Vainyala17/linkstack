package com.hp77.linkstash.presentation.addlink

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hp77.linkstash.presentation.components.ReminderSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditLinkScreen(
    onNavigateBack: () -> Unit,
    linkId: String? = null,
    viewModel: AddEditLinkViewModel = hiltViewModel()
) {
    LaunchedEffect(linkId) {
        linkId?.let { id ->
            viewModel.onEvent(AddEditLinkScreenEvent.OnInitializeEdit(id))
        }
    }
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.error) {
        state.error?.let { error ->
            // Show error snackbar
        }
    }

    LaunchedEffect(state.saved) {
        if (state.saved) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text(if (state.isEditMode) "Edit Link" else "Add Link") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
                .padding(16.dp),
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

            Button(
                onClick = { viewModel.onEvent(AddEditLinkScreenEvent.OnSave) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        }
    }
}
