package com.design.readerapp.ui.theme

import android.app.Activity
import android.widget.Toast
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.*
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {

    val context = LocalContext.current
    val activity = context as Activity

    val firebaseAuth = FirebaseAuth.getInstance()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isRegister by remember { mutableStateOf(false) }

    // 🔥 CLIENT ID AUTOMÁTICO DESDE FIREBASE
    val googleSignInClient = remember {

        val serverClientId = context.getString(
            context.resources.getIdentifier(
                "default_web_client_id",
                "string",
                context.packageName
            )
        )

        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(serverClientId)
            .requestEmail()
            .build()

        GoogleSignIn.getClient(activity, options)
    }

    // 🔥 RESULTADO GOOGLE
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->

        if (result.resultCode == Activity.RESULT_OK) {

            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

            try {
                val account = task.getResult(ApiException::class.java)

                if (account.idToken == null) {
                    Toast.makeText(context, "Token NULL (config Firebase)", Toast.LENGTH_LONG).show()
                    return@rememberLauncherForActivityResult
                }

                val credential = GoogleAuthProvider.getCredential(
                    account.idToken,
                    null
                )

                firebaseAuth.signInWithCredential(credential)
                    .addOnCompleteListener {

                        if (it.isSuccessful) {
                            Toast.makeText(context, "Google OK", Toast.LENGTH_SHORT).show()
                            onLoginSuccess()
                        } else {
                            Toast.makeText(
                                context,
                                "Error Firebase: ${it.exception?.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

            } catch (e: Exception) {
                Toast.makeText(context, "Error Google: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Login") })
        }
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // 📧 EMAIL
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 🔒 PASSWORD
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation(), // 🔒 oculta texto
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 🔐 LOGIN / REGISTER (NO TOCADO)
            Button(
                onClick = {

                    if (isRegister) {
                        firebaseAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener {

                                if (it.isSuccessful) {
                                    Toast.makeText(context, "Registro OK", Toast.LENGTH_SHORT).show()
                                    onLoginSuccess()
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Error: ${it.exception?.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }

                    } else {

                        firebaseAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener {

                                if (it.isSuccessful) {
                                    Toast.makeText(context, "Login OK", Toast.LENGTH_SHORT).show()
                                    onLoginSuccess()
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Error: ${it.exception?.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isRegister) "Registrarse" else "Iniciar sesión")
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = { isRegister = !isRegister }) {
                Text(
                    if (isRegister)
                        "¿Ya tienes cuenta? Inicia sesión"
                    else
                        "¿No tienes cuenta? Regístrate"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 🔵 GOOGLE LOGIN (ARREGLADO)
            Button(
                onClick = {
                    googleSignInClient.signOut()
                    launcher.launch(googleSignInClient.signInIntent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Iniciar con Google")
            }
        }
    }
}