package org.gmautostop.hitchlog.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.gmautostop.hitchlog.HitchLog
import org.gmautostop.hitchlog.HitchLogRecordType
import org.gmautostop.hitchlog.HitchLogViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HitchLogScreen(
    logId: String,
    viewModel: HitchLogViewModel = hiltViewModel(),
    createRecord: (type: HitchLogRecordType) -> Unit,
    editRecord: (id: String) -> Unit
) {
    val hitchLog by viewModel.hitchLog.observeAsState(HitchLog())
    val records by viewModel.records.observeAsState(listOf())

    val timeFormat = SimpleDateFormat("HH:mm", Locale.ROOT)

    Column(Modifier.fillMaxSize()) {
        Text(text = hitchLog.name)

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(records) { item ->
                Row (
                    Modifier.clickable { editRecord(item.id) },
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(text = timeFormat.format(item.time))
                    Text(text = stringResource(item.type.text))
                    Text(text = item.text)
                }
            }
        }

        LazyVerticalGrid(columns = GridCells.Fixed(4)) {
            items(HitchLogRecordType.values().asList()) { item ->
                Button(onClick = { createRecord(item) }) {
                    Text(text = stringResource(id = item.text))
                }
            }
        }


    }
}