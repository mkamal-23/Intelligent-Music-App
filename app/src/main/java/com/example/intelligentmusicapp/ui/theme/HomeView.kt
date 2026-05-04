package com.example.intelligentmusicapp.ui.theme

import android.R.attr.value
import android.graphics.drawable.Drawable
import androidx.compose.material3.Text
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.intelligentmusicapp.R
import kotlin.math.absoluteValue

@Composable
fun Home() {
    val categories = listOf("Hits", "Motivational", "Workout", "Love", "Rock", "Yoga")
    val grouped = listOf<String>( "Trending").groupBy{it[0]}
    LazyColumn {
        grouped.forEach { (key, value) ->
            stickyHeader {
                Text(text = value[0], modifier = Modifier.padding(16.dp))
                LazyRow {
                    items(categories) { cat ->
                        BrowserItem(cat = cat, drawable = R.drawable.trend)
                    }
                }
            }
        }

    }

}

@Composable
fun BrowserItem(cat : String, drawable: Int) {
    Card(
        modifier = Modifier.padding(16.dp).size(200.dp),
        border = BorderStroke(3.dp, color = Color.DarkGray)
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Text(text = cat)
            Image(
                painter = painterResource(id = drawable),
                contentDescription = cat,
            )
        }
    }
}