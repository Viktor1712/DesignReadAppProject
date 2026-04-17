package com.design.readerapp.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForumScreen() {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Foro") })
        }
    ) { padding ->
        Text(
            text = "Aquí irá el foro",
            modifier = Modifier.padding(padding)
        )
    }
}