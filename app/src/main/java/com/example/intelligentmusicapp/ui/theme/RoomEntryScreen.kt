package com.example.intelligentmusicapp.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// RoomEntryScreen.kt
@Composable
fun RoomEntryScreen(onCreateRoom: () -> Unit, onJoinRoom: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0D0D0D)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text("👥 Listen Together", fontSize = 30.sp,
                fontWeight = FontWeight.Bold, color = Color.White)
            Text("Create a room or join a friend's room",
                color = Color.Gray, fontSize = 15.sp)

            Button(
                onClick = onCreateRoom,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DB954)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("🎙 Create Room", color = Color.Black,
                    fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onJoinRoom,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1DB954)),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF1DB954)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("🎧 Join Room", fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}