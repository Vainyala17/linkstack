package com.hp77.linkstash.presentation.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hp77.linkstash.domain.model.Link
import androidx.hilt.navigation.compose.hiltViewModel
import com.hp77.linkstash.presentation.components.FilterChips
import com.hp77.linkstash.presentation.components.SearchBar
import com.hp77.linkstash.presentation.components.TagChips
import com.hp77.linkstash.presentation.components.DefaultFilters
import com.hp77.linkstash.presentation.components.LinkItem
import com.hp77.linkstash.presentation.components.ShareBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLink: (Link) -> Unit,
    onEditLink: (Link) -> Unit,
    onToggleFavorite: (Link) -> Unit,
    onToggleArchive: (Link) -> Unit,
    onToggleStatus: (Link) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.onEvent(SearchScreenEvent.OnErrorDismiss)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SearchBar(
                query = state.searchQuery,
                onQueryChange = { viewModel.onEvent(SearchScreenEvent.OnSearchQueryChange(it)) },
                onBackClick = onNavigateBack,
                autoFocus = true
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Tags",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TagChips(
                        tags = state.tags,
                        selectedTags = state.selectedTags,
                        onTagClick = { tag ->
                            if (tag in state.selectedTags) {
                                viewModel.onEvent(SearchScreenEvent.OnTagDeselect(tag))
                            } else {
                                viewModel.onEvent(SearchScreenEvent.OnTagSelect(tag))
                            }
                        }
                    )
                }

                if (state.searchResults.isNotEmpty()) {
                    item {
                        Text(
                            text = "Results",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }

                    items(state.searchResults) { link ->
                        LinkItem(
                            link = link,
                            onLinkClick = onNavigateToLink,
                            onEditClick = onEditLink,
                            onToggleFavorite = onToggleFavorite,
                            onToggleArchive = onToggleArchive,
                            onToggleStatus = onToggleStatus,
                            onShare = { link ->
                                viewModel.onEvent(SearchScreenEvent.OnShowShareSheet(link))
                            }
                        )
                    }
                }

                if (state.isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }

                if (state.recentSearches.isNotEmpty() && state.searchQuery.isEmpty()) {
                    item {
                        Text(
                            text = "Recent Searches",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    
                    items(state.recentSearches) { search ->
                        ListItem(
                            modifier = Modifier.clickable {
                                viewModel.onEvent(SearchScreenEvent.OnRecentSearchClick(search))
                            },
                            headlineContent = { Text(search) },
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = "Recent search"
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    // Show share sheet if a link is selected for sharing
    state.selectedLink?.let { selectedLink ->
        if (state.showShareSheet) {
            ShareBottomSheet(
                link = selectedLink,
                onDismiss = { viewModel.onEvent(SearchScreenEvent.OnDismissShareSheet) },
                onShareToHackerNews = {
                    viewModel.onEvent(SearchScreenEvent.OnShareToHackerNews(selectedLink))
                },
                onSyncToGitHub = {
                    viewModel.onEvent(SearchScreenEvent.OnSyncToGitHub(selectedLink))
                }
            )
        }
    }
}
