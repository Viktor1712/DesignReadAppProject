package com.design.readerapp.ui.theme

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.design.readerapp.ReaderState
import com.design.readerapp.PdfUtils
import java.io.File
import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupScreen(
    navController: NavController, 
    groupName: String, 
    darkTheme: Boolean, 
    onThemeToggle: () -> Unit
) {

    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var books by remember { mutableStateOf(listOf<Map<String, String>>()) }
    var selectedBook by remember { mutableStateOf<Map<String, String>?>(null) }
    var showMenu by remember { mutableStateOf(false) }
    var showMoveDialog by remember { mutableStateOf(false) }
    var allGroups by remember { mutableStateOf(listOf<String>()) }

    LaunchedEffect(uid) {
        db.collection("users").document(uid).collection("groups")
            .addSnapshotListener { snap, _ ->
                val list = snap?.documents?.map { it.id }?.toMutableList() ?: mutableListOf()
                if (!list.contains("Favoritos")) list.add("Favoritos")
                allGroups = list.sorted()
            }
    }

    LaunchedEffect(groupName) {
        db.collection("users").document(uid).collection("groups").document(groupName)
            .collection("books")
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) return@addSnapshotListener
                books = snapshot?.documents?.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    val map = data.mapValues { it.value.toString() }.toMutableMap()
                    map["id"] = doc.id
                    map
                } ?: emptyList()
            }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                val inputStream = context.contentResolver.openInputStream(uri) ?: throw Exception("Error")
                
                var fileName = "libro.pdf"
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1 && cursor.moveToFirst()) fileName = cursor.getString(nameIndex)
                }

                val tempFile = File(context.cacheDir, fileName)
                tempFile.outputStream().use { inputStream.copyTo(it) }

                val bookId = uri.toString().hashCode().toString()
                val fileUri = Uri.fromFile(tempFile).toString()

                db.collection("users").document(uid).collection("groups").document(groupName)
                    .collection("books").document(bookId)
                    .set(mapOf("uri" to fileUri, "name" to fileName))

            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(groupName, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Text("⬅️", fontSize = 20.sp)
                    }
                },
                actions = {
                    IconButton(onClick = onThemeToggle) {
                        Text(if (darkTheme) "☀️" else "🌙", fontSize = 20.sp)
                    }
                }
            )
        },
        floatingActionButton = {
            LargeFloatingActionButton(
                onClick = { launcher.launch(arrayOf("application/pdf")) },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(24.dp)
            ) { Text("+", fontSize = 32.sp) }
        }
    ) { padding ->

        if (books.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay libros en esta categoría", color = MaterialTheme.colorScheme.outline)
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(books) { book ->
                val uriString = book["uri"] ?: ""
                val uri = Uri.parse(uriString)
                val bitmap = remember(book) { PdfUtils.generateThumbnail(context, uri) }
                
                // Cargar progreso desde SharedPreferences
                val prefs = context.getSharedPreferences("reader_prefs", Context.MODE_PRIVATE)
                val savedPage = prefs.getInt("page_$uriString", 0)
                // Para el total necesitamos que se haya abierto al menos una vez
                // Por ahora lo guardaremos en ReaderScreen cuando se cargue el PDF
                // Si no hay total guardado, mostramos 0
                val totalPages = prefs.getInt("total_$uriString", 0)
                val progress = if (totalPages > 0) (savedPage + 1).toFloat() / totalPages else 0f

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = {
                                try { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) {}
                                ReaderState.currentPdf = uri
                                navController.navigate("reader")
                            },
                            onLongClick = {
                                selectedBook = book
                                showMenu = true
                            }
                        ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("PDF", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)
                                }
                            }
                            
                            // Barra de progreso sobre la imagen en la parte inferior
                            if (progress > 0) {
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier.fillMaxWidth().height(4.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = Color.Transparent,
                                    strokeCap = StrokeCap.Butt
                                )
                            }
                        }
                        
                        Row(
                            Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = book["name"] ?: "Libro",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            
                            if (progress > 0) {
                                Text(
                                    text = "${(progress * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showMenu && selectedBook != null) {
        ModalBottomSheet(onDismissRequest = { showMenu = false }) {
            Column(Modifier.padding(bottom = 32.dp)) {
                ListItem(
                    headlineContent = { Text("Añadir a Favoritos") },
                    leadingContent = { Text("⭐", fontSize = 20.sp) },
                    modifier = Modifier.clickable {
                        val id = selectedBook!!["id"] ?: ""
                        val data = selectedBook!!.filterKeys { it != "id" }
                        db.collection("users").document(uid).collection("groups").document("Favoritos").collection("books").document(id).set(data)
                        showMenu = false
                    }
                )
                ListItem(
                    headlineContent = { Text("Mover a otra categoría") },
                    leadingContent = { Text("📦", fontSize = 20.sp) },
                    modifier = Modifier.clickable {
                        showMenu = false
                        showMoveDialog = true
                    }
                )
                ListItem(
                    headlineContent = { Text("Eliminar libro", color = MaterialTheme.colorScheme.error) },
                    leadingContent = { Text("🗑️", fontSize = 20.sp) },
                    modifier = Modifier.clickable {
                        db.collection("users").document(uid).collection("groups").document(groupName).collection("books").document(selectedBook!!["id"] ?: "").delete()
                        showMenu = false
                    }
                )
            }
        }
    }

    if (showMoveDialog && selectedBook != null) {
        AlertDialog(
            onDismissRequest = { showMoveDialog = false },
            title = { Text("Mover a...") },
            text = {
                LazyColumn {
                    items(allGroups) { group ->
                        if (group != groupName) {
                            ListItem(
                                headlineContent = { Text(group) },
                                modifier = Modifier.clickable {
                                    val id = selectedBook?.get("id") ?: ""
                                    val bookData = selectedBook?.filterKeys { it != "id" } ?: emptyMap()
                                    val batch = db.batch()
                                    batch.set(db.collection("users").document(uid).collection("groups").document(group).collection("books").document(id), bookData)
                                    batch.delete(db.collection("users").document(uid).collection("groups").document(groupName).collection("books").document(id))
                                    batch.commit()
                                    showMoveDialog = false
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showMoveDialog = false }) { Text("Cancelar") } }
        )
    }
}