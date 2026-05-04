package com.example.intelligentmusicapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import com.example.intelligentmusicapp.ui.theme.MainView
import com.example.intelligentmusicapp.ui.theme.IntelligentMusicAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IntelligentMusicAppTheme {
                // Surface: like a white canvas to draw on
                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainView()
                }
            }
        }
    }
}

