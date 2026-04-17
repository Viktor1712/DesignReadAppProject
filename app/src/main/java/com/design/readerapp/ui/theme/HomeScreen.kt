package com.design.readerapp.ui.theme

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

const val FAVORITOS = "Favoritos"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, darkTheme: Boolean, onThemeToggle: () -> Unit) {

    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val uid = auth.currentUser?.uid ?: return

    var groups by remember { mutableStateOf(listOf<String>()) }
    var showDialog by remember { mutableStateOf(false) }
    var newGroup by remember { mutableStateOf("") }

    var editing by remember { mutableStateOf<String?>(null) }
    var editName by remember { mutableStateOf("") }

    LaunchedEffect(uid) {
        db.collection("users")
            .document(uid)
            .collection("groups")
            .addSnapshotListener { snap, _ ->
                val list = snap?.documents?.map { it.id }?.toMutableList() ?: mutableListOf()
                if (!list.contains(FAVORITOS)) list.add(FAVORITOS)
                groups = list.sorted()
            }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Mis Libros", 
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ) 
                },
                actions = {
                    IconButton(onClick = onThemeToggle) {
                        Text(if (darkTheme) "☀️" else "🌙", fontSize = 20.sp)
                    }
                    IconButton(onClick = { auth.signOut() }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Salir")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            LargeFloatingActionButton(
                onClick = {
                    newGroup = ""
                    showDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("+", fontSize = 32.sp)
            }
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(groups) { group ->
                Card(
                    onClick = { navController.navigate("group/$group") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = group,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (group == FAVORITOS) "Tus preferidoss" else "Colección de PDFs",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (group != FAVORITOS) {
                                IconButton(onClick = {
                                    editing = group
                                    editName = group
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar", modifier = Modifier.scale(0.8f))
                                }
                                IconButton(onClick = {
                                    db.collection("users").document(uid).collection("groups").document(group).delete()
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", modifier = Modifier.scale(0.8f))
                                }
                            }
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                        }
                    }
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Nueva Categoría") },
                text = {
                    OutlinedTextField(
                        value = newGroup,
                        onValueChange = { newGroup = it },
                        placeholder = { Text("Ej: Universidad, Trabajo...") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val name = newGroup.trim()
                            if (name.isNotEmpty()) {
                                db.collection("users").document(uid).collection("groups").document(name).set(mapOf("name" to name))
                            }
                            showDialog = false
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) { Text("Crear") }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) { Text("Cancelar") }
                }
            )
        }

        if (editing != null) {
            AlertDialog(
                onDismissRequest = { editing = null },
                title = { Text("Renombrar") },
                text = {
                    OutlinedTextField(
                        value = editName, 
                        onValueChange = { editName = it },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        val old = editing ?: return@Button
                        val new = editName.trim()
                        if (new.isNotEmpty() && new != old) {
                            db.collection("users").document(uid).collection("groups").document(old).delete()
                            db.collection("users").document(uid).collection("groups").document(new).set(mapOf("name" to new))
                        }
                        editing = null
                    }) { Text("Guardar") }
                },
                dismissButton = {
                    TextButton(onClick = { editing = null }) { Text("Cancelar") }
                }
            )
        }
    }
}