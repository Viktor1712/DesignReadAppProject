package com.design.readerapp.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.design.readerapp.BooksService
import com.design.readerapp.Notification
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(navController: NavController) {
    val scope = rememberCoroutineScope()
    var notifications by remember { mutableStateOf(listOf<Notification>()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            isLoading = true
            notifications = BooksService.getNotifications()
        } catch (_: Exception) {
            // Error fallback
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        com.design.readerapp.SignalRManager.notifications.collect { newNotification ->
            notifications = listOf(newNotification) + notifications
        }
    }

    Scaffold(
        containerColor = AzureBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Notificaciones", fontWeight = FontWeight.Bold, color = AzureText) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = AzureText)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = AzureBackground)
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AzureBlue)
            }
        } else if (notifications.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Notifications, null, modifier = Modifier.size(64.dp), tint = AzureTextSecondary.copy(alpha = 0.5f))
                    Spacer(Modifier.height(16.dp))
                    Text("No tienes notificaciones", color = AzureTextSecondary)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notifications) { notification ->
                    NotificationItem(notification)
                }
            }
        }
    }
}

@Composable
fun NotificationItem(notification: Notification) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AzureSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, AzureBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (notification.isRead) AzureBorder else AzureBlue.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                val icon = when(notification.type.lowercase()) {
                    "like" -> "👍"
                    "comment" -> "💬"
                    "follow" -> "👤"
                    "upload" -> "📚"
                    else -> "🔔"
                }
                Text(icon, fontSize = 18.sp)
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column {
                Text(
                    text = notification.message,
                    color = AzureText,
                    fontSize = 14.sp,
                    fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Bold
                )
                Text(
                    text = notification.createdAt.take(10), // Simplificado
                    color = AzureTextSecondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}
