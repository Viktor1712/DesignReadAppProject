package com.design.readerapp.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.layout.ContentScale
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.design.readerapp.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, darkTheme: Boolean, onThemeToggle: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    var categories by remember { mutableStateOf(listOf<Category>()) }
    var readingProgress by remember { mutableStateOf(listOf<ReadingProgress>()) }
    var favorites by remember { mutableStateOf(listOf<Favorite>()) }
    var allBooks by remember { mutableStateOf(listOf<Book>()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }

    val userId = BooksService.currentUser?.id ?: ""
    val firebaseUid = BooksService.currentUser?.firebaseUid ?: ""

    LaunchedEffect(Unit) {
        try {
            isLoading = true
            categories = BooksService.getCategories()
            allBooks = BooksService.getBooks()
            readingProgress = try { BooksService.getReadingProgress() } catch (e: Exception) { emptyList() }
            favorites = try { BooksService.getFavorites() } catch (e: Exception) { emptyList() }
            
            // Cargar portadas para todos los libros visibles
            val userFavIds = favorites.filter { it.userId == userId || it.userId == firebaseUid }.map { it.bookId }
            val booksToLoad = allBooks.filter { book -> 
                readingProgress.any { it.bookId == book.id } || book.id in userFavIds
            }

            booksToLoad.forEach { book ->
                launch {
                    try {
                        val sas = BooksService.getBookCoverUrl(book.id!!, userId.ifBlank { firebaseUid })
                        allBooks = allBooks.map { 
                            if (it.id == book.id) it.copy(coverUrl = sas.url) else it 
                        }
                    } catch (_: Exception) {}
                }
            }
        } catch (e: Exception) {
            // Reintentar si falla la sesión
            isLoading = false
        } finally {
            if (categories.isEmpty()) {
                categories = listOf(
                    Category(name = "fantasia", label = "Fantasía"),
                    Category(name = "ciencia", label = "Ciencia Ficción"),
                    Category(name = "clasico", label = "Clásicos"),
                    Category(name = "misterio", label = "Misterio")
                )
            }
            isLoading = false
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = AzureSurface,
                drawerShape = RoundedCornerShape(0.dp)
            ) {
                Spacer(Modifier.height(24.dp))
                Text(
                    "BIBLIO-TEC",
                    modifier = Modifier.padding(24.dp),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = AzureBlue
                )
                HorizontalDivider(color = AzureBorder, modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(Modifier.height(16.dp))
                
                DrawerItem("Inicio", Icons.Default.Home) { 
                    scope.launch { drawerState.close() } 
                }
                DrawerItem("Mi Biblioteca", Icons.AutoMirrored.Filled.MenuBook) { 
                    navController.navigate("group/mis-libros")
                }
                DrawerItem("Comunidad", Icons.Default.Public) { 
                    navController.navigate("group/explorar")
                }
                DrawerItem("Notificaciones", Icons.Default.Notifications) {
                    navController.navigate("notifications")
                }
                DrawerItem("Mi Perfil", Icons.Default.Person) {
                    navController.navigate("profile")
                }
                DrawerItem("Favoritos", Icons.Default.Favorite) { 
                    navController.navigate("group/favoritos")
                }
                
                Spacer(Modifier.weight(1f))
                
                DrawerItem("Cerrar Sesión", Icons.AutoMirrored.Filled.Logout) {
                    auth.signOut()
                    navController.navigate("login") { popUpTo(0) }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    ) {
        Scaffold(
            containerColor = AzureBackground,
            topBar = {
                TopAppBar(
                    title = { 
                        Column {
                            val name = BooksService.currentUser?.name?.split(" ")?.firstOrNull() ?: "Lector"
                            Text("Hola, $name", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = AzureText)
                            Text("Bienvenido de vuelta", style = MaterialTheme.typography.bodySmall, color = AzureTextSecondary)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, "Menu", tint = AzureText)
                        }
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate("upload") }) {
                            Icon(Icons.Default.Add, "Subir", tint = AzureBlue)
                        }
                        IconButton(onClick = onThemeToggle) {
                            Text(if (darkTheme) "☀️" else "🌙", fontSize = 20.sp)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = AzureBackground)
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search books...", color = AzureTextSecondary) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = AzureTextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AzureBlue,
                        unfocusedBorderColor = AzureBorder,
                        focusedContainerColor = AzureSurface,
                        unfocusedContainerColor = AzureSurface,
                        cursorColor = AzureBlue
                    )
                )

                if (isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AzureBlue)
                    }
                } else {
                    val continuingBooks = allBooks.filter { book -> 
                        readingProgress.any { it.bookId == book.id && it.currentPage > 0 } 
                    }
                    val userFavIds = favorites.filter { it.userId == userId || it.userId == firebaseUid }.map { it.bookId }
                    val libraryBooks = allBooks.filter { it.id in userFavIds }

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                        if (continuingBooks.isNotEmpty()) {
                            item {
                                SectionHeader("Continuar leyendo", "Ver todos") { navController.navigate("group/mis-libros") }
                                Spacer(Modifier.height(16.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    items(continuingBooks) { book ->
                                        val progress = readingProgress.find { it.bookId == book.id }
                                        val fraction = if (progress != null && progress.totalPages > 0) {
                                            progress.currentPage.toFloat() / progress.totalPages
                                        } else 0f

                                        ContinueReadingCard(book, fraction) {
                                            openBook(book, scope, context, userId, firebaseUid, navController)
                                        }
                                    }
                                }
                            }
                        }

                        if (libraryBooks.isNotEmpty()) {
                            item {
                                SectionHeader("Mi Biblioteca", "Ver todos") { navController.navigate("group/favoritos") }
                                Spacer(Modifier.height(16.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    items(libraryBooks) { book ->
                                        val progress = readingProgress.find { it.bookId == book.id }
                                        val fraction = if (progress != null && progress.totalPages > 0) {
                                            progress.currentPage.toFloat() / progress.totalPages
                                        } else 0f
                                        
                                        BookLibraryCard(book, fraction) {
                                            openBook(book, scope, context, userId, firebaseUid, navController)
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Text("Géneros populares", fontWeight = FontWeight.Bold, color = AzureText)
                            Spacer(Modifier.height(12.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(categories) { category ->
                                    FilterChip(
                                        selected = false,
                                        onClick = { navController.navigate("group/${category.name}") },
                                        label = { Text(category.label) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = AzureSurface,
                                            labelColor = AzureTextSecondary
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(borderColor = AzureBorder, enabled = true, selected = false)
                                    )
                                }
                            }
                        }

                        item {
                            SectionHeader("Mi Actividad", "Ver todo") { navController.navigate("group/mis-libros") }
                            Spacer(Modifier.height(16.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                ActivityCard("Mis Libros", "📖", AzureBlue, Modifier.weight(1f)) {
                                    navController.navigate("group/mis-libros")
                                }
                                ActivityCard("Favoritos", "❤️", Color(0xFFEF4444), Modifier.weight(1f)) {
                                    navController.navigate("group/favoritos")
                                }
                            }
                        }

                        item {
                            SectionHeader("Recomendados", "Descubrir") { 
                                navController.navigate("group/explorar")
                            }
                            Spacer(Modifier.height(16.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth().height(100.dp).clickable {
                                    navController.navigate("group/explorar")
                                },
                                colors = CardDefaults.cardColors(containerColor = AzureSurface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AzureBorder)
                            ) {
                                Row(Modifier.fillMaxSize().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)).background(AzureBackground), contentAlignment = Alignment.Center) {
                                        Text("📚", fontSize = 24.sp)
                                    }
                                    Spacer(Modifier.width(16.dp))
                                    Column {
                                        Text("Explora la comunidad", fontWeight = FontWeight.Bold, color = AzureText)
                                        Text("Encuentra libros de otros lectores.", fontSize = 12.sp, color = AzureTextSecondary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BookLibraryCard(book: Book, progress: Float, onClick: () -> Unit) {
    Card(
        modifier = Modifier.width(120.dp).clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = AzureSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, AzureBorder)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = book.coverUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    contentScale = ContentScale.Crop
                )
                if (progress > 0) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(3.dp),
                        color = AzureBlue,
                        trackColor = Color.Transparent
                    )
                }
            }
            Text(
                text = book.title,
                modifier = Modifier.padding(8.dp),
                color = AzureText,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                fontSize = 12.sp,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

private fun openBook(
    book: Book,
    scope: kotlinx.coroutines.CoroutineScope,
    context: android.content.Context,
    userId: String,
    firebaseUid: String,
    navController: NavController
) {
    scope.launch {
        try {
            val response = BooksService.getBookFileUrl(book.id!!, userId.ifBlank { firebaseUid })
            ReaderState.currentPdf = response.url.toUri()
            navController.navigate("reader?bookId=${book.id}")
        } catch (_: Exception) {
            android.widget.Toast.makeText(context, "Error al abrir el PDF", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
fun ContinueReadingCard(book: Book, progress: Float, onClick: () -> Unit) {
    Card(
        modifier = Modifier.width(160.dp).clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = AzureSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, AzureBorder)
    ) {
        Column {
            AsyncImage(
                model = book.coverUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(200.dp),
                contentScale = ContentScale.Crop
            )
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(4.dp),
                color = AzureBlue,
                trackColor = AzureBorder
            )
            Column(Modifier.padding(8.dp)) {
                Text(book.title, color = AzureText, fontWeight = FontWeight.Bold, maxLines = 1, fontSize = 14.sp)
                Text("${(progress * 100).toInt()}% completado", color = AzureTextSecondary, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun DrawerItem(label: String, icon: ImageVector, onClick: () -> Unit) {
    NavigationDrawerItem(
        label = { Text(label, fontWeight = FontWeight.Medium) },
        selected = false,
        onClick = onClick,
        icon = { Icon(icon, null) },
        colors = NavigationDrawerItemDefaults.colors(
            unselectedContainerColor = Color.Transparent,
            unselectedIconColor = AzureTextSecondary,
            unselectedTextColor = AzureTextSecondary
        ),
        modifier = Modifier.padding(horizontal = 12.dp)
    )
}

@Composable
fun SectionHeader(title: String, action: String, onAction: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, fontWeight = FontWeight.Bold, color = AzureText, modifier = Modifier.weight(1f))
        Text(action, fontSize = 12.sp, color = AzureBlue, modifier = Modifier.clickable { onAction() })
    }
}

@Composable
fun ActivityCard(title: String, icon: String, accent: Color, modifier: Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = AzureSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, AzureBorder)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(icon, fontSize = 24.sp)
            Spacer(Modifier.height(8.dp))
            Text(title, fontWeight = FontWeight.Bold, color = AzureText)
            Box(Modifier.size(24.dp, 2.dp).background(accent))
        }
    }
}
