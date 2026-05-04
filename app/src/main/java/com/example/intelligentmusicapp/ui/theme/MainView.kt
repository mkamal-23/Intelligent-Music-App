package com.example.intelligentmusicapp.ui.theme

// Core
import android.util.Log
import kotlinx.coroutines.launch

// Compose runtime
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Layout
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

// Icons
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Add

// Material 3
import androidx.compose.material3.*
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Icon
import androidx.compose.material3.Text

// Navigation
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

// ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.filled.Chat
// App resources
import com.example.intelligentmusicapp.R

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.auth.FirebaseAuth
import com.example.intelligentmusicapp.ui.theme.ChatScreen


@androidx.annotation.RequiresPermission(android.Manifest.permission.RECORD_AUDIO)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainView() {
    var showInvalidApiDialog by remember { mutableStateOf(false) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val viewModel: MainViewModel = viewModel()

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val currentScreen = viewModel.currentScreen.value
    val title = remember { mutableStateOf(currentScreen.title) }
    val dialogOpen = remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var showChat by remember { mutableStateOf(false) }
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
    val recognitionViewModel: MusicRecognitionViewModel = viewModel()
    val recognitionState by recognitionViewModel.state.collectAsState()
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            recognitionViewModel.startRecognition(context)
        }
    }
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                LazyColumn(modifier = Modifier.padding(16.dp)) {
                    items(screensInDrawer) { item ->
                        DrawerItem(
                            selected = currentRoute == item.dRoute,
                            item = item
                        ) {
                            scope.launch { drawerState.close() }

                            if (item.dRoute == "add_account") {
                                dialogOpen.value = true
                            } else {
                                navController.navigate(item.dRoute)
                                title.value = item.dtitle
                            }
                        }
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(title.value)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Default.AccountCircle, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            recognitionViewModel.startRecognition(context)
                        }) {
                            Icon(Icons.Filled.Mic, contentDescription = "Mic")
                        }

                        IconButton(onClick = { showChat = true }) {
                            Icon(Icons.Default.ChatBubble, contentDescription = "Chat")
                        }

                        IconButton(onClick = { navController.navigate("room_entry") }) {
                            Icon(Icons.Default.Add, contentDescription = "Room")
                        }
                        IconButton(onClick = { showSheet = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More")
                        }
                    }

                )
            },
            bottomBar = {
                if (currentScreen is Screen.DrawerScreen ||
                    currentScreen == Screen.BottomScreen.Home
                ) {
                    NavigationBar {
                        screensInBottom.forEach { item ->
                            NavigationBarItem(
                                selected = currentRoute == item.bRoute,
                                onClick = {
                                    navController.navigate(item.bRoute)
                                    title.value = item.btitle
                                },
                                icon = {
                                    Icon(
                                        painterResource(id = item.icon),
                                        contentDescription = item.btitle
                                    )
                                },
                                label = { Text(item.btitle) }
                            )
                        }
                    }
                }
            }
        ) { padding ->
            Navigation(
                navController = navController,
                viewModel = viewModel,
                pd = padding
            )

            AccountDialog(dialogOpen = dialogOpen)
        }
        when (val state = recognitionState) {
            is RecognitionState.Listening -> {
                AlertDialog(
                    onDismissRequest = {},
                    title = { Text("🎵 Listening...") },
                    text = { Text("Hold phone near the music") },
                    confirmButton = {}
                )
            }
            is RecognitionState.Processing -> {
                AlertDialog(
                    onDismissRequest = {},
                    title = { Text("🔍 Identifying...") },
                    text = { CircularProgressIndicator() },
                    confirmButton = {}
                )
            }
            is RecognitionState.Success -> {
                AlertDialog(
                    onDismissRequest = { recognitionViewModel.reset() },
                    title = { Text("✅ Found!") },
                    text = { Text("${state.title}\n${state.artist}") },
                    confirmButton = {
                        TextButton(onClick = { recognitionViewModel.reset() }) {
                            Text("OK")
                        }
                    }
                )
            }
            is RecognitionState.NotFound -> {
                AlertDialog(
                    onDismissRequest = { recognitionViewModel.reset() },
                    title = { Text("❌ Not Found") },
                    text = { Text("Song could not be identified") },
                    confirmButton = {
                        TextButton(onClick = { recognitionViewModel.reset() }) {
                            Text("OK")
                        }
                    }
                )
            }
            else -> {}
        }
        if (showChat) {
            ModalBottomSheet(
                onDismissRequest = { showChat = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                ChatScreen(userId = userId)
            }
        }
        if (showSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSheet = false },
                sheetState = sheetState
            ) {
                MoreBottomSheet()
            }
        }
        if (showInvalidApiDialog) {
            AlertDialog(
                onDismissRequest = { showInvalidApiDialog = false },
                title = { Text("Error") },
                text = { Text("Invalid API key") },
                confirmButton = {
                    TextButton(onClick = { showInvalidApiDialog = false }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}


@Composable
fun DrawerItem(
    selected: Boolean,
    item: Screen.DrawerScreen,
    onDrawerItemClicked: () -> Unit
) {
    val background =
        if (selected) MaterialTheme.colorScheme.surfaceVariant
        else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(background)
            .clickable { onDrawerItemClicked() }
            .padding(16.dp)
    ) {
        Icon(
            painter = painterResource(id = item.icon),
            contentDescription = item.dtitle
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = item.dtitle,
            style = MaterialTheme.typography.titleMedium
        )
    }
}


@Composable
fun MoreBottomSheet() {
    Column(modifier = Modifier.padding(24.dp)) {

        SheetItem("Settings", R.drawable.baseline_settings_24)
        SheetItem("Share", R.drawable.baseline_share_24)
        SheetItem("Help", R.drawable.baseline_help_24)
    }
}

@Composable
fun SheetItem(text: String, icon: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = text
        )
        Spacer(Modifier.width(16.dp))
        Text(text, style = MaterialTheme.typography.titleMedium)
    }
}


