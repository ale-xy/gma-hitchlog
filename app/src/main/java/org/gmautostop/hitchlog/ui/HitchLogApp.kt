package org.gmautostop.hitchlog.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.gmautostop.hitchlog.AuthViewModel
import org.gmautostop.hitchlog.ui.theme.HitchlogTheme

@ExperimentalMaterial3Api
@Preview
@Composable
fun HitchLogApp() {
    HitchlogTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
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
                    LogListScreen(viewModel = hiltViewModel(),
                        { navController.navigate("log/null")},//todo
                        { navController.navigate("log/null")}
                    )
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