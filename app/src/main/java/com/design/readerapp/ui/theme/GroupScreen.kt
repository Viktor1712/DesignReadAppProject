package com.design.readerapp.ui.theme

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.design.readerapp.Book
import com.design.readerapp.BooksService
import com.design.readerapp.Favorite
import com.design.readerapp.ReadingProgress
import com.design.readerapp.ReaderState
import kotlinx.coroutines.launch
import androidx.navigation.NavController as NavControllerAlias

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupScreen(
    navController: NavControllerAlias, 
    groupName: String, 
    darkTheme: Boolean, 
    onThemeToggle: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userId = BooksService.currentUser?.id ?: "" 
    val firebaseUid = BooksService.currentUser?.firebaseUid ?: ""

    var books by remember { mutableStateOf(listOf<Book>()) }
    var favorites by remember { mutableStateOf(listOf<Favorite>()) }
    var readingProgress by remember { mutableStateOf(listOf<ReadingProgress>()) }
    
    var selectedBook by remember { mutableStateOf<Book?>(null) }
    var showMenu by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredBooks = remember(books, searchQuery) {
        if (searchQuery.isBlank()) books
        else books.filter { 
            it.title.contains(searchQuery, ignoreCase = true) || 
            it.author.contains(searchQuery, ignoreCase = true) ||
            it.category.contains(searchQuery, ignoreCase = true)
        }
    }

    fun loadData() {
        scope.launch {
            try {
                isLoading = true
                val allBooks = BooksService.getBooks()
                // Intentamos obtener favoritos y progreso, si falla uno no bloqueamos el resto
                val allFavorites = try { BooksService.getFavorites() } catch (e: Exception) { emptyList() }
                val allProgress = try { BooksService.getReadingProgress() } catch (e: Exception) { emptyList() }
                
                favorites = allFavorites
                readingProgress = allProgress

                books = when (groupName.lowercase()) {
                    "favoritos" -> {
                        // Filtramos favoritos que pertenezcan al usuario actual (por userId o firebaseUid)
                        val userFavIds = favorites
                            .filter { it.userId == userId || it.userId == firebaseUid }
                            .map { it.bookId }
                        allBooks.filter { it.id in userFavIds }
                    }
                    "mis-libros" -> {
                        // Verificamos tanto el ID interno como el Firebase UID para mayor robustez
                        allBooks.filter { it.userId == userId || it.userId == firebaseUid }
                    }
                    "explorar" -> {
                        allBooks
                    }
                    else -> {
                        allBooks.filter { it.category.equals(groupName, ignoreCase = true) }
                    }
                }

                // Cargar SAS URLs para portadas de forma asíncrona para no bloquear
                books.forEach { book ->
                    book.id?.let { id ->
                        launch {
                            try {
                                val sas = BooksService.getBookCoverUrl(id, userId.ifBlank { firebaseUid })
                                books = books.map {
                                    if (it.id == id) it.copy(coverUrl = sas.url) else it
                                }
                            } catch (_: Exception) { }
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error al cargar libros: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(groupName) { loadData() }

    Scaffold(
        containerColor = AzureBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = groupName.replace("-", " ").uppercase(), 
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = AzureText
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = AzureText)
                    }
                },
                actions = {
                    if (groupName.lowercase() == "explorar") {
                        var isSearching by remember { mutableStateOf(false) }
                        if (isSearching) {
                            TextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Buscar...") },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent
                                ),
                                singleLine = true
                            )
                            IconButton(onClick = { isSearching = false; searchQuery = "" }) {
                                Icon(Icons.Default.Close, null, tint = AzureText)
                            }
                        } else {
                            IconButton(onClick = { isSearching = true }) {
                                Icon(Icons.Default.Search, null, tint = AzureText)
                            }
                        }
                    }
                    IconButton(onClick = onThemeToggle) {
                        Text(if (darkTheme) "☀️" else "🌙", fontSize = 20.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AzureBackground)
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AzureBlue)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                items(filteredBooks) { book ->
                    val isFav = favorites.any { it.bookId == book.id && (it.userId == userId || it.userId == firebaseUid) }
                    val progress = readingProgress.find { it.bookId == book.id }?.let { 
                        if (it.totalPages > 0) it.currentPage.toFloat() / it.totalPages else 0f 
                    } ?: 0f

                    BookCard(
                        book = book,
                        isFavorite = isFav,
                        progress = progress,
                        onClick = {
                            scope.launch {
                                try {
                                    val response = BooksService.getBookFileUrl(book.id!!, userId.ifBlank { firebaseUid })
                                    ReaderState.currentPdf = response.url.toUri()
                                    navController.navigate("reader?bookId=${book.id}")
                                } catch (_: Exception) {
                                    Toast.makeText(context, "Error de acceso al PDF", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        onLongClick = {
                            selectedBook = book
                            showMenu = true
                        }
                    )
                }
            }
        }
    }

    if (showMenu && selectedBook != null) {
        val isFav = favorites.any { it.bookId == selectedBook?.id && (it.userId == userId || it.userId == firebaseUid) }
        ModalBottomSheet(
            onDismissRequest = { showMenu = false },
            containerColor = AzureSurface,
            contentColor = AzureText
        ) {
            Column(Modifier.padding(bottom = 32.dp)) {
                ListItem(
                    headlineContent = { Text(if (isFav) "Quitar de favoritos" else "Añadir a favoritos") },
                    leadingContent = { Text(if (isFav) "💔" else "❤️", fontSize = 20.sp) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.clickable {
                        scope.launch {
                            try {
                                if (isFav) {
                                    val fav = favorites.find { it.bookId == selectedBook?.id }
                                    fav?.id?.let { BooksService.removeFavorite(it) }
                                } else {
                                    BooksService.addFavorite(selectedBook?.id!!, userId)
                                }
                                loadData()
                                showMenu = false
                            } catch (_: Exception) {
                                Toast.makeText(context, "Error en favoritos", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
                ListItem(
                    headlineContent = { Text("Me gusta") },
                    leadingContent = { Text("👍", fontSize = 20.sp) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.clickable {
                        scope.launch {
                            try {
                                selectedBook?.id?.let { BooksService.toggleLike(it) }
                                showMenu = false
                            } catch (_: Exception) {
                                Toast.makeText(context, "Error al dar Me gusta", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
                
                if (selectedBook?.userId == userId || selectedBook?.userId == firebaseUid) {
                    HorizontalDivider(color = AzureBorder, modifier = Modifier.padding(vertical = 8.dp))
                    
                    ListItem(
                        headlineContent = { Text("Editar Libro") },
                        leadingContent = { Text("✏️", fontSize = 20.sp) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable {
                            showMenu = false
                            navController.navigate("edit-book/${selectedBook?.id}")
                        }
                    )

                    ListItem(
                        headlineContent = { Text("Eliminar Libro") },
                        leadingContent = { Text("🗑️", fontSize = 20.sp) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent, headlineColor = Color.Red),
                        modifier = Modifier.clickable {
                            scope.launch {
                                try {
                                    selectedBook?.id?.let { BooksService.deleteBook(it) }
                                    loadData()
                                    showMenu = false
                                    Toast.makeText(context, "Libro eliminado", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error al eliminar: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun BookCard(book: Book, isFavorite: Boolean, progress: Float, onClick: () -> Unit, onLongClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = AzureSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, AzureBorder)
        ) {
            Box {
                AsyncImage(
                    model = book.coverUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(240.dp),
                    contentScale = ContentScale.Crop
                )
                if (isFavorite) {
                    Surface(
                        color = Color.Black.copy(0.6f),
                        shape = RoundedCornerShape(bottomStart = 8.dp),
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Text("❤️", Modifier.padding(4.dp), fontSize = 14.sp)
                    }
                }
                if (progress > 0) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(4.dp),
                        color = AzureBlue,
                        trackColor = Color.Transparent
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(book.title, color = AzureText, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(book.author, color = AzureTextSecondary, style = MaterialTheme.typography.bodySmall)
    }
}
