@file:OptIn(ExperimentalMaterial3Api::class)

package com.design.readerapp.ui.theme

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import com.github.barteksc.pdfviewer.PDFView
import com.design.readerapp.ReaderState

@Composable
fun ReaderScreen() {

    val context = LocalContext.current
    val pdfUri = ReaderState.currentPdf ?: return

    var page by remember { mutableStateOf(0) }
    var total by remember { mutableStateOf(0) }
    var showUI by remember { mutableStateOf(true) }

    val key = "page_${pdfUri}"

    LaunchedEffect(showUI) {
        if (showUI) {
            kotlinx.coroutines.delay(3000)
            showUI = false
        }
    }

    val prefs = context.getSharedPreferences("reader_prefs", Context.MODE_PRIVATE)
    val savedPage = prefs.getInt(key, 0)

    Scaffold(
        topBar = { TopAppBar(title = { Text("Lector") }) }
    ) { padding ->

        Box(Modifier.fillMaxSize().padding(padding)) {

            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = {

                    PDFView(it, null).apply {

                        fromUri(pdfUri)
                            .defaultPage(savedPage)
                            .onLoad { total = it }
                            .onPageChange { p, _ ->
                                page = p
                                prefs.edit().putInt(key, p).apply()
                            }
                            .onTap {
                                showUI = !showUI
                                true
                            }
                            .load()
                    }
                }
            )

            if (showUI && total > 0) {

                val progress = (page + 1) / total.toFloat()

                Column(
                    Modifier
                        .align(androidx.compose.ui.Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color.Black.copy(0.7f))
                        .padding(12.dp)
                ) {

                    LinearProgressIndicator(progress = progress)

                    Spacer(Modifier.height(6.dp))

                    Text(
                        "Página ${page + 1} / $total",
                        color = Color.White
                    )
                }
            }
        }
    }
}