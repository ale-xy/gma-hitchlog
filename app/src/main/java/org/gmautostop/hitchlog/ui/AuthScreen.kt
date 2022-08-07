package org.gmautostop.hitchlog.ui

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract

@Composable
fun AuthScreen(
    onAuthenticated: () -> Unit
) {
    val firebaseAuthLauncher = rememberLauncherForActivityResult(
        contract = FirebaseAuthUIActivityResultContract()
        ) {
        when (it.resultCode) {
            Activity.RESULT_OK -> onAuthenticated()

            //todo on error
        }
    }

    val intent = AuthUI.getInstance()
        .createSignInIntentBuilder()
        .setAvailableProviders(
            arrayListOf(
                AuthUI.IdpConfig.GoogleBuilder().build(),
                AuthUI.IdpConfig.EmailBuilder().build(),
//                AuthUI.IdpConfig.PhoneBuilder().build(),
            )
        ).build()

    Box(Modifier.fillMaxSize()) {
        Button(modifier = Modifier.align(Alignment.Center),
                onClick = { firebaseAuthLauncher.launch(intent) }) {
            Text("Login")
        }
    }

}