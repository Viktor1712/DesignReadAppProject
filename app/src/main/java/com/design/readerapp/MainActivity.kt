package com.design.readerapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.design.readerapp.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {
    var incomingPdf: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent?.action == Intent.ACTION_VIEW) {
            incomingPdf = intent.data
        }
        setContent {
            // rememberSaveable para que el tema persista tras rotaciones o cambios de config
            var darkTheme by rememberSaveable { mutableStateOf(false) }
            
            ReaderAppTheme(darkTheme = darkTheme) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    MyApp(incomingPdf, darkTheme) { darkTheme = !darkTheme }
                }
            }
        }
    }
}

@Composable
fun MyApp(incomingPdf: Uri?, darkTheme: Boolean, onThemeToggle: () -> Unit) {
    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    // Estado de sesión persistente
    var isSessionReady by remember { mutableStateOf(false) }
    var connectionError by remember { mutableStateOf(false) }
    var retryCount by remember { mutableStateOf(0) }

    LaunchedEffect(auth.currentUser, retryCount) {
        val user = auth.currentUser
        if (user != null) {
            try {
                connectionError = false
                // Forzamos un token fresco para asegurar que sea del nuevo proyecto
                val idTokenResult = user.getIdToken(true).await()
                val idToken = idTokenResult.token
                
                if (idToken != null) {
                    try {
                        // Intercambiamos por el JWT de Azure
                        val response = BooksService.getAuthToken(idToken)
                        BooksService.authToken = response.token
                        BooksService.currentUser = response.user.copy(firebaseUid = user.uid)
                        isSessionReady = true
                        SignalRManager.start(context, scope)
                    } catch (e: Exception) {
                        connectionError = true
                        // Si falla aquí es probablemente porque el Backend no confía en el nuevo proyecto
                        Toast.makeText(context, "Error Backend: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                connectionError = true
                Toast.makeText(context, "Error Auth: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        } else {
            isSessionReady = false
        }
    }

    // Observar notificaciones de SignalR
    LaunchedEffect(isSessionReady) {
        if (isSessionReady) {
            SignalRManager.notifications.collect { notification ->
                Toast.makeText(context, notification.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    // Pantalla de carga mientras se valida la sesión persistente
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
                    Button(onClick = { retryCount++ }) {
                        Text("Reintentar")
                    }
                    TextButton(onClick = { auth.signOut() }) {
                        Text("Cerrar Sesión")
                    }
                }
            } else {
                CircularProgressIndicator(color = AzureBlue)
            }
        }
        return
    }

    val startDestination = if (auth.currentUser != null) "home" else "login"

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("home") {
            HomeScreen(navController, darkTheme, onThemeToggle)
        }

        composable("upload") {
            UploadScreen(navController)
        }

        composable("group/{name}") {
            val name = it.arguments?.getString("name") ?: ""
            GroupScreen(navController, name, darkTheme, onThemeToggle)
        }

        composable(
            route = "reader?bookId={bookId}",
            arguments = listOf(navArgument("bookId") { 
                defaultValue = ""
                type = NavType.StringType 
            })
        ) {
            val bookId = it.arguments?.getString("bookId") ?: ""
            ReaderScreen(navController, bookId)
        }
    }
}
