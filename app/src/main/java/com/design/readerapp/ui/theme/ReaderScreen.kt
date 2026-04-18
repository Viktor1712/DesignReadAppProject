@file:OptIn(ExperimentalMaterial3Api::class)

package com.design.readerapp.ui.theme

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.barteksc.pdfviewer.PDFView
import com.design.readerapp.BooksService
import com.design.readerapp.ReadingProgress
import com.design.readerapp.ReaderState
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

@Composable
fun ReaderScreen(bookId: Int = -1) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userId = 1
    val pdfUri = ReaderState.currentPdf ?: return

    var page by remember { mutableStateOf(0) }
    var total by remember { mutableStateOf(0) }
    var showUI by remember { mutableStateOf(true) }
    var isLoadingPdf by remember { mutableStateOf(false) }
    var localPdfFile by remember { mutableStateOf<File?>(null) }
    
    val prefs = context.getSharedPreferences("reader_prefs", Context.MODE_PRIVATE)
    val key = "page_${userId}_${bookId}"
    val savedPage = remember { prefs.getInt(key, 0) }

    // Download PDF if it's a remote URL
    LaunchedEffect(pdfUri) {
        val uriString = pdfUri.toString()
        if (uriString.startsWith("http")) {
            isLoadingPdf = true
            withContext(Dispatchers.IO) {
                try {
                    val file = File(context.cacheDir, "book_${bookId}.pdf")
                    // Simple download logic
                    URL(uriString).openStream().use { input ->
                        file.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    localPdfFile = file
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    isLoadingPdf = false
                }
            }
        } else {
            // It's a local URI (e.g. from file picker)
            // Note: This might need conversion to File if PDFView.fromUri fails for remote
            // But if it's already local, we can try fromUri or copy to file
        }
    }

    // Sync progress with API
    LaunchedEffect(page, total) {
        if (bookId != -1 && total > 0) {
            try {
                BooksService.updateReadingProgress(
                    ReadingProgress(
                        userId = userId,
                        bookId = bookId,
                        currentPage = page,
                        totalPages = total,
                        percentage = if (total > 0) (page * 100) / total else 0
                    )
                )
                prefs.edit().putInt(key, page).apply()
            } catch (e: Exception) {
                // Ignore sync errors
            }
        }
    }

    Scaffold(
        topBar = {
            if (showUI) {
                TopAppBar(
                    title = { Text("Lector") },
                    navigationIcon = {
                        // We could add a back button here if needed
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    )
                )
            }
        }
    ) { padding ->

        Box(Modifier.fillMaxSize().padding(padding)) {
            if (isLoadingPdf) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Descargando libro...", style = MaterialTheme.typography.bodyMedium)
                }
            } else if (localPdfFile != null || (!pdfUri.toString().startsWith("http"))) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        PDFView(ctx, null).apply {
                            val configurator = if (localPdfFile != null) {
                                fromFile(localPdfFile)
                            } else {
                                fromUri(pdfUri)
                            }
                            
                            configurator
                                .defaultPage(savedPage)
                                .onLoad { t -> total = t }
                                .onPageChange { p, _ -> page = p }
                                .onTap {
                                    showUI = !showUI
                                    true
                                }
                                .load()
                        }
                    },
                    update = { _ -> }
                )
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No se pudo cargar el PDF")
                }
            }

            if (showUI && total > 0) {
                val progress = if (total > 0) (page + 1).toFloat() / total else 0f

                Column(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color.Black.copy(0.75f))
                ) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = Color.White.copy(alpha = 0.2f),
                        strokeCap = StrokeCap.Round
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Página ${page + 1} de $total",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                        )
                        
                        Text(
                            "${(progress * 100).toInt()}%",
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
