package com.hp77.linkstash.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.hp77.linkstash.presentation.components.NoRippleInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import com.hp77.linkstash.data.preferences.ThemeMode
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.res.painterResource
import com.hp77.linkstash.R
import androidx.compose.material3.*
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
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
import com.hp77.linkstash.presentation.components.ShareBottomSheet
import com.hp77.linkstash.util.Logger
import com.hp77.linkstash.util.UIAnomalyDetector
import com.hp77.linkstash.util.SystemLogMonitor

private fun handleLinkClick(
    clickedLink: Link,
    context: android.content.Context,
    onWebContent: (Link) -> Unit
) {
    when (val urlType = UrlHandler.parseUrl(clickedLink.url)) {
        is UrlHandler.UrlType.YouTube -> {
            // Try to open in YouTube app, fallback to WebView
            try {
                context.startActivity(UrlHandler.createIntent(context, clickedLink.url))
            } catch (e: Exception) {
                onWebContent(clickedLink)
            }
        }
        is UrlHandler.UrlType.Twitter,
        is UrlHandler.UrlType.Instagram -> {
            // Try to open in respective app, fallback to WebView
            try {
                context.startActivity(UrlHandler.createIntent(context, clickedLink.url))
            } catch (e: Exception) {
                onWebContent(clickedLink)
            }
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
    onNavigateToSettings: () -> Unit,
    onNavigateToWebView: (Link) -> Unit,
    onNavigateToAbout: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var selectedLink by remember { mutableStateOf<Link?>(null) }
    var showThemeDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.error) {
        state.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.onEvent(HomeScreenEvent.OnErrorDismiss)
        }
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Track drawer state
    var drawerOpenTime by remember { mutableStateOf(0L) }
    var drawerContentVisible by remember { mutableStateOf(false) }
    var drawerRenderAttempts by remember { mutableStateOf(0) }
    var hasGraphicsError by remember { mutableStateOf(false) }

    // Handle drawer state
    LaunchedEffect(state.isDrawerOpen) {
        try {
            if (state.isDrawerOpen) {
                Logger.d("HomeScreen", "Opening drawer")
                drawerOpenTime = System.currentTimeMillis()
                drawerContentVisible = false
                drawerRenderAttempts = 0
                hasGraphicsError = false
                drawerState.open()
            } else {
                Logger.d("HomeScreen", "Closing drawer")
                drawerState.close()
                drawerContentVisible = false
                drawerRenderAttempts = 0
                hasGraphicsError = false
            }
        } catch (e: Exception) {
            // Only log real errors, ignore interruptions
            if (!e.message?.contains("Mutation interrupted", ignoreCase = true)!!) {
                Logger.e("HomeScreen", "Drawer operation failed", e)
            }
        }
    }

    // Monitor drawer state and content
    LaunchedEffect(drawerState.currentValue) {
        when (drawerState.currentValue) {
            DrawerValue.Open -> {
                Logger.d("HomeScreen", "Drawer opened, waiting for content")
                viewModel.onEvent(HomeScreenEvent.OnDrawerOpen)
                
                // Give drawer time to render
                kotlinx.coroutines.delay(500)
                
                // If content isn't visible, start checking for issues
                if (!drawerContentVisible) {
                    drawerRenderAttempts++
                    
                    // Only check for graphics errors after multiple render attempts
                    if (drawerRenderAttempts >= 2 && !hasGraphicsError) {
                        // Check for actual graphics errors
                        if (SystemLogMonitor.checkForGraphicsErrors()) {
                            Logger.e("HomeScreen", "Graphics pipeline error detected after $drawerRenderAttempts attempts")
                            hasGraphicsError = true
                            viewModel.onEvent(HomeScreenEvent.ShowIssueReport)
                            viewModel.onEvent(HomeScreenEvent.UpdateIssueDescription(
                                """
                                Navigation drawer shows black screen
                                Time since attempt: ${System.currentTimeMillis() - drawerOpenTime}ms
                                Render attempts: $drawerRenderAttempts
                                
                                System Info:
                                Device: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}
                                Android: ${android.os.Build.VERSION.RELEASE}
                                
                                System logs show graphics pipeline issues:
                                - PQ Session initialization failed
                                - AAL Engine reconfiguration errors
                                
                                This appears to be a system-level graphics issue.
                                Please try:
                                1. Closing and reopening the drawer
                                2. If issue persists, restart the app
                                3. If still occurring, restart your device
                                """.trimIndent()
                            ))
                        } else {
                            Logger.d("HomeScreen", "No graphics errors detected after $drawerRenderAttempts attempts")
                        }
                    }
                }
            }
            DrawerValue.Closed -> {
                Logger.d("HomeScreen", "Drawer closed")
                viewModel.onEvent(HomeScreenEvent.OnDrawerClose)
                drawerContentVisible = false
                drawerRenderAttempts = 0
                hasGraphicsError = false
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp)
            ) {
                // Mark content as visible when drawer sheet is rendered
                drawerContentVisible = true
                Logger.d("HomeScreen", "Drawer content rendered")
                Spacer(Modifier.height(16.dp))

                // App Logo and Name
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_drawer_logo),
                            contentDescription = "LinkStack Logo",
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Text(
                        text = "LinkStack",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

                // Theme
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.DarkMode, contentDescription = null) },
                    label = { Text("Theme") },
                    badge = { 
                        Text(
                            text = when(state.currentTheme) {
                                ThemeMode.LIGHT -> "Light"
                                ThemeMode.DARK -> "Dark"
                                ThemeMode.SYSTEM -> "System"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    selected = false,
                    onClick = { showThemeDialog = true },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                // Report Issue
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.BugReport, contentDescription = null) },
                    label = { Text("Report an Issue") },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            viewModel.onEvent(HomeScreenEvent.ShowIssueReport)
                        }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                // Settings Section
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    // Issue Report Dialog
                    label = { Text("Settings") },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            onNavigateToSettings()
                        }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                // About Section
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Info, contentDescription = null) },
                    label = { Text("About") },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            onNavigateToAbout()
                        }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
    ) {
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
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onProfileClick = { viewModel.onEvent(HomeScreenEvent.OnProfileClick) },
                        onClick = onNavigateToSearch,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        githubProfile = state.githubProfile
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
                                        onLinkClick = {
                                            handleLinkClick(
                                                it,
                                                context
                                            ) { link -> selectedLink = link }
                                        },
                                        onEditClick = { link ->
                                            Logger.d(
                                                "HomeScreen",
                                                "Navigating to edit for link: id=${link.id}, url=${link.url}"
                                            )
                                            onNavigateToEdit(link)
                                        },
                                        onToggleFavorite = {
                                            viewModel.onEvent(HomeScreenEvent.OnToggleFavorite(it))
                                        },
                                        onToggleArchive = {
                                            viewModel.onEvent(HomeScreenEvent.OnToggleArchive(it))
                                        },
                                        onToggleStatus = {
                                            viewModel.onEvent(HomeScreenEvent.OnToggleStatus(it))
                                        },
                                        onShare = { link ->
                                            viewModel.onEvent(HomeScreenEvent.OnShowShareSheet(link))
                                        },
                                        onDelete = { link ->
                                            viewModel.onEvent(HomeScreenEvent.OnDeleteLink(link))
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }


        // Show theme selection dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            icon = { Icon(Icons.Default.DarkMode, contentDescription = null) },
            title = { Text("Choose Theme") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeMode.values().forEach { theme ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    viewModel.onEvent(HomeScreenEvent.OnThemeSelect(theme))
                                    showThemeDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = state.currentTheme == theme,
                                onClick = { 
                                    viewModel.onEvent(HomeScreenEvent.OnThemeSelect(theme))
                                    showThemeDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when(theme) {
                                    ThemeMode.LIGHT -> "Light"
                                    ThemeMode.DARK -> "Dark"
                                    ThemeMode.SYSTEM -> "System default"
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
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
                title = { Text("Connected Accounts") },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        state.githubProfile?.let { profile ->
                            // GitHub Profile Section
                            Column {
                                Text(
                                    text = "GitHub",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = profile.avatarUrl,
                                        contentDescription = "GitHub Avatar",
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = profile.name ?: profile.login,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        profile.bio?.let { bio ->
                                            Text(
                                                text = bio,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text("${profile.publicRepos} repositories")
                                    Text("${profile.followers} followers")
                                    Text("${profile.following} following")
                                }
                            }
                        }

                        state.hackerNewsProfile?.let { profile ->
                            // HackerNews Profile Section
                            Column {
                                Text(
                                    text = "HackerNews",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = profile.username,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                profile.about?.let { about ->
                                    Text(
                                        text = about,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("${profile.karma} karma")
                            }
                        }

                        if (state.githubProfile == null && state.hackerNewsProfile == null) {
                            Text(
                                text = "No accounts connected. You can connect to GitHub and HackerNews in Settings.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
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
                    onNavigateToWebView(link)
                },
                onOpenInBrowser = {
                    selectedLink = null
                    context.startActivity(UrlHandler.createBrowserIntent(link.url))
                },
                onEdit = {
                    selectedLink = null
                    onNavigateToEdit(link)
                },
                onOpenHackerNews = link.hackerNewsUrl?.let { hnUrl ->
                    {
                        selectedLink = null
                        context.startActivity(UrlHandler.createBrowserIntent(hnUrl))
                    }
                }
            )
        }

        // Show share sheet if a link is selected for sharing
        state.selectedLink?.let { selectedLink ->
            if (state.showShareSheet) {
                ShareBottomSheet(
                    link = selectedLink,
                    onDismiss = { viewModel.onEvent(HomeScreenEvent.OnDismissShareSheet) },
                    onShareToHackerNews = {
                        viewModel.onEvent(HomeScreenEvent.OnShareToHackerNews(selectedLink))
                    },
                    onSyncToGitHub = {
                        viewModel.onEvent(HomeScreenEvent.OnSyncToGitHub(selectedLink))
                    }
                )
            }
        }

        // Issue Report Dialog
        if (state.showIssueReportDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.onEvent(HomeScreenEvent.HideIssueReport) },
                icon = { Icon(Icons.Default.BugReport, contentDescription = null) },
                title = { Text("Report an Issue") },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Please describe the issue you're experiencing:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        OutlinedTextField(
                            value = state.issueDescription,
                            onValueChange = { viewModel.onEvent(HomeScreenEvent.UpdateIssueDescription(it)) },
                            label = { Text("Description") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            maxLines = 5
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { 
                            viewModel.onEvent(HomeScreenEvent.ReportIssue(state.issueDescription))
                        },
                        enabled = state.issueDescription.isNotBlank()
                    ) {
                        Text("Send Report")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { viewModel.onEvent(HomeScreenEvent.HideIssueReport) }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
