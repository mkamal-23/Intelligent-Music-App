package com.example.intelligentmusicapp.ui.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.intelligentmusicapp.R

@Composable
fun AccountView() {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Row(
            modifier =  Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Row() {
                Icon(
                    modifier = Modifier.padding(end = 8.dp),
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Account"
                )
                Text(text = "Account")
            }
            Column {
                Text("My Account")
                Text("Manage Account")
            }
        }

        Row(modifier = Modifier.padding(top = 16.dp))
        {
            Icon(
                modifier = Modifier.padding(end = 8.dp),
                painter = painterResource(R.drawable.ic_music),
                contentDescription = "My Music"
            )
            Text(text = "My Music")
        }
    }
}