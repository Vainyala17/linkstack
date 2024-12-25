package com.hp77.linkstash.presentation.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import com.hp77.linkstash.ui.theme.LinkColor
import com.hp77.linkstash.ui.theme.LinkColorDark
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hp77.linkstash.domain.model.Link
import com.hp77.linkstash.domain.model.Tag
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
private fun LinkActions(
    link: Link,
    onToggleFavorite: (Link) -> Unit,
    onEditClick: (Link) -> Unit,
    onToggleArchive: (Link) -> Unit,
    onShare: (Link) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.requiredWidth(200.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { onToggleFavorite(link) }) {
            Icon(
                imageVector = if (link.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = if (link.isFavorite) "Remove from favorites" else "Add to favorites",
                tint = if (link.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
        IconButton(onClick = { onEditClick(link) }) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit link"
            )
        }
        IconButton(onClick = { onToggleArchive(link) }) {
            Icon(
                imageVector = if (link.isArchived) Icons.Default.Unarchive else Icons.Default.Archive,
                contentDescription = if (link.isArchived) "Unarchive" else "Archive"
            )
        }
        IconButton(onClick = { onShare(link) }) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Share options"
            )
        }
        if (link.lastSyncedAt != null) {
            Icon(
                imageVector = if (link.syncError != null) 
                    Icons.Default.CloudOff 
                else 
                    Icons.Default.CloudDone,
                contentDescription = if (link.syncError != null) 
                    "Sync error: ${link.syncError}" 
                else 
                    "Synced to GitHub",
                tint = if (link.syncError != null)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LinkTags(
    tags: List<Tag>,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        userScrollEnabled = false
    ) {
        items(tags) { tag ->
            ElevatedAssistChip(
                onClick = { },
                label = { 
                    Text(
                        text = tag.name,
                        color = MaterialTheme.colorScheme.onSurface
                    ) 
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkItem(
    link: Link,
    onLinkClick: (Link) -> Unit,
    onEditClick: (Link) -> Unit,
    onToggleFavorite: (Link) -> Unit,
    onToggleArchive: (Link) -> Unit,
    onToggleStatus: (Link) -> Unit,
    onShare: (Link) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }
    val isFavorite = remember(link.isFavorite) { link.isFavorite }
    val isArchived = remember(link.isArchived) { link.isArchived }
    val isCompleted = remember(link.isCompleted) { link.isCompleted }
    val tags = remember(link.tags) { link.tags }

    Card(
        onClick = { onLinkClick(link) },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = link.title ?: link.url,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = if (isSystemInDarkTheme()) LinkColorDark else LinkColor
                    )
                    if (link.title != null) {
                        Text(
                            text = link.url,
                            style = MaterialTheme.typography.bodySmall.copy(
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                            ),
                            color = if (isSystemInDarkTheme()) 
                                LinkColorDark.copy(alpha = 0.9f) 
                            else 
                                LinkColor.copy(alpha = 0.95f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    Text(
                        text = dateFormatter.format(Date(link.createdAt)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            link.description?.let { description ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = link.type.getStatusLabel(link.isCompleted),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (link.isCompleted) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Updated: ${link.completedAt?.let { dateFormatter.format(Date(it)) } ?: ""}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                alpha = if (link.completedAt != null) 1f else 0f
                            )
                        )
                    }
                }
                androidx.compose.material3.Switch(
                    checked = link.isCompleted,
                    onCheckedChange = { onToggleStatus(link) }
                )
            }

            if (tags.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 16.dp)
                    ) {
                        LinkTags(
                            tags = tags,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    LinkActions(
                        link = link.copy(
                            isFavorite = isFavorite,
                            isArchived = isArchived
                        ),
                        onToggleFavorite = onToggleFavorite,
                        onEditClick = onEditClick,
                        onToggleArchive = onToggleArchive,
                        onShare = onShare
                    )
                }
            }
        }
    }
}
