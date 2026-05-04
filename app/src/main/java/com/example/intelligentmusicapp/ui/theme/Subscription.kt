package com.example.intelligentmusicapp.ui.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun Subscription(){
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text(text = "Manage Subscription")
        Card(                                                  // an ui box kind of thing with rounded corners
            modifier = Modifier.padding(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)                                      // it uplifts the box to show the shadow
        ){
            Column(
                modifier = Modifier.padding(8.dp)
            ){
                Column(){
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ){
                        Text(text = "Free Tier")
                        TextButton(onClick = {}) {
                            Row(){
                                Text(text = "See all plans")
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "See all plans"
                                )
                            }
                        }
                    }
                }
            }
            Divider(thickness = 1.dp, modifier = Modifier.padding(horizontal =  8.dp))     // a line to divide the information from each other

            Row {
                Icon(imageVector = Icons.Default.AccountBox , contentDescription = "Get a plan")
                Text(text = "Get a plan")
            }
        }
    }
}