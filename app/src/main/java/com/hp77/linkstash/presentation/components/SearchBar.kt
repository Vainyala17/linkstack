package com.hp77.linkstash.presentation.components
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Menu
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.hp77.linkstash.domain.model.GitHubProfile

@Composable
fun LeadingIcon(onBackClick: (() -> Unit)?, onMenuClick: (() -> Unit)?) {
    when {
        onBackClick != null -> SearchIconButton(onBackClick, Icons.Default.ArrowBack, "Back")
        onMenuClick != null -> SearchIconButton(onMenuClick, Icons.Default.Menu, "Menu")
        else -> Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
    }
}

@Composable
fun SearchIconButton(onClick: () -> Unit, icon: ImageVector, contentDesc: String) {
    IconButton(onClick = onClick) {
        Icon(imageVector = icon, contentDescription = contentDesc)
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search links...",
    readOnly: Boolean = false,
    onMenuClick: (() -> Unit)? = null,
    onProfileClick: (() -> Unit)? = null,
    onBackClick: (() -> Unit)? = null,
    autoFocus: Boolean = false,
    onClick: (() -> Unit)? = null,
    githubProfile: GitHubProfile? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        if (onClick != null) {
            val interactionSource = remember { MutableInteractionSource() }
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CircleShape)
                    .clickable(
                        onClick = { onClick.invoke() },
                        interactionSource = interactionSource,
                        indication = null
                    ),
                shape = CircleShape,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(0.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LeadingIcon(onBackClick, onMenuClick)
                    Text(
                        text = if (query.isEmpty()) placeholder else query,
                        color = if (query.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search"
                            )
                        }
                    } else if (onProfileClick != null) {
                        IconButton(onClick = onProfileClick) {
                            if (githubProfile != null) {
                                AsyncImage(
                                    model = githubProfile.avatarUrl,
                                    contentDescription = "GitHub Profile",
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile"
                                )
                            }
                        }
                    }
                }
            }
        } else {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = CircleShape,
                tonalElevation = 2.dp,
            ) {
                CustomTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 8.dp),
                    onClick = { onClick?.invoke() },
                    placeholder = { Text(text = placeholder) },
                    leadingIcon = {
                        LeadingIcon(onBackClick, onMenuClick)
                    },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { onQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search"
                                )
                            }
                        } else if (onProfileClick != null) {
                            IconButton(onClick = onProfileClick) {
                                if (githubProfile != null) {
                                    AsyncImage(
                                        model = githubProfile.avatarUrl,
                                        contentDescription = "GitHub Profile",
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Profile"
                                    )
                                }
                            }
                        }
                    },
                    singleLine = true,
                    shape = CircleShape,
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    readOnly = readOnly,
                )
            }
        }
    }
}