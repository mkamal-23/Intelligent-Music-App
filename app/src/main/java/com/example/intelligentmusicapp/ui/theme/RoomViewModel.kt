package com.example.intelligentmusicapp.ui.theme

// RoomViewModel.kt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class RoomState(
    val roomCode: String = "",
    val isHost: Boolean = false,
    val isPlaying: Boolean = false,
    val currentSongId: String = "",
    val currentSongTitle: String = "",
    val currentSongArtist: String = "",
    val currentSongUrl: String = "",
    val currentSongImage: String = "",
    val seekPosition: Long = 0L,
    val queue: List<String> = emptyList(), // list of songIds
    val members: Int = 1,
    val error: String? = null
)

data class ChatMessage(
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val timestamp: Long = 0L
)

class RoomViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _roomState = MutableStateFlow(RoomState())
    val roomState: StateFlow<RoomState> = _roomState

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages

    private val _allSongs = MutableStateFlow<List<Song>>(emptyList())
    val allSongs: StateFlow<List<Song>> = _allSongs

    private var roomListener: ListenerRegistration? = null
    private var chatListener: ListenerRegistration? = null

    // ── Generate random 6-char room code ──────────────────────────
    private fun generateRoomCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6).map { chars.random() }.joinToString("")
    }

    // ── Fetch all songs from Firestore ─────────────────────────────
    fun fetchAllSongs() {
        viewModelScope.launch {
            try {
                val snapshot = db.collection("Songs").get().await()
                val songs = snapshot.documents.mapNotNull { doc ->
                    Song(
                        songid = doc.getString("songid") ?: return@mapNotNull null,
                        title = doc.getString("title") ?: "",
                        artist = doc.getString("artist") ?: "",
                        url = doc.getString("url") ?: "",
                        imageUrl = doc.getString("imageUrl") ?: ""
                    )
                }
                _allSongs.value = songs
            } catch (e: Exception) {
                _roomState.value = _roomState.value.copy(error = e.message)
            }
        }
    }

    // ── HOST: Create Room ──────────────────────────────────────────
    fun createRoom(onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val code = generateRoomCode()
                val userId = auth.currentUser?.uid ?: "anonymous"

                val roomData = hashMapOf(
                    "hostId" to userId,
                    "roomCode" to code,
                    "isPlaying" to false,
                    "currentSongId" to "",
                    "currentSongUrl" to "",
                    "currentSongTitle" to "",
                    "currentSongArtist" to "",
                    "currentSongImage" to "",
                    "seekPosition" to 0L,
                    "queue" to emptyList<String>(),
                    "members" to 1,
                    "createdAt" to FieldValue.serverTimestamp()
                )

                db.collection("Rooms").document(code).set(roomData).await()

                _roomState.value = _roomState.value.copy(
                    roomCode = code,
                    isHost = true
                )
                listenToRoom(code)
                onSuccess(code)
            } catch (e: Exception) {
                _roomState.value = _roomState.value.copy(error = e.message)
            }
        }
    }

    // ── LISTENER: Join Room ────────────────────────────────────────
    fun joinRoom(code: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val doc = db.collection("Rooms").document(code).get().await()
                if (!doc.exists()) {
                    onError("Room not found! Check the code.")
                    return@launch
                }

                // Increment member count
                db.collection("Rooms").document(code)
                    .update("members", FieldValue.increment(1)).await()

                _roomState.value = _roomState.value.copy(
                    roomCode = code,
                    isHost = false
                )
                listenToRoom(code)
                listenToChat(code)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to join room")
            }
        }
    }

    // ── Real-time Room Listener ────────────────────────────────────
    private fun listenToRoom(code: String) {
        roomListener?.remove()
        roomListener = db.collection("Rooms").document(code)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                _roomState.value = _roomState.value.copy(
                    isPlaying = snapshot.getBoolean("isPlaying") ?: false,
                    currentSongId = snapshot.getString("currentSongId") ?: "",
                    currentSongUrl = snapshot.getString("currentSongUrl") ?: "",
                    currentSongTitle = snapshot.getString("currentSongTitle") ?: "",
                    currentSongArtist = snapshot.getString("currentSongArtist") ?: "",
                    currentSongImage = snapshot.getString("currentSongImage") ?: "",
                    seekPosition = snapshot.getLong("seekPosition") ?: 0L,
                    queue = (snapshot.get("queue") as? List<String>) ?: emptyList(),
                    members = (snapshot.getLong("members") ?: 1L).toInt()
                )
            }

        listenToChat(code)
    }

    // ── HOST CONTROLS ──────────────────────────────────────────────

    fun playSong(song: Song) {
        if (!_roomState.value.isHost) return
        val code = _roomState.value.roomCode
        db.collection("Rooms").document(code).update(
            mapOf(
                "currentSongId" to song.songid,
                "currentSongUrl" to song.url,
                "currentSongTitle" to song.title,
                "currentSongArtist" to song.artist,
                "currentSongImage" to song.imageUrl,
                "isPlaying" to true,
                "seekPosition" to 0L
            )
        )
    }

    fun togglePlayPause() {
        if (!_roomState.value.isHost) return
        val code = _roomState.value.roomCode
        val newState = !_roomState.value.isPlaying
        db.collection("Rooms").document(code).update("isPlaying", newState)
    }

    fun seekTo(position: Long) {
        if (!_roomState.value.isHost) return
        val code = _roomState.value.roomCode
        db.collection("Rooms").document(code).update("seekPosition", position)
    }

    fun playNext() {
        if (!_roomState.value.isHost) return
        val queue = _roomState.value.queue
        val currentId = _roomState.value.currentSongId
        val currentIndex = queue.indexOf(currentId)
        if (currentIndex < queue.size - 1) {
            val nextSongId = queue[currentIndex + 1]
            val nextSong = _allSongs.value.find { it.songid == nextSongId } ?: return
            playSong(nextSong)
        }
    }

    fun playPrev() {
        if (!_roomState.value.isHost) return
        val queue = _roomState.value.queue
        val currentId = _roomState.value.currentSongId
        val currentIndex = queue.indexOf(currentId)
        if (currentIndex > 0) {
            val prevSongId = queue[currentIndex - 1]
            val prevSong = _allSongs.value.find { it.songid == prevSongId } ?: return
            playSong(prevSong)
        }
    }

    fun addToQueue(songId: String) {
        if (!_roomState.value.isHost) return
        val code = _roomState.value.roomCode
        db.collection("Rooms").document(code)
            .update("queue", FieldValue.arrayUnion(songId))
    }

    fun removeFromQueue(songId: String) {
        if (!_roomState.value.isHost) return
        val code = _roomState.value.roomCode
        db.collection("Rooms").document(code)
            .update("queue", FieldValue.arrayRemove(songId))
    }

    // ── CHAT ───────────────────────────────────────────────────────

    fun sendMessage(text: String) {
        val code = _roomState.value.roomCode
        val user = auth.currentUser
        val msg = hashMapOf(
            "senderId" to (user?.uid ?: "anon"),
            "senderName" to (user?.displayName ?: "User"),
            "text" to text,
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("Rooms").document(code)
            .collection("messages").add(msg)
    }

    private fun listenToChat(code: String) {
        chatListener?.remove()
        chatListener = db.collection("Rooms").document(code)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot == null) return@addSnapshotListener
                _chatMessages.value = snapshot.documents.mapNotNull { doc ->
                    ChatMessage(
                        senderId = doc.getString("senderId") ?: "",
                        senderName = doc.getString("senderName") ?: "User",
                        text = doc.getString("text") ?: "",
                        timestamp = doc.getLong("timestamp") ?: 0L
                    )
                }
            }
    }

    // ── Leave / Cleanup ────────────────────────────────────────────

    fun leaveRoom() {
        val code = _roomState.value.roomCode
        if (_roomState.value.isHost) {
            db.collection("Rooms").document(code).delete()
        } else {
            db.collection("Rooms").document(code)
                .update("members", FieldValue.increment(-1))
        }
        roomListener?.remove()
        chatListener?.remove()
        _roomState.value = RoomState()
        _chatMessages.value = emptyList()
    }

    override fun onCleared() {
        super.onCleared()
        roomListener?.remove()
        chatListener?.remove()
    }
}