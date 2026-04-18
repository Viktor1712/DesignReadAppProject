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
import coil.compose.AsyncImage
import com.design.readerapp.Book
import com.design.readerapp.BooksService
import com.design.readerapp.Favorite
import com.design.readerapp.ReadingProgress
import com.design.readerapp.ReaderState
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
    // For now, we use a fixed userId = 1 since we don't have mapping yet
    val userId = 1 

    var books by remember { mutableStateOf(listOf<Book>()) }
    var favorites by remember { mutableStateOf(listOf<Favorite>()) }
    var readingProgress by remember { mutableStateOf(listOf<ReadingProgress>()) }
    
    var selectedBook by remember { mutableStateOf<Book?>(null) }
    var showMenu by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    fun loadData() {
        scope.launch {
            try {
                isLoading = true
                val allBooks = BooksService.getBooks()
                val allFavorites = BooksService.getFavorites()
                val allProgress = BooksService.getReadingProgress()
                
                favorites = allFavorites.filter { it.userId == userId }
                readingProgress = allProgress.filter { it.userId == userId }

                books = if (groupName == "favoritos") {
                    val favIds = favorites.map { it.bookId }
                    allBooks.filter { it.id in favIds }
                } else {
                    allBooks.filter { it.category == groupName }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error al cargar datos", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(groupName) {
        loadData()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(groupName.replaceFirstChar { it.uppercase() }, fontWeight = FontWeight.Bold) },
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
                val progressObj = readingProgress.find { it.bookId == book.id }
                val progress = if (progressObj != null && progressObj.totalPages > 0) {
                    progressObj.currentPage.toFloat() / progressObj.totalPages
                } else 0f
                
                val isFav = favorites.any { it.bookId == book.id }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = {
                                ReaderState.currentPdf = Uri.parse(book.pdfUrl)
                                // We should also pass the book ID to the reader to update progress
                                navController.navigate("reader?bookId=${book.id}")
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
                                .height(220.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            AsyncImage(
                                model = book.coverUrl,
                                contentDescription = book.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            
                            if (isFav) {
                                Text(
                                    "❤️", 
                                    Modifier.align(Alignment.TopEnd).padding(8.dp),
                                    fontSize = 18.sp
                                )
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
                        
                        Column(Modifier.padding(12.dp)) {
                            Text(
                                text = book.title,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = book.author,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            if (progress > 0) {
                                Text(
                                    text = "${(progress * 100).toInt()}% completado",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showMenu && selectedBook != null) {
        val isFav = favorites.any { it.bookId == selectedBook?.id }
        
        ModalBottomSheet(onDismissRequest = { showMenu = false }) {
            Column(Modifier.padding(bottom = 32.dp)) {
                ListItem(
                    headlineContent = { Text(if (isFav) "Quitar de favoritos" else "Añadir a favoritos") },
                    leadingContent = { Text(if (isFav) "💔" else "❤️", fontSize = 20.sp) },
                    modifier = Modifier.clickable {
                        scope.launch {
                            try {
                                if (isFav) {
                                    val fav = favorites.find { it.bookId == selectedBook?.id }
                                    fav?.id?.let { BooksService.removeFavorite(it) }
                                } else {
                                    BooksService.addFavorite(Favorite(userId = userId, bookId = selectedBook?.id ?: 0))
                                }
                                loadData()
                                showMenu = false
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error al actualizar favoritos", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
                ListItem(
                    headlineContent = { Text("Ver detalles", color = MaterialTheme.colorScheme.primary) },
                    leadingContent = { Text("ℹ️", fontSize = 20.sp) },
                    modifier = Modifier.clickable {
                        // Show details dialog or similar
                        showMenu = false
                    }
                )
            }
        }
    }
}
