package com.example.intelligentmusicapp.ui.theme

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class AudDService {

    suspend fun recognize(audioBytes: ByteArray): Pair<String, String>? {

        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("api_token", AudDConfig.API_KEY)
            .addFormDataPart("return", "apple_music,spotify")
            .addFormDataPart(
                "file",
                "sample.pcm",
                audioBytes.toRequestBody("application/octet-stream".toMediaType())
            )
            .build()

        val request = Request.Builder()
            .url(AudDConfig.ENDPOINT)
            .post(body)
            .build()

        val response = OkHttpClient().newCall(request).execute()
        val json = JSONObject(response.body!!.string())

        val result = json.optJSONObject("result") ?: return null

        val title = result.optString("title")
        val artist = result.optString("artist")

        return title to artist
    }
}
