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
import org.gmautostop.hitchlog.EditLogViewModel
import org.gmautostop.hitchlog.R

@Composable
fun EditLogScreen(
    logId: String,
    viewModel: EditLogViewModel = hiltViewModel(),
    finish: () -> Unit
) {
    val hitchLog by viewModel.hitchLog

    Column {
        TextField(value = hitchLog.name,
            onValueChange = { viewModel.updateName(it) },
            label = { Text(stringResource(R.string.name))}
        )

        Row {
            Button(onClick = {
                viewModel.saveLog()
                finish()
            }) {
                Text(stringResource(android.R.string.ok))
            }

            if (hitchLog.id.isNotEmpty()) {
                Button(onClick = {
                    viewModel.deleteLog()
                    finish()
                }) {
                    Text(stringResource(R.string.delete))
                }
            }
        }
    }
}