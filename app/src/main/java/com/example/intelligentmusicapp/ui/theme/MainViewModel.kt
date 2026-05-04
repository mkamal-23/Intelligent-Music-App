package com.example.intelligentmusicapp.ui.theme

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.database.*



class MainViewModel: ViewModel() {

    private val _currentScreen : MutableState<Screen> =
        mutableStateOf(Screen.DrawerScreen.Account)

    val currentScreen : MutableState<Screen>
        get() = _currentScreen

    fun setcurrentscreen(screen : Screen) {
        _currentScreen.value = screen
    }

    // 🔥 ===== ADD FROM HERE =====
    // 🔥 ===== END =====
}