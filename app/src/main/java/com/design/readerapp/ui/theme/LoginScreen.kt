package com.design.readerapp.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    /* 
    Lógica de inicio de sesión comentada temporalmente
    
    val context = LocalContext.current
    val activity = context as Activity
    val firebaseAuth = FirebaseAuth.getInstance()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isRegister by remember { mutableStateOf(false) }

    val googleSignInClient = remember {
        val serverClientId = context.getString(
            context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        )
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(serverClientId)
            .requestEmail()
            .build()
        GoogleSignIn.getClient(activity, options)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account.idToken != null) {
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    firebaseAuth.signInWithCredential(credential).addOnCompleteListener {
                        if (it.isSuccessful) onLoginSuccess()
                    }
                }
            } catch (e: Exception) {}
        }
    }
    */

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Bienvenido") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Módulo de inicio de sesión temporalmente deshabilitado.")
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { onLoginSuccess() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Entrar directamente")
            }
        }
    }
}