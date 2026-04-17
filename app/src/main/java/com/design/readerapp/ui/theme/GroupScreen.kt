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
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.design.readerapp.Book
import com.design.readerapp.BooksService
import kotlinx.coroutines.launch
import android.content.Context
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupScreen(
    navController: NavController, 
    groupName: String, 
    darkTheme: Boolean, 
    onThemeToggle: () -> Unit
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var books by remember { mutableStateOf(listOf<Book>()) }
    var selectedBook by remember { mutableStateOf<Book?>(null) }
    var showMenu by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    fun loadBooks() {
        scope.launch {
            try {
                isLoading = true
                // Filtramos por categoría (groupName) si la API lo permite, 
                // o filtramos localmente si la API devuelve todos.
                val allBooks = BooksService.getBooks()
                books = allBooks.filter { it.categoria == groupName || groupName == "Todos" }
            } catch (e: Exception) {
                Toast.makeText(context, "Error al cargar libros", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(groupName) {
        loadBooks()
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    var fileName = "libro.pdf"
                    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1 && cursor.moveToFirst()) fileName = cursor.getString(nameIndex)
                    }

                    val newBook = Book(
                        titulo = fileName,
                        autor = "Desconocido",
                        categoria = groupName,
                        pdfUrl = uri.toString(),
                        estado = "Publicado"
                    )
                    
                    BooksService.createBook(newBook)
                    loadBooks()
                    Toast.makeText(context, "Libro agregado", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
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

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (books.isEmpty()) {
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
                val uriString = book.pdfUrl
                val uri = Uri.parse(uriString)
                val bitmap = remember(book) { PdfUtils.generateThumbnail(context, uri) }
                
                val prefs = context.getSharedPreferences("reader_prefs", Context.MODE_PRIVATE)
                val savedPage = prefs.getInt("page_${uid}_$uriString", 0)
                val totalPages = prefs.getInt("total_${uid}_$uriString", 0)
                val progress = if (totalPages > 0) (savedPage + 1).toFloat() / totalPages else 0f

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = {
                                try { 
                                    context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) 
                                } catch (_: Exception) {}
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
                                text = book.titulo,
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
                    headlineContent = { Text("Eliminar libro", color = MaterialTheme.colorScheme.error) },
                    leadingContent = { Text("🗑️", fontSize = 20.sp) },
                    modifier = Modifier.clickable {
                        scope.launch {
                            try {
                                selectedBook?.id?.let { BooksService.deleteBook(it) }
                                loadBooks()
                                showMenu = false
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error al eliminar", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
            }
        }
    }
}