@Composable
fun Navigation(navController: NavController, viewModel: MainViewModel, pd: PaddingValues) {
    val songViewModel: MusicViewModel= viewModel()
    val roomViewModel: RoomViewModel = viewModel()
    NavHost(
        navController = navController as NavHostController,
        startDestination = if (FirebaseAuth.getInstance().currentUser != null)
            Screen.DrawerScreen.Account.route
        else "login",
        Modifier.padding(pd)
    ) {
        composable("login") {
            GoogleSignInScreen(onSignInSuccess = {
                navController.navigate(Screen.DrawerScreen.Account.route) {
                    popUpTo("login") { inclusive = true }
                }
            })
        }
        // baaki sab same
        // These navigation composable are for bottom navigation bar
        composable(Screen.BottomScreen.Home.route) {
            Home()
        }

        composable(Screen.BottomScreen.library.route) {
            Library()
        }

        composable(Screen.BottomScreen.Browse.route) {
            SongListScreen(songViewModel, onPlayClick = {navController.navigate("Player")})
        }

        // These navigation composable are for drawer navigation bar
        composable(Screen.DrawerScreen.Account.route){
            AccountView()
        }

        composable(Screen.DrawerScreen.Subscription.route){
            Subscription()
        }

        composable("Player") {
            PlayerScreen(songViewModel)
        }

        composable("room_entry") {
            // Entry screen: Create ya Join choose karo
            RoomEntryScreen(
                onCreateRoom = { navController.navigate("create_room") },
                onJoinRoom = { navController.navigate("join_room") }
            )
        }

        composable("create_room") {
            CreateRoomScreen(
                viewModel = roomViewModel,
                onRoomCreated = { navController.navigate("room_player") },
                onBack = { navController.popBackStack() }
            )
        }
        composable("join_room") {
            JoinRoomScreen(
                viewModel = roomViewModel,
                onJoined = { navController.navigate("room_player") },
                onBack = { navController.popBackStack() }
            )
        }

        composable("room_player") {
            RoomPlayerScreen(
                viewModel = roomViewModel,
                onLeave = { navController.navigate("home") { popUpTo("home") } }
            )
        }

    }
}