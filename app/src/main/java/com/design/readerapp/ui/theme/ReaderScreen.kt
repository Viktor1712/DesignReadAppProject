@file:OptIn(ExperimentalMaterial3Api::class)

package com.design.readerapp.ui.theme

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.github.barteksc.pdfviewer.PDFView
import com.design.readerapp.BooksService
import com.design.readerapp.ReaderState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream

@Composable
fun ReaderScreen(navController: NavController, bookId: String = "") {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userId = BooksService.currentUser?.id ?: ""
    val pdfUri = ReaderState.currentPdf

    var page by remember { mutableStateOf(0) }
    var total by remember { mutableStateOf(0) }
    var showUI by remember { mutableStateOf(true) }
    var isLoadingPdf by remember { mutableStateOf(false) }
    var localPdfFile by remember { mutableStateOf<File?>(null) }
    var errorLoading by remember { mutableStateOf<String?>(null) }

    val prefs = context.getSharedPreferences("reader_prefs", Context.MODE_PRIVATE)
    val key = "page_${userId}_${bookId}"
    var initialPage by remember { mutableIntStateOf(prefs.getInt(key, 0)) }
    var savedPage by remember { mutableIntStateOf(initialPage) }

    LaunchedEffect(bookId) {
        if (bookId.isNotEmpty()) {
            try {
                val progress = BooksService.getReadingProgress()
                val bookProgress = progress.find { it.bookId == bookId }
                bookProgress?.let {
                    initialPage = it.currentPage
                    prefs.edit().putInt(key, it.currentPage).apply()
                }
            } catch (_: Exception) {}
        }
    }

    val progressFloat = if (total > 0) (page + 1).toFloat() / total.toFloat() else 0f
    val progressPercent = (progressFloat * 100).toInt().coerceIn(0, 100)
    val hasUnsavedChanges = page != savedPage

    fun saveProgress(p: Int, t: Int = total) {
        if (bookId.isNotEmpty() && t > 0) {
            scope.launch {
                try {
                    val perc = (((p + 1).toFloat() / t.toFloat()) * 100).toInt().coerceIn(0, 100)
                    BooksService.updateReadingProgress(
                        com.design.readerapp.ReadingProgress(
                            bookId = bookId,
                            userId = userId,
                            currentPage = p,
                            totalPages = t,
                            percentage = perc
                        )
                    )
                    prefs.edit().putInt(key, p).apply()
                    savedPage = p
                    android.widget.Toast.makeText(context, "Progreso guardado: Página ${p + 1}", android.widget.Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    android.widget.Toast.makeText(context, "Error al guardar progreso", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LaunchedEffect(pdfUri) {
        if (pdfUri == null) {
            errorLoading = "URL no encontrada"
            return@LaunchedEffect
        }

        val urlString = pdfUri.toString()
        if (urlString.startsWith("http")) {
            isLoadingPdf = true
            withContext(Dispatchers.IO) {
                try {
                    val file = File(context.cacheDir, "book_$bookId.pdf")
                    // Si el archivo existe pero la bookId es vacía (ej. abrir PDF externo), lo re-descargamos
                    if (file.exists() && file.length() > 0 && bookId.isNotEmpty()) {
                        localPdfFile = file
                        isLoadingPdf = false
                        return@withContext
                    }
                    
                    val request = Request.Builder().url(urlString).build()
                    val client = BooksService.getOkHttpClient() 
                    
                    val response: Response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        response.body?.byteStream()?.use { input ->
                            FileOutputStream(file).use { output ->
                                input.copyTo(output)
                            }
                        }
                        localPdfFile = file
                    } else {
                        errorLoading = "Error de servidor: ${response.code}"
                    }
                    response.close()
                } catch (e: Exception) {
                    errorLoading = "Error de red: ${e.localizedMessage}"
                } finally {
                    isLoadingPdf = false
                }
            }
        } else {
            // Caso Uri local (content://)
            try {
                val file = File(context.cacheDir, "temp_reader.pdf")
                context.contentResolver.openInputStream(pdfUri)?.use { input ->
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }
                localPdfFile = file
            } catch (e: Exception) {
                errorLoading = "Error local: ${e.localizedMessage}"
            }
        }
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            if (showUI) {
                CenterAlignedTopAppBar(
                    title = { Text("Visualizador", color = Color.White, fontSize = 16.sp) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Black.copy(alpha = 0.8f)
                    ),
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.Close, "Cerrar", tint = Color.White)
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (showUI && total > 0) {
                BottomAppBar(
                    containerColor = Color.Black.copy(alpha = 0.8f),
                    modifier = Modifier.wrapContentHeight()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = { progressFloat },
                            modifier = Modifier.fillMaxWidth(),
                            color = AzureBlue,
                            trackColor = Color.White.copy(alpha = 0.2f)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$progressPercent% completado",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                            if (hasUnsavedChanges) {
                                Text(
                                    text = "Cambios sin guardar",
                                    color = Color(0xFFFFCC44),
                                    fontSize = 12.sp
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Página ${page + 1} de $total",
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            Button(
                                onClick = { saveProgress(page, total) },
                                colors = ButtonDefaults.buttonColors(containerColor = AzureBlue),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Guardar Progreso")
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().background(Color.Black).padding(padding)) {
            if (isLoadingPdf) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = AzureBlue)
                    Spacer(Modifier.height(8.dp))
                    Text("Cargando PDF...", color = Color.White)
                }
            } else if (errorLoading != null) {
                Text(errorLoading!!, color = Color.Red, modifier = Modifier.align(Alignment.Center))
            } else if (localPdfFile != null) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        PDFView(ctx, null).apply {
                            fromFile(localPdfFile)
                                .defaultPage(initialPage)
                                .enableSwipe(true)
                                .swipeHorizontal(false)
                                .enableDoubletap(true)
                                .onLoad { t -> 
                                    total = t
                                }
                                .onPageChange { p, _ -> 
                                    page = p
                                }
                                .onTap { showUI = !showUI; true }
                                .load()
                        }
                    },
                    update = { pdfView ->
                        // Si cambia initialPage externamente, podríamos forzar el salto, 
                        // pero aquí PDFView maneja su propio estado interno.
                    }
                )
            }
        }
    }
}
