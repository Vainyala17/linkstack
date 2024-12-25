package com.hp77.linkstash.presentation.home

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hp77.linkstash.util.DateUtils
import com.hp77.linkstash.presentation.components.DefaultFilters
import com.hp77.linkstash.presentation.components.FilterChips
import com.hp77.linkstash.presentation.components.LinkItem
import com.hp77.linkstash.presentation.components.SearchBar
import com.hp77.linkstash.presentation.components.TagChips

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAddLink: () -> Unit,
    onNavigateToLink: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

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
                    .padding(paddingValues)
            ) {
                SearchBar(
                    query = state.searchQuery,
                    onQueryChange = { viewModel.onEvent(HomeScreenEvent.OnSearchQueryChange(it)) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                FilterChips(
                    filters = DefaultFilters(),
                    selectedFilter = state.selectedFilter,
                    onFilterSelect = { viewModel.onEvent(HomeScreenEvent.OnFilterSelect(it)) }
                )

                if (state.tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TagChips(
                        tags = state.tags,
                        selectedTags = state.selectedTags,
                        onTagClick = { tag ->
                            if (tag in state.selectedTags) {
                                viewModel.onEvent(HomeScreenEvent.OnTagDeselect(tag))
                            } else {
                                viewModel.onEvent(HomeScreenEvent.OnTagSelect(tag))
                            }
                        }
                    )
                }

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
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                    onLinkClick = { onNavigateToLink(it.id) },
                                    onToggleFavorite = {
                                        viewModel.onEvent(HomeScreenEvent.OnToggleFavorite(it))
                                    },
                                    onToggleArchive = {
                                        viewModel.onEvent(HomeScreenEvent.OnToggleArchive(it))
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
