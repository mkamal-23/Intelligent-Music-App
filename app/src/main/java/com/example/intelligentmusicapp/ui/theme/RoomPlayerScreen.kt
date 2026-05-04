package com.example.intelligentmusicapp.ui.theme

import android.media.MediaPlayer
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.style.TextAlign

@Composable
fun RoomPlayerScreen(
    viewModel: RoomViewModel = viewModel(),
    onLeave: () -> Unit
) {
    val roomState by viewModel.roomState.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val allSongs by viewModel.allSongs.collectAsState()

    var showChat by remember { mutableStateOf(false) }
    var showQueue by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var currentLoadedUrl by remember { mutableStateOf("") }
    var chatInput by remember { mutableStateOf("") }
    var sliderPosition by remember { mutableStateOf(0f) }
    var duration by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) { viewModel.fetchAllSongs() }

    // MediaPlayer: sync with room state
    LaunchedEffect(roomState.currentSongUrl) {
        if (roomState.currentSongUrl.isNotEmpty() && roomState.currentSongUrl != currentLoadedUrl) {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(roomState.currentSongUrl)
                prepareAsync()
                setOnPreparedListener {
                    duration = it.duration.toFloat()
                    if (roomState.isPlaying) start()
                    if (roomState.seekPosition > 0) seekTo(roomState.seekPosition.toInt())
                }
            }
            currentLoadedUrl = roomState.currentSongUrl
        }
    }

    LaunchedEffect(roomState.isPlaying) {
        mediaPlayer?.let {
            if (roomState.isPlaying && !it.isPlaying) it.start()
            else if (!roomState.isPlaying && it.isPlaying) it.pause()
        }
    }

    // Listener: sync seek from host
    LaunchedEffect(roomState.seekPosition) {
        if (!roomState.isHost) {
            mediaPlayer?.let {
                val diff = kotlin.math.abs(it.currentPosition - roomState.seekPosition)
                if (diff > 3000) it.seekTo(roomState.seekPosition.toInt())
            }
        }
    }

    // Update slider while playing
    LaunchedEffect(mediaPlayer, roomState.isPlaying) {
        while (true) {
            delay(500)
            mediaPlayer?.let {
                if (it.isPlaying) sliderPosition = it.currentPosition.toFloat()
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
            viewModel.leaveRoom()
        }
    }

    // ─── UI ───────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {

            // Top bar
            item{Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Room: ${roomState.roomCode}",
                        color = Color(0xFF1DB954),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        "${if (roomState.isHost) "🎙 Host" else "🎧 Listener"} · ${roomState.members} members",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
                TextButton(onClick = {
                    mediaPlayer?.release()
                    viewModel.leaveRoom()
                    onLeave()
                }) {
                    Text("Leave", color = Color.Red)
                }
            }}

            // Album art
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .padding(horizontal = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (roomState.currentSongImage.isNotEmpty()) {
                        AsyncImage(
                            model = roomState.currentSongImage,
                            contentDescription = null,
                            modifier = Modifier
                                .size(220.dp)
                                .clip(RoundedCornerShape(20.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(220.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFF1E1E1E)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🎵", fontSize = 64.sp)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
            // Song info
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    Text(
                        text = roomState.currentSongTitle.ifEmpty { "No song playing" },
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = roomState.currentSongArtist,
                        color = Color.Gray,
                        fontSize = 15.sp
                    )
                }

                Spacer(Modifier.height(12.dp))
            }
            // Seek bar (host only)
            item{
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Slider(
                    value = sliderPosition,
                    onValueChange = {
                        if (roomState.isHost) {
                            sliderPosition = it
                        }
                    },
                    onValueChangeFinished = {
                        if (roomState.isHost) {
                            mediaPlayer?.seekTo(sliderPosition.toInt())
                            viewModel.seekTo(sliderPosition.toLong())
                        }
                    },
                    valueRange = 0f..if (duration > 0f) duration else 1f,
                    enabled = roomState.isHost,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF1DB954),
                        activeTrackColor = Color(0xFF1DB954),
                        inactiveTrackColor = Color(0xFF333333)
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(formatTime(sliderPosition.toLong()), color = Color.Gray, fontSize = 11.sp)
                    Text(formatTime(duration.toLong()), color = Color.Gray, fontSize = 11.sp)
                }
            }

            Spacer(Modifier.height(8.dp))}
            // Controls (host only — listeners see locked icons)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Prev
                    IconButton(
                        onClick = { if (roomState.isHost) viewModel.playPrev() },
                        enabled = roomState.isHost
                    ) {
                        Icon(
                            Icons.Default.SkipPrevious,
                            contentDescription = "Previous",
                            tint = if (roomState.isHost) Color.White else Color(0xFF444444),
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // Play / Pause
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(
                                if (roomState.isHost) Color(0xFF1DB954) else Color(
                                    0xFF333333
                                )
                            )
                            .clickable(enabled = roomState.isHost) { viewModel.togglePlayPause() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (roomState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = if (roomState.isHost) Color.Black else Color(0xFF666666),
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // Next
                    IconButton(
                        onClick = { if (roomState.isHost) viewModel.playNext() },
                        enabled = roomState.isHost
                    ) {
                        Icon(
                            Icons.Default.SkipNext,
                            contentDescription = "Next",
                            tint = if (roomState.isHost) Color.White else Color(0xFF444444),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
            item{
            if (!roomState.isHost) {
                Text(
                    "🔒 Only the host can control playback",
                    color = Color(0xFF555555),
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(16.dp))}
            item{
            // Bottom tabs: Queue | Chat
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showQueue = !showQueue; showChat = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (showQueue) Color(0xFF1DB954) else Color(0xFF1E1E1E)
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("🎵 Queue", color = if (showQueue) Color.Black else Color.White)
                }
                Button(
                    onClick = { showChat = !showChat; showQueue = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (showChat) Color(0xFF1DB954) else Color(0xFF1E1E1E)
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("💬 Chat", color = if (showChat) Color.Black else Color.White)
                }
            }}
            // Queue Panel
            item{
            if (showQueue) {
                Column(modifier = Modifier.heightIn(max = 300.dp).verticalScroll(rememberScrollState())) {
                    if (roomState.isHost) {

                            Text("All Songs", color = Color.Gray, fontSize = 12.sp,
                                modifier = Modifier.padding(vertical = 4.dp))

                        allSongs.forEach { song ->
                            val inQueue = roomState.queue.contains(song.songid)
                            val isPlaying = roomState.currentSongId == song.songid

                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = when {
                                        isPlaying -> Color(0xFF1A3320)
                                        inQueue -> Color(0xFF1E1E1E)
                                        else -> Color(0xFF111111)
                                    }
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            song.title,
                                            color = if (isPlaying) Color(0xFF1DB954) else Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(song.artist, color = Color.Gray, fontSize = 12.sp)
                                    }
                                    Row {
                                        if (!isPlaying) {
                                            IconButton(onClick = { viewModel.playSong(song) }) {
                                                Icon(Icons.Default.PlayArrow,
                                                    contentDescription = null, tint = Color(0xFF1DB954))
                                            }
                                        }
                                        IconButton(onClick = {
                                            if (inQueue) viewModel.removeFromQueue(song.songid)
                                            else viewModel.addToQueue(song.songid)
                                        }) {
                                            Icon(
                                                if (inQueue) Icons.Default.Remove else Icons.Default.Add,
                                                contentDescription = null,
                                                tint = if (inQueue) Color.Red else Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Listener: only see queue
                        roomState.queue.forEach { songId ->
                            val song = allSongs.find { it.songid == songId }
                            val isPlaying = roomState.currentSongId == songId
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isPlaying) Color(0xFF1A3320) else Color(0xFF1E1E1E)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        if (isPlaying) "▶ " else "  ",
                                        color = Color(0xFF1DB954)
                                    )
                                    Column {
                                        Text(
                                            song?.title ?: songId,
                                            color = if (isPlaying) Color(0xFF1DB954) else Color.White,
                                            fontSize = 14.sp
                                        )
                                        Text(song?.artist ?: "", color = Color.Gray, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }}
            item {

                // Chat Panel
                if (showChat) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .heightIn(max = 300.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            chatMessages.reversed().forEach { msg ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF1A1A1A), RoundedCornerShape(10.dp))
                                        .padding(10.dp)
                                ) {
                                    Text(msg.senderName, color = Color(0xFF1DB954), fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold)
                                    Text(msg.text, color = Color.White, fontSize = 14.sp)
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = chatInput,
                                onValueChange = { chatInput = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("Type a message...", color = Color.Gray) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF1DB954),
                                    unfocusedBorderColor = Color(0xFF333333),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                maxLines = 2
                            )
                            IconButton(
                                onClick = {
                                    if (chatInput.isNotBlank()) {
                                        viewModel.sendMessage(chatInput.trim())
                                        chatInput = ""
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Default.Send, contentDescription = "Send",
                                    tint = Color(0xFF1DB954)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun formatTime(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return "%d:%02d".format(min, sec)
}