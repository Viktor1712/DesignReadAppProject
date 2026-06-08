package com.design.readerapp.ui.theme

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Subir Nuevo Libro", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = MaterialTheme.colorScheme.onBackground)
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                "DETALLES DEL LIBRO",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    UploadInputField("Título del libro", title) { title = it }
                    UploadInputField("Autor", author) { author = it }
                    UploadInputField("Categoría (fantasía, drama, etc.)", category) { category = it }
                }
            }

            Text(
                "ARCHIVOS Y MEDIA",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // PDF Selector Card
                FileSelectorCard(
                    title = if (pdfUri == null) "Seleccionar PDF" else "PDF Cargado",
                    subtitle = pdfUri?.lastPathSegment ?: "El archivo principal de lectura",
                    icon = "📄",
                    onClick = { pdfPicker.launch("application/pdf") },
                    isSelected = pdfUri != null
                )

                // Image Selector Card
                FileSelectorCard(
                    title = if (imageUri == null) "Seleccionar Portada" else "Portada Cargada",
                    subtitle = imageUri?.lastPathSegment ?: "Imagen JPG/PNG para la biblioteca",
                    icon = "🖼️",
                    onClick = { imagePicker.launch("image/*") },
                    isSelected = imageUri != null
                )
            }

            if (imageUri != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Box(modifier = Modifier.height(200.dp).fillMaxWidth()) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = "Vista previa",
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

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
                            val currentUserId = BooksService.currentUser?.id
                            if (currentUserId == null) throw Exception("Usuario no autenticado en BooksService")

                            BooksService.createBook(Book(
                                title = title,
                                author = author,
                                category = category,
                                pdfBlobName = pdfSas.blobName,
                                coverBlobName = coverSas.blobName,
                                userId = currentUserId
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
                modifier = Modifier.fillMaxWidth().height(50.dp).padding(bottom = 0.dp),
                enabled = !isUploading,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                if (isUploading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                } else {
                    Text("SUBIR Y PUBLICAR", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun UploadInputField(label: String, value: String, onValueChange: (String) -> Unit) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileSelectorCard(title: String, subtitle: String, icon: String, onClick: () -> Unit, isSelected: Boolean) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.background
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(icon, fontSize = 20.sp)
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, maxLines = 1)
            }

            if (isSelected) {
                Text("✅", fontSize = 16.sp)
            } else {
                Text("📁", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
