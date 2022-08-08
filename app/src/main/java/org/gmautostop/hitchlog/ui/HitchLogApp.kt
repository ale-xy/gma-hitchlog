package org.gmautostop.hitchlog.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.gmautostop.hitchlog.*
import org.gmautostop.hitchlog.ui.theme.HitchlogTheme

@Preview
@Composable
fun HitchLogApp() {
    HitchlogTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            val navController = rememberNavController()
            val authViewModel: AuthViewModel = hiltViewModel()

            val startDestination = when {
                authViewModel.signedIn -> "logList"
                else -> "auth"
            }

            NavHost(navController = navController, startDestination = startDestination) {
                composable("auth") {
                    AuthScreen() { navController.navigate("logList") {
                        popUpTo("auth") { inclusive = true }
                    } }
                }

                composable("logList") {
                    LogListScreen(
                        openLog = { navController.navigate("log/$it") },
                        createLog = { navController.navigate("editLog") },
                        editLog = { navController.navigate("editLog?logId=$it") }
                    )
                }

                composable(
                    route = "editLog?logId={logId}",
                    arguments = listOf(navArgument("logId") { nullable = true})
                ) {
                    val logId = it.arguments?.getString("logId") ?: ""
                    val viewModel: EditLogViewModel = hiltViewModel()
                    viewModel.initialize(logId)

                    EditLogScreen(
                        viewModel = viewModel,
                        logId = logId
                    ) { navController.popBackStack() }
                }

                composable(route= "log/{logId}",
                    arguments = listOf(navArgument("logId") { type = NavType.StringType})
                ) { backStackEntry ->
                    val logId = backStackEntry.arguments?.getString("logId") ?:""
                    val viewModel: HitchLogViewModel = hiltViewModel()
                    viewModel.initialize(logId)

                    HitchLogScreen(logId = logId,
                        viewModel = viewModel,
                        createRecord = { type -> navController.navigate("item?itemType=${type.name}") },
                        editRecord = { id -> navController.navigate("item?itemId=$id") })
                }

                composable(
                    route = "item?itemId={itemId}&itemType={itemType}",
                    arguments = listOf(
                        navArgument("itemId") { nullable = true},
                        navArgument("itemType") { defaultValue = HitchLogRecordType.FREE_TEXT.name }
                    )
                ) {
                    val itemId = it.arguments?.getString("itemId") ?: ""

                    val itemType = try {
                        HitchLogRecordType.valueOf(it.arguments?.getString("itemType") ?: HitchLogRecordType.FREE_TEXT.name)
                    } catch (e: IllegalArgumentException) {
                        HitchLogRecordType.FREE_TEXT
                    }

                    val viewModel: RecordViewModel = hiltViewModel()

                    if (itemId.isNotEmpty()) {
                        viewModel.initialize(itemId)
                    } else {
                        viewModel.initialize(itemType)
                    }

                    EditRecordScreen(
                        viewModel = viewModel,
                    ) { navController.popBackStack() }
                }
            }
        }
    }
}