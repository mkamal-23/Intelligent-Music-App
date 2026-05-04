package com.example.intelligentmusicapp.ui.theme

import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import kotlin.random.Random

fun testApi(onResult: (String?) -> Unit) {
    val x = Random.nextInt(1, 101)
    var y = Random.nextInt(1, 101)
    while (x==y) {
        y = Random.nextInt(1, 101)
    }
    val client = OkHttpClient()

    val url = "https://unlegislative-carylon-vernacularly.ngrok-free.dev/recommend"

    // JSON payload
    val json = JSONObject()
    json.put("s1", x)
    json.put("s2", y)

    val mediaType = "application/json".toMediaType()
    val body = json.toString().toRequestBody(mediaType)

    val request = Request.Builder()
        .url(url)
        .post(body)
        .build()

    client.newCall(request).enqueue(object : okhttp3.Callback {
        override fun onFailure(call: okhttp3.Call, e: IOException) {
            e.printStackTrace()
        }

        override fun onResponse(call: Call, response: Response) {
            val result = response.body?.string()
            onResult(result)
        }
    })

}