package com.harrisonog.taperAndroid.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.harrisonog.taperAndroid.data.TaperRepository
import com.harrisonog.taperAndroid.ui.HabitDetailState
import com.harrisonog.taperAndroid.ui.HabitDetailViewModel
import com.harrisonog.taperAndroid.ui.HabitEditorViewModel
import com.harrisonog.taperAndroid.ui.HabitListViewModel
import com.harrisonog.taperAndroid.ui.screens.HabitDetailScreen
import com.harrisonog.taperAndroid.ui.screens.HabitEditorScreen
import com.harrisonog.taperAndroid.ui.screens.HabitListScreen

@Composable
fun TaperNavHost(
    repository: TaperRepository,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = HabitListRoute,
        modifier = modifier,
    ) {
        composable(HabitListRoute) {
            val viewModel: HabitListViewModel =
                viewModel(
                    factory = habitListViewModelFactory(repository),
                )
            val state by viewModel.uiState.collectAsState()

            HabitListScreen(
                state = state,
                onAdd = { navController.navigate(HabitEditorRoute) },
                onOpen = { habitId -> navController.navigate("$HabitDetailRoute/$habitId") },
                onDelete = { habit -> viewModel.deleteHabit(habit) },
            )
        }

        composable(HabitEditorRoute) {
            val viewModel: HabitEditorViewModel =
                viewModel(
                    factory = habitEditorViewModelFactory(repository),
                )

            HabitEditorScreen(
                onSave = { draft, _ ->
                    viewModel.create(
                        name = draft.name,
                        description = draft.description,
                        message = draft.message,
                        startPerDay = draft.startPerDay,
                        endPerDay = draft.endPerDay,
                        weeks = draft.weeks,
                        isGoodHabit = draft.isGoodHabit,
                        startDate = draft.startDate,
                    ) {
                        navController.popBackStack()
                    }
                },
                onCancel = { navController.popBackStack() },
            )
        }

        composable(
            route = "$HabitDetailRoute/{habitId}",
            arguments = listOf(navArgument("habitId") { type = NavType.LongType }),
        ) { entry ->
            val habitId = entry.arguments?.getLong("habitId") ?: return@composable
            val viewModel: HabitDetailViewModel =
                viewModel(
                    factory = habitDetailViewModelFactory(repository, habitId),
                )
            val state by viewModel.ui.collectAsState()

            HabitDetailScreen(
                state = state as HabitDetailState,
                onBack = { navController.popBackStack() },
                onDelete = { habit ->
                    viewModel.deleteHabit(habit).invokeOnCompletion {
                        navController.popBackStack()
                    }
                },
            )
        }
    }
}

private const val HabitListRoute = "habitList"
private const val HabitEditorRoute = "habitEditor"
private const val HabitDetailRoute = "habitDetail"

private fun habitListViewModelFactory(repository: TaperRepository) = simpleFactory { HabitListViewModel(repository) }

private fun habitEditorViewModelFactory(repository: TaperRepository) =
    simpleFactory { HabitEditorViewModel(repository) }

private fun habitDetailViewModelFactory(
    repository: TaperRepository,
    habitId: Long,
) = simpleFactory { HabitDetailViewModel(repository, habitId) }

private fun <T : ViewModel> simpleFactory(create: () -> T): ViewModelProvider.Factory =
    object : ViewModelProvider.Factory {
        override fun <VM : ViewModel> create(modelClass: Class<VM>): VM {
            @Suppress("UNCHECKED_CAST")
            return create() as VM
        }
    }
