package com.design.readerapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.design.readerapp.ui.theme.*
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {

    var incomingPdf: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent?.action == Intent.ACTION_VIEW) {
            incomingPdf = intent.data
        }

        setContent {
            var darkTheme by remember { mutableStateOf(false) }
            
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

    var startDestination by remember {
        mutableStateOf(if (auth.currentUser != null) "home" else "login")
    }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener {
            startDestination = if (it.currentUser != null) "home" else "login"
        }

        auth.addAuthStateListener(listener)

        onDispose {
            auth.removeAuthStateListener(listener)
        }
    }

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

        composable("group/{name}") {
            val name = it.arguments?.getString("name") ?: ""
            GroupScreen(navController, name, darkTheme, onThemeToggle)
        }

        composable(
            route = "reader?bookId={bookId}",
            arguments = listOf(navArgument("bookId") { 
                defaultValue = -1
                type = NavType.IntType 
            })
        ) {
            val bookId = it.arguments?.getInt("bookId") ?: -1
            ReaderScreen(bookId)
        }
    }
}
