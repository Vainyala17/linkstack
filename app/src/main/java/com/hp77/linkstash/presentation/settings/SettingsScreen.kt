package com.hp77.linkstash.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hp77.linkstash.presentation.home.HomeScreenEvent
import com.hp77.linkstash.presentation.home.HomeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    onNavigateToAbout: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val dateFormatter = remember { SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault()) }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // GitHub Section
            Card {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Code, contentDescription = "GitHub")
                        Text(
                            text = "GitHub Sync",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Text(
                        text = "Backup your links to a private GitHub repository",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (state.isGitHubAuthenticated) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (state.isEditingGitHubRepo) {
                                    // Edit mode
                                    OutlinedTextField(
                                        value = state.tempGithubRepoName,
                                        onValueChange = { viewModel.onEvent(SettingsScreenEvent.UpdateTempGitHubRepoName(it)) },
                                        label = { Text("Repository Name") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = state.tempGithubRepoOwner,
                                        onValueChange = { viewModel.onEvent(SettingsScreenEvent.UpdateTempGitHubRepoOwner(it)) },
                                        label = { Text("Repository Owner") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { viewModel.onEvent(SettingsScreenEvent.CancelEditingGitHubRepo) },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Cancel")
                                        }
                                        Button(
                                            onClick = { viewModel.onEvent(SettingsScreenEvent.SaveGitHubRepo) },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Save")
                                        }
                                    }
                                } else {
                                    // Read-only mode
                                    OutlinedTextField(
                                        value = state.githubRepoName,
                                        onValueChange = { },
                                        label = { Text("Repository Name") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        readOnly = true,
                                        enabled = false
                                    )
                                    OutlinedTextField(
                                        value = state.githubRepoOwner,
                                        onValueChange = { },
                                        label = { Text("Repository Owner") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        readOnly = true,
                                        enabled = false
                                    )
                                    Button(
                                        onClick = { viewModel.onEvent(SettingsScreenEvent.StartEditingGitHubRepo) },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Edit Repository Details")
                                    }
                                }

                                // Sync Status
                                when (val progress = state.syncProgress) {
                                    is SyncProgress.InProgress -> {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                strokeWidth = 2.dp
                                            )
                                            Text(
                                                text = progress.message,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    is SyncProgress.Success -> {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CloudDone,
                                                contentDescription = "Sync successful",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Column {
                                                Text(
                                                    text = "Last synced: ${dateFormatter.format(Date(progress.timestamp))}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                if (progress.newLinks > 0) {
                                                    Text(
                                                        text = "${progress.newLinks} new links imported",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    is SyncProgress.Error -> {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CloudOff,
                                                contentDescription = "Sync failed",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = progress.message,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                    else -> { /* No status to show */ }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.onEvent(SettingsScreenEvent.SyncToGitHub) },
                                        enabled = !state.isGitHubSyncing,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Sync, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(if (state.isGitHubSyncing) "Syncing..." else "Sync Now")
                                    }
                                    OutlinedButton(
                                        onClick = { viewModel.onEvent(SettingsScreenEvent.DisconnectGitHub) },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Disconnect")
                                    }
                                }
                            }
                        } else {
                            Button(
                                onClick = { viewModel.onEvent(SettingsScreenEvent.ShowGitHubDeviceFlow) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Connect GitHub")
                            }
                        }
                    }
                }
            }
            // HackerNews Section
            Card {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Link, contentDescription = "HackerNews")
                        Text(
                            text = "HackerNews Integration",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Text(
                        text = "Share your links directly to HackerNews",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (state.isHackerNewsAuthenticated) {
                        OutlinedButton(
                            onClick = { viewModel.onEvent(SettingsScreenEvent.LogoutFromHackerNews) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Disconnect")
                        }
                    } else {
                        Button(
                            onClick = { viewModel.onEvent(SettingsScreenEvent.ShowHackerNewsLogin) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Connect HackerNews")
                        }
                    }
                }
            }
            // Advanced Settings Section
            Card {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Build, contentDescription = "Advanced")
                        Text(
                            text = "Advanced",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Text(
                        text = "Advanced settings and maintenance",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(
                        onClick = { viewModel.onEvent(SettingsScreenEvent.CleanupInvalidLinks) },
                        enabled = !state.isCleaningUp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (state.isCleaningUp) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cleaning up...")
                        } else {
                            Text("Cleanup Invalid Links")
                        }
                    }
                    state.cleanupResult?.let { result ->
                        Text(
                            text = "Removed $result invalid links",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // About Section
            Card {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = "About")
                        Text(
                            text = "About",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Text(
                        text = "App information and features",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(
                        onClick = onNavigateToAbout,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("View Details")
                    }
                }
            }
        }
    }

    // GitHub Device Flow Dialog
    if (state.showGitHubDeviceFlowDialog) {
        val uriHandler = LocalUriHandler.current
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(SettingsScreenEvent.HideGitHubDeviceFlow) },
            title = { Text("Connect GitHub") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (state.isInitiatingDeviceFlow || state.deviceFlowStatus.isNotEmpty()) {
                        CircularProgressIndicator()
                        Text(
                            text = state.deviceFlowStatus.ifEmpty { "Getting device code from GitHub..." },
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else if (state.githubUserCode.isEmpty()) {
                        Text(
                            text = "Click Start to begin GitHub connection",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Text(
                            text = "Enter this code on GitHub:",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = state.githubUserCode,
                            style = MaterialTheme.typography.headlineMedium,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = { uriHandler.openUri(state.githubVerificationUri) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Open GitHub")
                        }
                        if (state.isPollingForGitHubToken) {
                            CircularProgressIndicator()
                            Text(
                                text = state.deviceFlowStatus.ifEmpty { "Waiting for authorization..." },
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            },
            confirmButton = {
                if (state.githubUserCode.isEmpty()) {
                    Button(
                        onClick = { viewModel.onEvent(SettingsScreenEvent.InitiateGitHubDeviceFlow) },
                        enabled = !state.isInitiatingDeviceFlow
                    ) {
                        Text(if (state.isInitiatingDeviceFlow) "Starting..." else "Start")
                    }
                } else {
                    Button(
                        onClick = { viewModel.onEvent(SettingsScreenEvent.CancelGitHubDeviceFlow) }
                    ) {
                        Text("Cancel")
                    }
                }
            },
            dismissButton = {
                if (state.githubUserCode.isEmpty()) {
                    TextButton(
                        onClick = { viewModel.onEvent(SettingsScreenEvent.HideGitHubDeviceFlow) }
                    ) {
                        Text("Cancel")
                    }
                }
            }
        )
    }

    // HackerNews Login Dialog
    if (state.showHackerNewsLoginDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(SettingsScreenEvent.HideHackerNewsLogin) },
            title = { Text("Login to HackerNews") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = state.hackerNewsUsername,
                        onValueChange = { viewModel.onEvent(SettingsScreenEvent.UpdateHackerNewsUsername(it)) },
                        label = { Text("Username") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = state.hackerNewsPassword,
                        onValueChange = { viewModel.onEvent(SettingsScreenEvent.UpdateHackerNewsPassword(it)) },
                        label = { Text("Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.onEvent(SettingsScreenEvent.LoginToHackerNews) },
                    enabled = !state.isLoading
                ) {
                    Text(if (state.isLoading) "Logging in..." else "Login")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(SettingsScreenEvent.HideHackerNewsLogin) }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Clear GitHub profile when disconnected
    LaunchedEffect(state.isGitHubAuthenticated) {
        if (!state.isGitHubAuthenticated) {
            homeViewModel.onEvent(HomeScreenEvent.ClearGitHubProfile)
        }
    }

    // Clear HackerNews profile when logged out
    LaunchedEffect(state.isHackerNewsAuthenticated) {
        if (!state.isHackerNewsAuthenticated) {
            homeViewModel.onEvent(HomeScreenEvent.ClearHackerNewsProfile)
        }
    }

    // Show error in snackbar if present
    state.error?.let { error ->
        LaunchedEffect(error) {
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short,
                withDismissAction = true
            )
            viewModel.onEvent(SettingsScreenEvent.DismissError)
        }
    }
}
