package org.gmautostop.hitchlog.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import org.gmautostop.hitchlog.R
import org.gmautostop.hitchlog.RecordViewModel

@Composable
fun EditRecordScreen(
    viewModel: RecordViewModel = hiltViewModel(),
    finish: () -> Unit
) {
    val hitchLogRecord by viewModel.record

    val date = viewModel.date.observeAsState("")

    val time = viewModel.time.observeAsState("")

    Column {
        Text(text = stringResource(id = hitchLogRecord.type.text))

        TextField(
            value = date.value,
            onValueChange = { viewModel.updateDate(it) },
            label = { Text(stringResource(R.string.date))},
            singleLine = true
        )
        TextField(
            value = time.value,
            onValueChange = { viewModel.updateTime(it) },
            label = { Text(stringResource(R.string.time))},
            singleLine = true
        )
        TextField(
            value = hitchLogRecord.text,
            onValueChange = { viewModel.updateText(it) },
            label = { Text(stringResource(R.string.text))}
        )

        Row {
            Button(onClick = {
                viewModel.save()
                finish()
            }) {
                Text(stringResource(android.R.string.ok))
            }

            if (hitchLogRecord.id.isNotEmpty()) {
                Button(onClick = {
                    viewModel.delete()
                    finish()
                }) {
                    Text(stringResource(R.string.delete))
                }
            }
        }
    }

}