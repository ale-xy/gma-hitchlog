package org.gmautostop.hitchlog.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.gmautostop.hitchlog.LogListViewModel
import org.gmautostop.hitchlog.R

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@ExperimentalMaterial3Api
@Composable
fun LogListScreen(
    viewModel: LogListViewModel,
    openLog: (id: String) -> Unit,
    createLog: () -> Unit) {

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { createLog() }) {
                Icon(Icons.Filled.Add, stringResource(id = R.string.create))
            }
        }) {
            val list = viewModel.logs().value

            if(list.isNullOrEmpty()) {
                Box(Modifier.fillMaxSize()) {
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        text = stringResource(id = R.string.no_logs)
                    )
                }
            } else {
                Text(text = stringResource(id = R.string.my_logs))

                LazyColumn {
                    items(list) { item ->
                        Text(text = item.name ?: "",
                            modifier = Modifier.clickable {
                                openLog(item.id)
                            })
                        }
                    }
                }
            }
}
