package org.gmautostop.hitchlog.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.gmautostop.hitchlog.EditLogViewModel
import org.gmautostop.hitchlog.HitchLog
import org.gmautostop.hitchlog.R
import org.gmautostop.hitchlog.ViewState

@Composable
fun EditLogScreen(
    viewModel: EditLogViewModel = hiltViewModel(),
    finish: () -> Unit
) {
    val state: ViewState<HitchLog> by viewModel.state.collectAsStateWithLifecycle()

    when (state) {
        is ViewState.Loading -> Loading()
        is ViewState.Error -> Error((state as ViewState.Error).error)
        is ViewState.Show<HitchLog> ->
            Log(
                (state as ViewState.Show<HitchLog>).value,
                { name -> viewModel.updateName(name) },
                { viewModel.saveLog() },
                { viewModel.deleteLog() },
                finish
            )
    }
}

@Composable
fun Log(
    log: HitchLog,
    updateName: (String) -> Unit,
    saveLog: () -> Unit,
    deleteLog: () -> Unit,
    finish: () -> Unit
) {
    Column {
        TextField(value = log.name,
            onValueChange = { updateName(it) },
            label = { Text(stringResource(R.string.name))}
        )

        Row {
            Button(onClick = {
                saveLog()
                finish()
            }) {
                Text(stringResource(android.R.string.ok))
            }

            if (log.id.isNotEmpty()) {
                Button(onClick = {
                    deleteLog()
                    finish()
                }) {
                    Text(stringResource(R.string.delete))
                }
            }
        }
    }
}
