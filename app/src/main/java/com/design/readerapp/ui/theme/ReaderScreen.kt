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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.barteksc.pdfviewer.PDFView
import com.design.readerapp.ReaderState
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ReaderScreen() {

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val uid = auth.currentUser?.uid ?: "anonymous"
    val pdfUri = ReaderState.currentPdf ?: return

    var page by remember { mutableStateOf(0) }
    var total by remember { mutableStateOf(0) }
    var showUI by remember { mutableStateOf(true) }

    val key = "page_${uid}_${pdfUri}"
    val totalKey = "total_${uid}_${pdfUri}"

    LaunchedEffect(showUI) {
        if (showUI) {
            kotlinx.coroutines.delay(3000)
            showUI = false
        }
    }

    val prefs = context.getSharedPreferences("reader_prefs", Context.MODE_PRIVATE)
    val savedPage = prefs.getInt(key, 0)

    Scaffold(
        topBar = {
            if (showUI) {
                TopAppBar(
                    title = { Text("Lector") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    )
                )
            }
        }
    ) { padding ->

        Box(Modifier.fillMaxSize()) {

            AndroidView(
                modifier = Modifier.fillMaxSize().padding(padding),
                factory = {

                    PDFView(it, null).apply {

                        fromUri(pdfUri)
                            .defaultPage(savedPage)
                            .onLoad { 
                                total = it
                                prefs.edit().putInt(totalKey, it).apply()
                            }
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
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
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