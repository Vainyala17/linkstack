package com.hp77.linkstash.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.OpenInNew
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
import androidx.compose.material.icons.filled.Edit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenLinkBottomSheet(
    link: Link,
    onDismiss: () -> Unit,
    onOpenInApp: () -> Unit,
    onOpenInBrowser: () -> Unit,
    onEdit: () -> Unit
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
                text = link.title ?: link.url,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            ListItem(
                headlineContent = { Text("Open in app") },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = "Open in app"
                    )
                },
                modifier = Modifier.clickable(onClick = onOpenInApp)
            )

            ListItem(
                headlineContent = { Text("Open in browser") },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.OpenInBrowser,
                        contentDescription = "Open in browser"
                    )
                },
                modifier = Modifier.clickable(onClick = onOpenInBrowser)
            )

            ListItem(
                headlineContent = { Text("Edit") },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit link"
                    )
                },
                modifier = Modifier.clickable(onClick = onEdit)
            )
        }
    }
}
