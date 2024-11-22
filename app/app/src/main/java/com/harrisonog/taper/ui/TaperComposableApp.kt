package com.harrisonog.taper.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.harrisonog.taper.R
import com.harrisonog.taper.data.MainViewModel
import com.harrisonog.taper.ui.components.TaperAppBar

enum class TaperScreen(@StringRes val title: Int) {
    Main(title = R.string.main_screen_id),
    Edit(title = R.string.edit_habit_screen_id),
    Settings(title = R.string.settings_screen_id)
}

@Composable
fun TaperComposableApp(
    viewModel: MainViewModel,
    navController : NavHostController = rememberNavController()
) {

    // Get current back stack entry
    val backStackEntry by navController.currentBackStackEntryAsState()

    // Get the name of the Current screen
    val currentScreen = TaperScreen.valueOf(
        backStackEntry?.destination?.route ?: TaperScreen.Main.name
    )

    Scaffold(
        topBar = { TaperAppBar() }
    ) { innerPadding ->
//        val uiState by viewModel

        NavHost(
            navController = navController,
            startDestination = TaperScreen.Main.name,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
        ) {
            composable(route = TaperScreen.Main.name) {
                val context = LocalContext.current
                
            }
        }
    }
}