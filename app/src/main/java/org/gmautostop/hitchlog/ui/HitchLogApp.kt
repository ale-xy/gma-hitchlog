package org.gmautostop.hitchlog.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.gmautostop.hitchlog.AuthViewModel
import org.gmautostop.hitchlog.ui.theme.EditLogScreen
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
                        openLog = { navController.navigate("log/{$it}")},//todo
                        createLog = { navController.navigate("editLog")},
                        editLog = { navController.navigate("editLog?logId={$it}")}
                    )
                }

                composable(
                    route = "editLog?logId={logId}",
                    arguments = listOf(navArgument("logId") { nullable = true})
                ) {
                    EditLogScreen(
                      it.arguments?.getString("logId") ?: ""
                    ) { navController.popBackStack() }
                }

                composable("log/{logId}") {

                }
//
//                composable("item/{itemId}") {
//
//                }
            }

        }
    }
}