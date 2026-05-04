package com.example.intelligentmusicapp.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun JoinRoomScreen(
    viewModel: RoomViewModel = viewModel(),
    onJoined: () -> Unit,
    onBack: () -> Unit
) {
    var roomCodeInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text("🎧 Join a Room", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)

            Text(
                "Enter the 6-character room code\nshared by your friend",
                color = Color.Gray,
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )

            OutlinedTextField(
                value = roomCodeInput,
                onValueChange = {
                    if (it.length <= 6) roomCodeInput = it.uppercase()
                    errorMsg = ""
                },
                label = { Text("Room Code", color = Color.Gray) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF1DB954),
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color(0xFF1DB954)
                ),
                modifier = Modifier.fillMaxWidth(),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    letterSpacing = 6.sp
                )
            )

            if (errorMsg.isNotEmpty()) {
                Text(errorMsg, color = Color.Red, fontSize = 13.sp)
            }

            Button(
                onClick = {
                    if (roomCodeInput.length != 6) {
                        errorMsg = "Please enter a valid 6-character code"
                        return@Button
                    }
                    isLoading = true
                    viewModel.joinRoom(
                        code = roomCodeInput,
                        onSuccess = {
                            isLoading = false
                            onJoined()
                        },
                        onError = { err ->
                            isLoading = false
                            errorMsg = err
                        }
                    )
                },
                enabled = !isLoading && roomCodeInput.length == 6,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DB954)),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                } else {
                    Text("Join Room", color = Color.Black, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                }
            }

            TextButton(onClick = onBack) {
                Text("← Back", color = Color.Gray)
            }
        }
    }
}