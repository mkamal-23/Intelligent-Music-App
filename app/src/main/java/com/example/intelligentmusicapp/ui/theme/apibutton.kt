package com.example.intelligentmusicapp.ui.theme

import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MicTopBar(onMicClick: () -> Unit) {
    TopAppBar(
        title = { Text("My App") },
        actions = {
            IconButton(onClick = onMicClick) {
                Icon(
                    imageVector = Icons.Filled.Mic,
                    contentDescription = "Mic"
                )
            }
        }
    )
}
