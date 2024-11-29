package org.gmautostop.hitchlog.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import org.gmautostop.hitchlog.AuthViewModel
import org.gmautostop.hitchlog.EditLogViewModel
import org.gmautostop.hitchlog.HitchLogRecordType
import org.gmautostop.hitchlog.HitchLogViewModel
import org.gmautostop.hitchlog.RecordViewModel
import org.gmautostop.hitchlog.ui.theme.HitchlogTheme

@Composable
fun HitchLogApp(navController: NavHostController) {
    HitchlogTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            val authViewModel: AuthViewModel = hiltViewModel()

            val startDestination = when {
                authViewModel.signedIn -> "logList"
                else -> "auth"
            }

            NavHost(navController = navController, startDestination = startDestination) {
                composable("auth") {
                    AuthScreen { navController.navigate("logList") {
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
                    it.arguments?.getString("logId") ?: ""
                    val viewModel: EditLogViewModel = hiltViewModel()

                    EditLogScreen(
                        viewModel = viewModel,
                    ) { navController.popBackStack() }
                }

                composable(route= "log/{logId}",
                    arguments = listOf(navArgument("logId") { type = NavType.StringType})
                ) { backStackEntry ->
                    val logId = backStackEntry.arguments?.getString("logId") ?:""
                    val viewModel: HitchLogViewModel = hiltViewModel()

                    HitchLogScreen(
                        viewModel = viewModel,
                        createRecord = { type -> navController.navigate("item?logId=$logId&itemType=${type.name}") },
                        editRecord = { id -> navController.navigate("item?logId=$logId&itemId=$id") }
                    )
                }

                composable(
                    route = "item?logId={logId}&itemId={itemId}&itemType={itemType}",
                    arguments = listOf(
                        navArgument("logId") { type = NavType.StringType},
                        navArgument("itemId") { nullable = true},
                        navArgument("itemType") { defaultValue = HitchLogRecordType.FREE_TEXT.name }
                    )
                ) {
                    val viewModel: RecordViewModel = hiltViewModel()

                    EditRecordScreen(
                        viewModel = viewModel,
                    ) { navController.popBackStack() }
                }
            }
        }
    }
}


@Composable
fun Loading() {
    Box(Modifier.fillMaxSize()) {
        CircularProgressIndicator(Modifier.align(Alignment.Center))
    }
}


@Composable
fun Error(error: String) {
    Box(Modifier.fillMaxSize()) {
        Text(error, Modifier.align(Alignment.Center))
    }
}