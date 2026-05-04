package com.example.intelligentmusicapp.ui.theme

import android.Manifest
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

sealed class RecognitionState {
    object Idle : RecognitionState()
    object Listening : RecognitionState()
    object Processing : RecognitionState()
    data class Success(val title: String, val artist: String) : RecognitionState()
    data class NotFound(val message: String = "Song not found") : RecognitionState()
    data class Error(val message: String) : RecognitionState()
}

class MusicRecognitionViewModel : ViewModel() {

    private val _state = MutableStateFlow<RecognitionState>(RecognitionState.Idle)
    val state: StateFlow<RecognitionState> = _state

    private val RAPID_API_KEY = "48e811c004msh163c7f7c887e5a3p1eae54jsn8704f28881e3"

    private var isRecording = false
    @RequiresPermission(
        Manifest.permission.RECORD_AUDIO
    )
    fun startRecognition(context: Context) {
        if (_state.value is RecognitionState.Listening) return

        viewModelScope.launch(Dispatchers.IO) @RequiresPermission(Manifest.permission.RECORD_AUDIO)  {
            try {
                _state.value = RecognitionState.Listening

                // 5 second audio record karo
                val audioData = recordAudio()

                _state.value = RecognitionState.Processing

                // File mein save karo
                val audioFile = File(context.cacheDir, "recognition.wav")
                saveAsWav(audioData, audioFile)

                // API call karo
                recognizeSong(audioFile)

            } catch (e: Exception) {
                _state.value = RecognitionState.Error(e.message ?: "Something went wrong")
            }
        }
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    private fun recordAudio(): ByteArray {
        val sampleRate = 44100
        val bufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        val recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        val outputStream = ByteArrayOutputStream()
        val buffer = ByteArray(bufferSize)

        recorder.startRecording()
        isRecording = true

        val endTime = System.currentTimeMillis() + 5000 // 5 seconds
        while (System.currentTimeMillis() < endTime && isRecording) {
            val read = recorder.read(buffer, 0, bufferSize)
            if (read > 0) outputStream.write(buffer, 0, read)
        }

        recorder.stop()
        recorder.release()
        isRecording = false

        return outputStream.toByteArray()
    }

    private fun saveAsWav(pcmData: ByteArray, file: File) {
        val sampleRate = 44100
        val channels = 1
        val bitsPerSample = 16
        val byteRate = sampleRate * channels * bitsPerSample / 8
        val dataSize = pcmData.size
        val headerSize = 44

        FileOutputStream(file).use { fos ->
            // WAV header
            fos.write("RIFF".toByteArray())
            fos.write(intToBytes(dataSize + headerSize - 8))
            fos.write("WAVE".toByteArray())
            fos.write("fmt ".toByteArray())
            fos.write(intToBytes(16))
            fos.write(shortToBytes(1))
            fos.write(shortToBytes(channels.toShort()))
            fos.write(intToBytes(sampleRate))
            fos.write(intToBytes(byteRate))
            fos.write(shortToBytes((channels * bitsPerSample / 8).toShort()))
            fos.write(shortToBytes(bitsPerSample.toShort()))
            fos.write("data".toByteArray())
            fos.write(intToBytes(dataSize))
            fos.write(pcmData)
        }
    }

    private fun recognizeSong(file: File) {
        val client = OkHttpClient()

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file", "recognition.wav",
                file.readBytes().toRequestBody("audio/wav".toMediaType())
            )
            .build()

        val request = Request.Builder()
            .url("https://shazam-core.p.rapidapi.com/v1/tracks/recognize")
            .post(requestBody)
            .addHeader("x-rapidapi-key", RAPID_API_KEY)
            .addHeader("x-rapidapi-host", "shazam-core.p.rapidapi.com")
            .build()

        val response = client.newCall(request).execute()
        val body = response.body?.string()

        if (body.isNullOrEmpty()) {
            _state.value = RecognitionState.NotFound()
            return
        }

        val json = JSONObject(body)
        val track = json.optJSONObject("track")

        if (track == null) {
            _state.value = RecognitionState.NotFound()
            return
        }

        val title = track.optString("title", "")
        val artist = track.optString("subtitle", "")

        _state.value = RecognitionState.Success(title, artist)
    }

    fun reset() {
        isRecording = false
        _state.value = RecognitionState.Idle
    }

    private fun intToBytes(value: Int): ByteArray {
        return byteArrayOf(
            (value and 0xff).toByte(),
            (value shr 8 and 0xff).toByte(),
            (value shr 16 and 0xff).toByte(),
            (value shr 24 and 0xff).toByte()
        )
    }

    private fun shortToBytes(value: Short): ByteArray {
        return byteArrayOf(
            (value.toInt() and 0xff).toByte(),
            (value.toInt() shr 8 and 0xff).toByte()
        )
    }
}