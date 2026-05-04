package com.example.intelligentmusicapp.ui.theme


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun CreateRoomScreen(
    viewModel: RoomViewModel = viewModel(),
    onRoomCreated: (String) -> Unit,
    onBack: () -> Unit
) {
    val roomState by viewModel.roomState.collectAsState()
    val clipboard = LocalClipboardManager.current
    var isLoading by remember { mutableStateOf(false) }
    var roomCode by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "🎵 Create a Room",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "Share the room code with friends\nand listen together!",
                fontSize = 15.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            if (roomCode.isNotEmpty()) {
                // Room code display card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Room Code", color = Color.Gray, fontSize = 13.sp)
                        Text(
                            text = roomCode,
                            fontSize = 40.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1DB954), // Spotify green
                            letterSpacing = 8.sp
                        )
                        Text(
                            text = "👥 ${roomState.members} member(s)",
                            color = Color.LightGray,
                            fontSize = 13.sp
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Copy button
                            OutlinedButton(
                                onClick = {
                                    clipboard.setText(AnnotatedString(roomCode))
                                },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFF1DB954)
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp, Color(0xFF1DB954)
                                )
                            ) {
                                Text("📋 Copy Code")
                            }

                            // Go to player
                            Button(
                                onClick = { onRoomCreated(roomCode) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF1DB954)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Start Playing →", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                // Create button
                Button(
                    onClick = {
                        isLoading = true
                        viewModel.createRoom { code ->
                            roomCode = code
                            isLoading = false
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1DB954)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Create Room", color = Color.Black, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            TextButton(onClick = onBack) {
                Text("← Back", color = Color.Gray)
            }

            roomState.error?.let {
                Text(text = it, color = Color.Red, fontSize = 13.sp)
            }
        }
    }
}