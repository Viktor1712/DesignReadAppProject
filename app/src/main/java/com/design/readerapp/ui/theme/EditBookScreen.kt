package com.design.readerapp.ui.theme

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.design.readerapp.Book
import com.design.readerapp.BooksService
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBookScreen(navController: NavController, bookId: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var book by remember { mutableStateOf<Book?>(null) }

    LaunchedEffect(bookId) {
        try {
            isLoading = true
            val fetchedBook = BooksService.getBookById(bookId)
            book = fetchedBook
            title = fetchedBook.title
            author = fetchedBook.author
            category = fetchedBook.category
        } catch (e: Exception) {
            Toast.makeText(context, "Error al cargar libro", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        containerColor = AzureBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Editar Libro", fontWeight = FontWeight.Bold, color = AzureText) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = AzureText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AzureBackground)
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AzureBlue)
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AzureBlue)
                )
                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("Autor") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AzureBlue)
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Categoría") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AzureBlue)
                )

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (title.isBlank() || author.isBlank() || category.isBlank()) {
                            Toast.makeText(context, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        
                        scope.launch {
                            try {
                                isSaving = true
                                val updatedBook = book!!.copy(
                                    title = title,
                                    author = author,
                                    category = category
                                )
                                BooksService.updateBook(bookId, updatedBook)
                                Toast.makeText(context, "Libro actualizado", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                            } finally {
                                isSaving = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = !isSaving,
                    colors = ButtonDefaults.buttonColors(containerColor = AzureBlue)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text("Guardar Cambios")
                    }
                }
            }
        }
    }
}
