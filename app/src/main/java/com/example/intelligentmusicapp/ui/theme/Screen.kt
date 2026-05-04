package com.example.intelligentmusicapp.ui.theme

import androidx.annotation.DrawableRes
import com.example.intelligentmusicapp.R

sealed class Screen(val title : String, val route : String) {

    // Bottom navigation screens
    sealed class BottomScreen(
        val btitle : String,
        val bRoute : String,
        @DrawableRes val icon : Int) : Screen ( btitle, bRoute ) {
        object Home : BottomScreen(
            btitle = "Home",
            bRoute = "home",
            icon = R.drawable.baseline_music_video_24
        )

        object library : BottomScreen(
            btitle = "Library",
            bRoute = "library",
            icon = R.drawable.baseline_library_music_24
        )

        object Browse : BottomScreen(
            btitle = "Browse",
            bRoute = "browse",
            icon = R.drawable.baseline_apps_24
        )
    }

    // Drawer navigation screens
    sealed class DrawerScreen(
        val dtitle : String,
        val dRoute : String,
        @DrawableRes val icon : Int) : Screen ( dtitle, dRoute )
    {
        object Account : DrawerScreen(
            dtitle = "Account",
            dRoute = "account",
            icon = R.drawable.ic_account
        )
        object Subscription : DrawerScreen(
            dtitle = "Subscription",
            dRoute = "subscription",
            icon = R.drawable.ic_subscription
        )
        object AddAccount : DrawerScreen(
            dtitle = "Add Account",
            dRoute = "add_account",
            icon = R.drawable.baseline_person_add_alt_1_24
        )
    }

    object RoomHost : Screen(
        title = "Room",
        route = "room_host"
    )

}

val screensInBottom = listOf(
    Screen.BottomScreen.Home,
    Screen.BottomScreen.library,
    Screen.BottomScreen.Browse
)

val screensInDrawer = listOf(
    Screen.DrawerScreen.Account,
    Screen.DrawerScreen.Subscription,
    Screen.DrawerScreen.AddAccount
)