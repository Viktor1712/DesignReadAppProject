package com.design.readerapp.ui.theme

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.design.readerapp.BooksService
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentUser = BooksService.currentUser

    var name by remember { mutableStateOf(currentUser?.name ?: "") }
    var username by remember { mutableStateOf(currentUser?.username ?: "") }
    var isSaving by remember { mutableStateOf(false) }

    // Email editing state
    var activeSection by remember { mutableStateOf<String?>(null) }
    var newEmail by remember { mutableStateOf(currentUser?.email ?: "") }
    var currentPasswordForEmail by remember { mutableStateOf("") }

    // Password editing state
    var currentPasswordForPwd by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    fun saveNameAndUsername() {
        if (currentUser?.id != null) {
            scope.launch {
                try {
                    isSaving = true
                    val updatedUser = currentUser.copy(name = name, username = username)
                    val response = BooksService.updateUser(currentUser.id, updatedUser)
                    BooksService.currentUser = response.copy(firebaseUid = currentUser.firebaseUid)
                    Toast.makeText(context, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                } finally {
                    isSaving = false
                }
            }
        }
    }

    fun updateEmail() {
        scope.launch {
            try {
                isSaving = true
                val firebaseUser = FirebaseAuth.getInstance().currentUser ?: return@launch
                val credential = EmailAuthProvider.getCredential(firebaseUser.email ?: "", currentPasswordForEmail)
                firebaseUser.reauthenticate(credential).await()
                firebaseUser.updateEmail(newEmail).await()
                if (currentUser?.id != null) {
                    val updatedUser = currentUser.copy(email = newEmail)
                    val response = BooksService.updateUser(currentUser.id, updatedUser)
                    BooksService.currentUser = response.copy(firebaseUid = currentUser.firebaseUid)
                }
                Toast.makeText(context, "Correo actualizado correctamente", Toast.LENGTH_SHORT).show()
                activeSection = null
                currentPasswordForEmail = ""
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                isSaving = false
            }
        }
    }

    fun updatePassword() {
        if (newPassword != confirmPassword) {
            Toast.makeText(context, "Las contraseñas nuevas no coinciden", Toast.LENGTH_SHORT).show()
            return
        }
        if (newPassword.length < 6) {
            Toast.makeText(context, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
            return
        }
        scope.launch {
            try {
                isSaving = true
                val firebaseUser = FirebaseAuth.getInstance().currentUser ?: return@launch
                val credential = EmailAuthProvider.getCredential(firebaseUser.email ?: "", currentPasswordForPwd)
                firebaseUser.reauthenticate(credential).await()
                firebaseUser.updatePassword(newPassword).await()
                Toast.makeText(context, "Contraseña actualizada correctamente", Toast.LENGTH_SHORT).show()
                activeSection = null
                currentPasswordForPwd = ""
                newPassword = ""
                confirmPassword = ""
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                isSaving = false
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Configuración de Perfil", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            // Header con Banner y Avatar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(vertical = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                )
                Surface(
                    modifier = Modifier
                        .size(100.dp)
                        .align(Alignment.BottomCenter),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(4.dp, MaterialTheme.colorScheme.background)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = currentUser?.initials ?: (if (name.isNotEmpty()) name.take(1).uppercase() else "U"),
                            fontSize = 40.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Text(
                text = currentUser?.email ?: "usuario@ejemplo.com",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(24.dp))

            // Nombre y Username
            Text("DETALLES DE LA CUENTA", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    ProfileInputField("Nombre completo", name) { name = it }
                    ProfileInputField("Nombre de usuario", username) { username = it }
                }
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = { saveNameAndUsername() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !isSaving,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (isSaving && activeSection == null) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("GUARDAR NOMBRE Y USUARIO", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                }
            }

            Spacer(Modifier.height(24.dp))

            // Correo electrónico
            Text("CORREO ELECTRÓNICO", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Correo actual", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(currentUser?.email ?: "", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                        }
                        IconButton(onClick = { activeSection = if (activeSection == "email") null else "email" }) {
                            Icon(Icons.Default.Edit, "Editar correo", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    if (activeSection == "email") {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                        ProfileInputField("Nuevo correo", newEmail) { newEmail = it }
                        ProfilePasswordField("Contraseña actual") { currentPasswordForEmail = it }
                        Button(
                            onClick = { updateEmail() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isSaving,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            if (isSaving) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                            else Text("Actualizar correo", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Contraseña
            Text("CONTRASEÑA", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Contraseña", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("••••••••", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                        }
                        IconButton(onClick = { activeSection = if (activeSection == "password") null else "password" }) {
                            Icon(Icons.Default.Edit, "Cambiar contraseña", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    if (activeSection == "password") {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                        ProfilePasswordField("Contraseña actual") { currentPasswordForPwd = it }
                        ProfilePasswordField("Nueva contraseña") { newPassword = it }
                        ProfilePasswordField("Confirmar nueva contraseña") { confirmPassword = it }
                        Button(
                            onClick = { updatePassword() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isSaving,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            if (isSaving) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                            else Text("Actualizar contraseña", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Preferencias
            Text("PREFERENCIAS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Notificaciones", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                        Text("Recibir alertas de nuevos libros", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }
                    Switch(checked = true, onCheckedChange = {}, colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary, checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)))
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun ProfileInputField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = MaterialTheme.colorScheme.background,
                unfocusedContainerColor = MaterialTheme.colorScheme.background,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            singleLine = true
        )
    }
}

@Composable
fun ProfilePasswordField(label: String, onValueChange: (String) -> Unit) {
    var value by remember { mutableStateOf("") }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
        OutlinedTextField(
            value = value,
            onValueChange = { value = it; onValueChange(it) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            visualTransformation = PasswordVisualTransformation(),
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = MaterialTheme.colorScheme.background,
                unfocusedContainerColor = MaterialTheme.colorScheme.background,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            singleLine = true
        )
    }
}
