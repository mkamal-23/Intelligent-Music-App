package com.example.intelligentmusicapp.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import coil.compose.AsyncImage
import kotlinx.coroutines.delay

// ── Accent color ──────────────────────────────────────────────
private val Green = Color(0xFF1DB954)
private val BgDark = Color(0xFF0A0A0A)
private val Surface = Color(0xFF161616)
private val SurfaceLight = Color(0xFF222222)
private val TextPrimary = Color(0xFFF2F2F2)
private val TextSecondary = Color(0xFF888888)

@Composable
fun PlayerScreen(viewModel: MusicViewModel) {
    val context = LocalContext.current
    val player = rememberExoPlayer(context)
    val song = viewModel.currentSong ?: return

    // Load song into ExoPlayer
    LaunchedEffect(song.url) {
        val mediaItem = MediaItem.fromUri(song.url)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }

    // Track play state for UI
    var isPlaying by remember { mutableStateOf(true) }
    var isFavorited by remember { mutableStateOf(false) }

    LaunchedEffect(player) {
        while (true) {
            isPlaying = player.isPlaying
            delay(300)
        }
    }

    // Rotating album art animation
    val infiniteTransition = rememberInfiniteTransition(label = "spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "albumSpin"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
    ) {
        // Blurred background glow from album art
        AsyncImage(
            model = song.imageUrl,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .blur(80.dp)
                .alpha(0.25f),
            contentScale = ContentScale.Crop
        )

        // Dark gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            BgDark.copy(alpha = 0.7f),
                            BgDark
                        ),
                        startY = 100f,
                        endY = 700f
                    )
                )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {

            // ── Album Art ─────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer glow ring
                    Box(
                        modifier = Modifier
                            .size(260.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Green.copy(alpha = 0.25f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // Spinning album art
                    AsyncImage(
                        model = song.imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(220.dp)
                            .clip(CircleShape)
                            .rotate(if (isPlaying) rotation else rotation)
                            .border(3.dp, Green.copy(alpha = 0.6f), CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    // Center dot (vinyl hole)
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(BgDark)
                            .border(2.dp, Green.copy(alpha = 0.5f), CircleShape)
                    )
                }
            }

            // ── Song Info + Favorite ───────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = song.title,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = song.artist,
                            fontSize = 14.sp,
                            color = TextSecondary,
                            maxLines = 1
                        )
                    }

                    IconButton(onClick = { isFavorited = !isFavorited }) {
                        Icon(
                            imageVector = if (isFavorited) Icons.Filled.Favorite
                            else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorited) Green else TextSecondary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            // ── Seek Slider ────────────────────────────────────
            item {
                EnhancedPlayerSlider(player)
            }

            // ── Controls ───────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Shuffle
                    IconButton(onClick = {}) {
                        Icon(
                            Icons.Rounded.Shuffle,
                            contentDescription = "Shuffle",
                            tint = TextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Previous
                    IconButton(
                        onClick = {
                            val prev = viewModel.getPreviousSong()
                            if (prev != null) viewModel.selectSong(prev)
                        },
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icon(
                            Icons.Rounded.SkipPrevious,
                            contentDescription = "Previous",
                            tint = TextPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // Play / Pause — main button
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Green)
                            .clickable {
                                if (player.isPlaying) player.pause()
                                else player.play()
                                isPlaying = !isPlaying
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Rounded.Pause
                            else Icons.Rounded.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color.Black,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    // Next
                    IconButton(
                        onClick = {
                            val next = viewModel.getNextSong()
                            if (next != null) viewModel.selectSong(next)
                        },
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icon(
                            Icons.Rounded.SkipNext,
                            contentDescription = "Next",
                            tint = TextPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // Repeat
                    IconButton(onClick = {}) {
                        Icon(
                            Icons.Rounded.Repeat,
                            contentDescription = "Repeat",
                            tint = TextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // ── Volume Slider ──────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.VolumeDown,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    var volume by remember { mutableStateOf(0.8f) }
                    Slider(
                        value = volume,
                        onValueChange = {
                            volume = it
                            player.volume = it
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color.White,
                            inactiveTrackColor = SurfaceLight
                        )
                    )
                    Icon(
                        Icons.Rounded.VolumeUp,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // ── Divider ────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = SurfaceLight
                    )
                    Text(
                        "  Recommended Songs  ",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = SurfaceLight
                    )
                }
            }

            // ── Recommended Songs ──────────────────────────────
            items(viewModel.recommendedSongs) { rec ->
                val isCurrentSong = viewModel.currentSong?.songid == rec.songid

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isCurrentSong) Green.copy(alpha = 0.08f)
                            else Color.Transparent
                        )
                        .clickable {
                            viewModel.selectSong(rec)
                            val mediaItem = MediaItem.fromUri(rec.url)
                            player.setMediaItem(mediaItem)
                            player.prepare()
                            player.play()
                        }
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Thumbnail
                    AsyncImage(
                        model = rec.imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            rec.title,
                            color = if (isCurrentSong) Green else TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            rec.artist,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }

                    if (isCurrentSong) {
                        // Playing indicator bars
                        PlayingIndicator()
                    } else {
                        Icon(
                            Icons.Rounded.PlayArrow,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

// ── Enhanced Seek Slider ──────────────────────────────────────
@Composable
fun EnhancedPlayerSlider(player: ExoPlayer) {
    var position by remember { mutableStateOf(0f) }
    val duration = player.duration.coerceAtLeast(1)

    LaunchedEffect(player) {
        while (true) {
            position = player.currentPosition.toFloat()
            delay(500)
        }
    }

    Column(modifier = Modifier.padding(horizontal = 28.dp)) {
        Slider(
            value = position,
            onValueChange = {
                position = it
                player.seekTo(it.toLong())
            },
            valueRange = 0f..duration.toFloat(),
            colors = SliderDefaults.colors(
                thumbColor = Green,
                activeTrackColor = Green,
                inactiveTrackColor = SurfaceLight
            )
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(formatMs(position.toLong()), color = TextSecondary, fontSize = 11.sp)
            Text(formatMs(duration), color = TextSecondary, fontSize = 11.sp)
        }
    }
}

// ── Animated playing indicator ────────────────────────────────
@Composable
fun PlayingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "bars")
    val bar1 by infiniteTransition.animateFloat(
        initialValue = 4f, targetValue = 16f,
        animationSpec = infiniteRepeatable(tween(400), RepeatMode.Reverse),
        label = "b1"
    )
    val bar2 by infiniteTransition.animateFloat(
        initialValue = 12f, targetValue = 4f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
        label = "b2"
    )
    val bar3 by infiniteTransition.animateFloat(
        initialValue = 8f, targetValue = 20f,
        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
        label = "b3"
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.height(20.dp)
    ) {
        listOf(bar1, bar2, bar3).forEach { h ->
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(h.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Green)
            )
        }
    }
}

// ── Time formatter ────────────────────────────────────────────
fun formatMs(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return "%d:%02d".format(min, sec)
}

