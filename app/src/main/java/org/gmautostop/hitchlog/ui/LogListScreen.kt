package org.gmautostop.hitchlog.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.gmautostop.hitchlog.LogListViewModel
import org.gmautostop.hitchlog.R

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun LogListScreen(
    viewModel: LogListViewModel = hiltViewModel(),
    openLog: (id: String) -> Unit,
    createLog: () -> Unit,
    editLog: (id: String) -> Unit) {

    val list by viewModel.logs.observeAsState(listOf())

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { createLog() }) {
                Icon(Icons.Filled.Add, stringResource(id = R.string.create))
            }
        }) {
            if(list.isNullOrEmpty()) {
                Box(Modifier.fillMaxSize()) {
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        text = stringResource(id = R.string.no_logs)
                    )
                }
            } else {
                Column {
                    Text(text = stringResource(id = R.string.my_logs))
                    LazyColumn {
                        items(list) { item ->
                            Row(Modifier.clickable { openLog(item.id) }) {
                                Text(text = item.name,
                                    modifier = Modifier
                                        .padding(vertical = Dp(5.0f))
                                )
                                IconButton(onClick = {
                                    editLog(item.id)
                                }) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Edit")
                                }

                            }
                            //todo edit button
                        }
                    }

                }
            }
        }
}
