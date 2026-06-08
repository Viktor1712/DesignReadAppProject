package com.design.readerapp.ui.theme

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.design.readerapp.Book
import com.design.readerapp.BooksService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var pdfUri by remember { mutableStateOf<Uri?>(null) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    val pdfPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { pdfUri = it }
    )

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { imageUri = it }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Subir Libro") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Text("⬅️")
                    }
                }
            )
        }
    ) { padding ->
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
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = author,
                onValueChange = { author = it },
                label = { Text("Autor") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Categoría (e.g. fantasia)") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { pdfPicker.launch("application/pdf") },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (pdfUri == null) "Seleccionar PDF" else "PDF Seleccionado")
                }
                
                Button(
                    onClick = { imagePicker.launch("image/*") },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (imageUri == null) "Seleccionar Portada" else "Portada Seleccionada")
                }
            }
            
            if (pdfUri != null) {
                Text("Archivo PDF: ${pdfUri?.lastPathSegment}", style = MaterialTheme.typography.bodySmall)
            }
            if (imageUri != null) {
                Text("Portada: ${imageUri?.lastPathSegment}", style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    if (title.isBlank() || author.isBlank() || category.isBlank() || pdfUri == null || imageUri == null) {
                        Toast.makeText(context, "Completa todos los campos, incluyendo la portada", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    
                    scope.launch {
                        try {
                            isUploading = true
                            
                            // 1. Subir PDF
                            val pdfFileName = pdfUri?.lastPathSegment ?: "book.pdf"
                            val pdfSas = BooksService.generateUploadUrl(pdfFileName, "application/pdf")
                            val pdfBytes = context.contentResolver.openInputStream(pdfUri!!)?.readBytes() ?: throw Exception("Error leyendo PDF")
                            
                            withContext(Dispatchers.IO) {
                                val client = OkHttpClient()
                                val pdfRequest = Request.Builder()
                                    .url(pdfSas.url)
                                    .put(pdfBytes.toRequestBody("application/pdf".toMediaType()))
                                    .header("x-ms-blob-type", "BlockBlob")
                                    .build()
                                client.newCall(pdfRequest).execute().use { if (!it.isSuccessful) throw Exception("Error subiendo PDF") }
                            }

                            // 2. Subir Portada
                            val coverFileName = imageUri?.lastPathSegment ?: "cover.jpg"
                            val coverSas = BooksService.generateUploadUrl(coverFileName, "image/jpeg")
                            val coverBytes = context.contentResolver.openInputStream(imageUri!!)?.readBytes() ?: throw Exception("Error leyendo Portada")
                            
                            withContext(Dispatchers.IO) {
                                val client = OkHttpClient()
                                val coverRequest = Request.Builder()
                                    .url(coverSas.url)
                                    .put(coverBytes.toRequestBody("image/jpeg".toMediaType()))
                                    .header("x-ms-blob-type", "BlockBlob")
                                    .build()
                                client.newCall(coverRequest).execute().use { if (!it.isSuccessful) throw Exception("Error subiendo Portada") }
                            }
                            
                            // 3. Registrar en la base de datos
                            BooksService.createBook(Book(
                                title = title,
                                author = author,
                                category = category,
                                pdfBlobName = pdfSas.blobName,
                                coverBlobName = coverSas.blobName,
                                userId = BooksService.currentUser?.id
                            ))
                            
                            Toast.makeText(context, "¡Libro y portada subidos con éxito!", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                        } finally {
                            isUploading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isUploading
            ) {
                if (isUploading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Subir y Publicar")
                }
            }
        }
    }
}
