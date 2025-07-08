// MainScreen.kt
package com.example.apptracker.gui.main

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.apptracker.data.AppUsage
import com.example.apptracker.utils.isUsageStatsPermissionGranted
import com.example.apptracker.utils.requestUsageStatsPermission

@OptIn(ExperimentalMaterial3Api::class) // Opt-in for experimental Material 3 APIs
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(isUsageStatsPermissionGranted(context)) }
    val appUsageList by viewModel.uiState.collectAsStateWithLifecycle()

    // Refresh data if permission is granted
    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            viewModel.refreshData()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Todays Usage") }, // Title of the app bar
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    // Refresh button added to the top right
                    IconButton(onClick = {
                        viewModel.refreshData()
                    }, modifier = Modifier.size(48.dp)) { // Explicitly set size for better touch target
                        Icon(
                            imageVector = Icons.Filled.Refresh, // Refresh icon
                            contentDescription = "Refresh", // Content description for accessibility
                            tint = MaterialTheme.colorScheme.onPrimary // Icon color
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (hasPermission) {
            AppUsageList(
                modifier = Modifier.padding(paddingValues),
                usageList = appUsageList
            )
        } else {
            PermissionRequestScreen(
                modifier = Modifier.padding(paddingValues),
                onPermissionRequest = {
                    requestUsageStatsPermission(context)
                    // We need to re-check the permission when the user returns from settings.
                    // Doing this in onResume is more reliable, but for now, let's keep it simple.
                    // The user might have granted permission some time after pressing the button.
                }
            )
        }
    }
}

@Composable
fun AppUsageList(modifier: Modifier = Modifier, usageList: List<AppUsage>) {
    // Show loading or no data message if usage list is empty
    if (usageList.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Loading") // Loading or no data message
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator() // Loading indicator
            }
        }
    } else {
        // Display the list of app usages
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(usageList) { usage ->
                AppUsageRow(usage = usage)
                Divider() // Divider between items
            }
        }
    }
}

@Composable
fun AppUsageRow(usage: AppUsage) {
    // Display a single app usage row
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = usage.appName, fontWeight = FontWeight.Bold) // App name
            Text(text = usage.packageName, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) // Package name
        }
        Text(text = formatUsageTime(usage.usageTimeInMillis)) // Formatted usage time
    }
}

@Composable
fun PermissionRequestScreen(modifier: Modifier = Modifier, onPermissionRequest: () -> Unit) {
    // Screen displayed when usage stats permission is not granted
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "You have to grant usage data permission for app to work",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onPermissionRequest) {
                Text("Grant Permission") // Button to request permission
            }
        }
    }
}

// Helper function to format usage time from milliseconds to hours, minutes, seconds
fun formatUsageTime(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / (1000 * 60)) % 60
    val hours = (millis / (1000 * 60 * 60))
    val sb = StringBuilder()
    if (hours > 0) sb.append("$hours sa ")
    if (minutes > 0) sb.append("$minutes dk ")
    if (seconds > 0 || (hours == 0L && minutes == 0L)) sb.append("$seconds sn")
    return sb.toString().trim()
}
