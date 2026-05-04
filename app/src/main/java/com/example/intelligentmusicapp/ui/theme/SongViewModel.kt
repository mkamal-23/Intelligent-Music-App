package com.example.intelligentmusicapp.ui.theme

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import kotlin.random.Random

class MusicViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    var songs by mutableStateOf<List<Song>>(emptyList())
        private set
    var currentSong by mutableStateOf<Song?>(null)
        private set

    // Top mein add karo
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var songStartTime: Long = 0L
    private var currentlyPlayingSongId: String = ""
    val listeningHistory = mutableStateListOf<String>()
    init {
        fetchSongs()
    }

    private fun fetchSongs() {
        firestore.collection("Songs")
            .get()
            .addOnSuccessListener { result ->
                songs = result.map { doc ->
                    doc.toObject(Song::class.java)
                }
            }
    }
    fun selectSong(song: Song) {
        currentSong = song
        fetchRecommendations()
        listeningHistory.add(song.songid)
        saveHistoryToFirestore(song.songid)
        if (currentlyPlayingSongId.isNotEmpty()) {
            val duration = System.currentTimeMillis() - songStartTime
            saveHistoryToFirestore(currentlyPlayingSongId, duration)
        }

        // Ab naya song set karo
        currentSong = song
        listeningHistory.add(song.songid)
        songStartTime = System.currentTimeMillis()
        currentlyPlayingSongId = song.songid
    }
    override fun onCleared() {
        super.onCleared()
        if (currentlyPlayingSongId.isNotEmpty()) {
            val duration = System.currentTimeMillis() - songStartTime
            saveHistoryToFirestore(currentlyPlayingSongId, duration)
        }
    }
    private fun saveHistoryToFirestore(songId: String) {
        val userId = auth.currentUser?.uid ?: return  // login nahi hai toh skip

        db.collection("Users")
            .document(userId)
            .collection("history")
            .add(
                mapOf(
                    "songId" to songId,
                    "timestamp" to System.currentTimeMillis()
                )
            )
    }
    private fun saveHistoryToFirestore(songId: String, duration: Long) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("Users")
            .document(userId)
            .collection("history")
            .add(
                mapOf(
                    "songId" to songId,
                    "duration" to duration,  // milliseconds mein
                    "timestamp" to System.currentTimeMillis()
                )
            )
    }
    fun fetchRecommendations() {
        val userId = auth.currentUser?.uid ?: return
        val url = "https://your-api-url.com/recommend"  // deploy karne ke baad

        viewModelScope.launch {
            try {
                val client = OkHttpClient()
                val body = """{"userId": "$userId"}"""
                    .toRequestBody("application/json".toMediaType())

                val request = Request.Builder()
                    .url(url)
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                val json = JSONObject(responseBody ?: return@launch)
                val array = json.getJSONArray("recommendations")

                // recommendedSongs update karo
                val songIds = (0 until array.length()).map { array.getString(it) }
                updateRecommendedSongs(songIds)

            } catch (e: Exception) {
                Log.e("Recommend", e.message ?: "Error")
            }
        }
    }

    private fun updateRecommendedSongs(songIds: List<String>) {
        viewModelScope.launch {
            val songs = songIds.mapNotNull { id ->
                db.collection("Songs")
                    .whereEqualTo("songid", id)
                    .get()
                    .await()
                    .documents
                    .firstOrNull()
                    ?.let { doc ->
                        Song(
                            songid = doc.getString("songid") ?: "",
                            title = doc.getString("title") ?: "",
                            artist = doc.getString("artist") ?: "",
                            url = doc.getString("url") ?: "",
                            imageUrl = doc.getString("imageUrl") ?: ""
                        )
                    }
            }
            recommendedSongs.clear()
            recommendedSongs.addAll(songs)
        }
    }
    val recommendedSongs = mutableStateListOf<Song>()
    private fun fetchRecommendedSongs(currentSongId: String) {

        val recommendedIds = getRecommendedSongIdsFromML(currentSongId)

        if (recommendedIds.isEmpty()) return

        firestore.collection("Songs")
            .whereIn("songid", recommendedIds)
            .get()
            .addOnSuccessListener { result ->
                recommendedSongs.clear()
                recommendedSongs.addAll(result.map { it.toObject(Song::class.java) })
            }
    }

    // 🔹 Mock ML output (replace with real model call)
    private fun getRecommendedSongIdsFromML(songId: String): List<String> {
        var apiResponse: String = ""
        testApi { result ->
            apiResponse = result.toString()
        }


        val Lol = apiResponse
            .removePrefix("[")
            .removeSuffix("]")
            .split(",")
            .map { it.trim() }


        return listOf(Random.nextInt(1, 101).toString(),Random.nextInt(1, 101).toString(),Random.nextInt(1, 101).toString())
    }
    // MusicViewModel.kt — existing file mein add karo

    fun getNextSong(): Song? {
        val list = recommendedSongs
        val currentIndex = list.indexOfFirst { it.songid == currentSong?.songid }
        return if (currentIndex != -1 && currentIndex < list.size - 1)
            list[currentIndex + 1] else null
    }

    fun getPreviousSong(): Song? {
        val list = recommendedSongs
        val currentIndex = list.indexOfFirst { it.songid == currentSong?.songid }
        return if (currentIndex > 0) list[currentIndex - 1] else null
    }
}