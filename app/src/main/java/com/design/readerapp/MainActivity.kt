package com.design.readerapp

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.design.readerapp.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {
    var incomingPdf: Uri? = null

    private val appScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* granted or denied */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent?.action == Intent.ACTION_VIEW) {
            incomingPdf = intent.data
        }

        createNotificationChannel()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
            var darkTheme by rememberSaveable { mutableStateOf(prefs.getBoolean("dark_theme", false)) }

            LaunchedEffect(darkTheme) {
                prefs.edit().putBoolean("dark_theme", darkTheme).apply()
            }

            ReaderAppTheme(darkTheme = darkTheme) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    MyApp(incomingPdf, darkTheme) { darkTheme = !darkTheme }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Reconectar SignalR si la conexión se cayó mientras la app estaba en background
        if (BooksService.authToken != null) {
            appScope.launch {
                SignalRManager.reconnectIfNeeded(applicationContext, appScope)
            }
        }
    }

    override fun onDestroy() {
        // Detener el servicio solo si la actividad se destruye definitivamente
        // (no en rotaciones de pantalla gracias a configChanges en el manifest)
        if (isFinishing) {
            stopService(Intent(this, SignalRForegroundService::class.java))
        }
        super.onDestroy()
    }

    fun startSignalRService() {
        val intent = Intent(this, SignalRForegroundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    fun stopSignalRService() {
        stopService(Intent(this, SignalRForegroundService::class.java))
        SignalRManager.stop()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                SignalRManager.CHANNEL_ID,
                "Notificaciones",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de actividad en tus libros"
            }
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }
}

@Composable
fun MyApp(incomingPdf: Uri?, darkTheme: Boolean, onThemeToggle: () -> Unit) {
    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val activity = context as MainActivity

    var isSessionReady by remember { mutableStateOf(false) }
    var connectionError by remember { mutableStateOf(false) }
    var retryCount by remember { mutableStateOf(0) }

    LaunchedEffect(auth.currentUser, retryCount) {
        val user = auth.currentUser
        if (user != null) {
            try {
                connectionError = false
                val idTokenResult = user.getIdToken(true).await()
                val idToken = idTokenResult.token

                if (idToken != null) {
                    try {
                        val response = BooksService.getAuthToken(idToken)
                        BooksService.authToken = response.token
                        BooksService.currentUser = response.user.copy(firebaseUid = user.uid)
                        isSessionReady = true

                        // Arrancar conexión SignalR + Foreground Service
                        SignalRManager.start(context, scope)
                        activity.startSignalRService()
                    } catch (e: Exception) {
                        connectionError = true
                        Toast.makeText(context, "Error Backend: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                connectionError = true
                Toast.makeText(context, "Error Auth: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        } else {
            isSessionReady = false
            activity.stopSignalRService()
        }
    }

    LaunchedEffect(isSessionReady) {
        if (isSessionReady) {
            SignalRManager.notifications.collect { notification ->
                Toast.makeText(context, notification.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    if (auth.currentUser != null && !isSessionReady) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            if (connectionError) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Error de conexión con el servidor", color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { retryCount++ }) { Text("Reintentar") }
                    TextButton(onClick = { auth.signOut() }) { Text("Cerrar Sesión") }
                }
            } else {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
        return
    }

    val startDestination = if (auth.currentUser != null) "home" else "login"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(onLoginSuccess = {
                navController.navigate("home") { popUpTo("login") { inclusive = true } }
            })
        }
        composable("home") { HomeScreen(navController, darkTheme, onThemeToggle) }
        composable("upload") { UploadScreen(navController) }
        composable("profile") { ProfileScreen(navController) }
        composable("notifications") { NotificationsScreen(navController) }
        composable(
            route = "edit-book/{bookId}",
            arguments = listOf(navArgument("bookId") { type = NavType.StringType })
        ) {
            EditBookScreen(navController, it.arguments?.getString("bookId") ?: "")
        }
        composable("group/{name}") {
            GroupScreen(navController, it.arguments?.getString("name") ?: "", darkTheme, onThemeToggle)
        }
        composable(
            route = "reader?bookId={bookId}",
            arguments = listOf(navArgument("bookId") { defaultValue = ""; type = NavType.StringType })
        ) {
            ReaderScreen(navController, it.arguments?.getString("bookId") ?: "")
        }
    }
}
