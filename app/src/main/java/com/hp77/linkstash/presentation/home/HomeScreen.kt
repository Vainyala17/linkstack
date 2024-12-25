package com.hp77.linkstash.presentation.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color
import com.hp77.linkstash.presentation.components.NoRippleInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.hp77.linkstash.domain.model.Link
import com.hp77.linkstash.presentation.components.OpenLinkBottomSheet
import com.hp77.linkstash.util.UrlHandler
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hp77.linkstash.presentation.components.DefaultFilters
import com.hp77.linkstash.util.DateUtils
import com.hp77.linkstash.presentation.components.LinkItem
import com.hp77.linkstash.presentation.components.SearchBar
import com.hp77.linkstash.presentation.components.FilterChips

private fun handleLinkClick(
    clickedLink: Link,
    context: android.content.Context,
    onWebContent: (Link) -> Unit
) {
    when (val urlType = UrlHandler.parseUrl(clickedLink.url)) {
        is UrlHandler.UrlType.YouTube,
        is UrlHandler.UrlType.Twitter,
        is UrlHandler.UrlType.Instagram -> {
            // Launch appropriate app intent
            context.startActivity(UrlHandler.createIntent(context, clickedLink.url))
        }
        is UrlHandler.UrlType.Web -> {
            // Show bottom sheet for web content
            onWebContent(clickedLink)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAddLink: () -> Unit,
    onNavigateToEdit: (Link) -> Unit,
    onNavigateToSearch: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var selectedLink by remember { mutableStateOf<Link?>(null) }

    LaunchedEffect(state.error) {
        state.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.onEvent(HomeScreenEvent.OnErrorDismiss)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddLink) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add link"
                )
            }
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(
                        top = paddingValues.calculateTopPadding(),
                        bottom = paddingValues.calculateBottomPadding()
                    )
            ) {
                SearchBar(
                    query = state.searchQuery,
                    onQueryChange = { viewModel.onEvent(HomeScreenEvent.OnSearchQueryChange(it)) },
                    readOnly = true,
                    onMenuClick = { viewModel.onEvent(HomeScreenEvent.OnMenuClick) },
                    onProfileClick = { viewModel.onEvent(HomeScreenEvent.OnProfileClick) },
                    onClick = onNavigateToSearch,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                FilterChips(
                    filters = DefaultFilters(),
                    selectedFilter = state.selectedFilter,
                    onFilterSelect = { filter ->
                        viewModel.onEvent(HomeScreenEvent.OnFilterSelect(filter))
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (state.links.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No links found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    val groupedLinks = state.links.groupBy { link ->
                        DateUtils.formatDate(link.createdAt)
                    }.toSortedMap { a, b ->
                        // Custom comparator to ensure "Today" and "Yesterday" appear first
                        when {
                            a == "Today" -> -1
                            b == "Today" -> 1
                            a == "Yesterday" -> -1
                            b == "Yesterday" -> 1
                            else -> b.compareTo(a) // For other dates, maintain reverse chronological order
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        groupedLinks.forEach { (date, linksForDate) ->
                            item(key = date) {
                                Text(
                                    text = date,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                            
                            items(
                                items = linksForDate,
                                key = { it.id }
                            ) { link ->
                                LinkItem(
                                    link = link,
                                    onLinkClick = { handleLinkClick(it, context) { link -> selectedLink = link } },
                                    onEditClick = { onNavigateToEdit(it) },
                                    onToggleFavorite = {
                                        viewModel.onEvent(HomeScreenEvent.OnToggleFavorite(it))
                                    },
                                    onToggleArchive = {
                                        viewModel.onEvent(HomeScreenEvent.OnToggleArchive(it))
                                    },
                                    onToggleStatus = {
                                        viewModel.onEvent(HomeScreenEvent.OnToggleStatus(it))
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Show menu dialog
    if (state.showMenu) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(HomeScreenEvent.OnMenuDismiss) },
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            title = { Text("Menu") },
            text = { Text("Menu options coming soon!") },
            confirmButton = {
                TextButton(onClick = { viewModel.onEvent(HomeScreenEvent.OnMenuDismiss) }) {
                    Text("Close")
                }
            }
        )
    }

    // Show profile dialog
    if (state.showProfile) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(HomeScreenEvent.OnProfileDismiss) },
            icon = { Icon(Icons.Default.Person, contentDescription = null) },
            title = { Text("Profile") },
            text = { Text("Profile options coming soon!") },
            confirmButton = {
                TextButton(onClick = { viewModel.onEvent(HomeScreenEvent.OnProfileDismiss) }) {
                    Text("Close")
                }
            }
        )
    }

    // Show bottom sheet if a link is selected
    selectedLink?.let { link ->
        OpenLinkBottomSheet(
            link = link,
            onDismiss = { selectedLink = null },
            onOpenInApp = {
                selectedLink = null
                context.startActivity(UrlHandler.createIntent(context, link.url))
            },
            onOpenInBrowser = {
                selectedLink = null
                context.startActivity(UrlHandler.createBrowserIntent(link.url))
            },
            onEdit = {
                selectedLink = null
                onNavigateToEdit(link)
            }
        )
    }
}
