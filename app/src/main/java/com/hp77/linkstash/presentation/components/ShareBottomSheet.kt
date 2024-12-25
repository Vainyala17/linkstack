package com.hp77.linkstash.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hp77.linkstash.domain.model.Link
import androidx.compose.foundation.clickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareBottomSheet(
    link: Link,
    onDismiss: () -> Unit,
    onShareToHackerNews: () -> Unit,
    onSyncToGitHub: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = "Share ${link.title ?: link.url}",
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            ListItem(
                headlineContent = { 
                    Text(
                        if (link.hackerNewsUrl != null) "View on Hacker News" 
                        else "Share to Hacker News"
                    ) 
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.Forum,
                        contentDescription = "Share to Hacker News"
                    )
                },
                modifier = Modifier.clickable(onClick = onShareToHackerNews)
            )

            ListItem(
                headlineContent = { 
                    Text(
                        if (link.lastSyncedAt != null) "Update on GitHub" 
                        else "Sync to GitHub"
                    ) 
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.Cloud,
                        contentDescription = "Sync to GitHub"
                    )
                },
                modifier = Modifier.clickable(onClick = onSyncToGitHub)
            )
        }
    }
}
