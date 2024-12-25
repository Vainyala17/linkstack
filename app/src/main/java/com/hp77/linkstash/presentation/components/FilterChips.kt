package com.hp77.linkstash.presentation.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hp77.linkstash.presentation.home.LinkFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChips(
    filters: List<LinkFilter>,
    selectedFilter: LinkFilter,
    onFilterSelect: (LinkFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        filters.forEach { filter ->
            ElevatedFilterChip(
                selected = filter == selectedFilter,
                onClick = { onFilterSelect(filter) },
                label = { Text(filter.title) },
                modifier = Modifier.padding(end = 8.dp)
            )
        }
    }
}

@Composable
fun DefaultFilters() = listOf(
    LinkFilter.All,
    LinkFilter.Active,
    LinkFilter.Archived,
    LinkFilter.Favorites
)
