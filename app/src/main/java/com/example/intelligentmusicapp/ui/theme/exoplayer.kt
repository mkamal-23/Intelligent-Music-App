package com.example.intelligentmusicapp.ui.theme

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.media3.exoplayer.ExoPlayer

@Composable
fun rememberExoPlayer(context: Context): ExoPlayer {
    return remember {
        ExoPlayer.Builder(context).build()
    }
}