package org.gmautostop.hitchlog

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.text.ParseException
import java.text.SimpleDateFormat
import javax.inject.Inject

sealed class ViewState<out T: Any> {
    data object Loading : ViewState<Nothing>()
    class Show<out T: Any>(val value: T) : ViewState<T>()
    class Error(val error: String) : ViewState<Nothing>()
}

data class HitchLogState(
    val log: HitchLog,
    val records: List<HitchLogRecord>
)

@HiltViewModel
class HitchLogViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repository: FirestoreRepository
) : ViewModel() {
    val state = MutableStateFlow<ViewState<HitchLogState>>(ViewState.Loading)

    init {
        val logId = savedStateHandle.get<String>("logId")
            ?: throw IllegalArgumentException("log ID is null")

        viewModelScope.launch {
            repository.getLog(logId).onEach { response ->
                when(response) {
                    is Response.Loading -> state.value = ViewState.Loading
                    is Response.Failure -> state.value = ViewState.Error(response.errorMessage)
                    is Response.Success -> {
                        repository.getLogRecords(logId)
                            .catch {
                                state.value = ViewState.Error(it.message.orEmpty())
                            }.collect { recordResponse ->
                                state.value = ViewState.Show(HitchLogState(response.data, recordResponse.data))
                            }
                        }
                }
            }.distinctUntilChanged().collect()
        }
    }
}


@HiltViewModel
class RecordViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: FirestoreRepository
):ViewModel() {
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy")
    private val timeFormat = SimpleDateFormat("HH:mm")
    private val dateTimeFormat = SimpleDateFormat("dd.MM.yyyy HH:mm")

    private val logId:String

    val state = MutableStateFlow<ViewState<HitchLogRecord>>(ViewState.Loading)

    var record = mutableStateOf(HitchLogRecord())
        private set

    var date = MutableLiveData<String>()
    var time = MutableLiveData<String>()

    init {
        logId = savedStateHandle.get<String>("logId")
            ?: throw IllegalArgumentException("log ID is null")

        val itemId = savedStateHandle.get<String>("itemId")

        val itemType = try {
            HitchLogRecordType.valueOf(savedStateHandle.get<String>("itemType")
                ?: HitchLogRecordType.FREE_TEXT.name)
        } catch (e: IllegalArgumentException) {
            HitchLogRecordType.FREE_TEXT
        }

        if (itemId.isNullOrEmpty()) {
            record.value = HitchLogRecord(type = itemType).also {
                date.value = dateFormat.format(it.time)
                time.value = timeFormat.format(it.time)
            }
            state.value = ViewState.Show(record.value)
        } else {
            viewModelScope.launch {
                repository.getRecord(logId, itemId).distinctUntilChanged().collect { response ->
                    when (response) {
                        is Response.Loading<*> -> state.value = ViewState.Loading
                        is Response.Success<HitchLogRecord> -> {
                            response.data.let {
                                state.value = ViewState.Show(it)
                                record.value = it
                                date.value = dateFormat.format(it.time)
                                time.value = timeFormat.format(it.time)
                            }
                        }
                        is Response.Failure<*> -> state.value = ViewState.Error(response.errorMessage)
                    }
                }
            }
        }
    }

    fun updateDate(date: String) {
        this.date.value = date
    }

    fun updateTime(time: String) {
        this.time.value = time
    }

    private fun saveDate() {
        try {
            dateTimeFormat.parse("${date.value} ${time.value}")?.let {
                record.value = record.value.copy(time = it)
            }
        } catch (e: ParseException) {}
    }

    fun updateText(text:String) {
        record.value = record.value.copy(text = text)
    }

    fun save() {
        saveDate()
        repository.saveRecord(logId, record.value).launchIn(viewModelScope)
    }

    fun delete() = repository.deleteRecord(logId, record.value).launchIn(viewModelScope)
}

@HiltViewModel
class AuthViewModel @Inject constructor(private val repository: FirestoreRepository): ViewModel() {
    val signedIn
        get() = repository.user != null

    val userName
        get() = repository.user?.displayName
}

@HiltViewModel
class LogListViewModel @Inject constructor(repository: FirestoreRepository): ViewModel() {
    val state = MutableStateFlow<ViewState<List<HitchLog>>>(ViewState.Loading)

    init {
        viewModelScope.launch {
            state.value = ViewState.Loading

            repository.getUserLogs().distinctUntilChanged()
                .catch {
                    state.value = ViewState.Error(it.message.orEmpty())
                }
                .collect { response ->
                    state.value = ViewState.Show(response.data)
                }
            }
        }
    }


@HiltViewModel
class EditLogViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: FirestoreRepository
): ViewModel() {
    val state = MutableStateFlow<ViewState<HitchLog>>(ViewState.Loading)
    private var name: String = ""

    init {
        val logId = savedStateHandle.get<String>("logId")

        viewModelScope.launch {
            val userId = repository.user?.uid
            when {
                userId == null -> state.value = ViewState.Error("Not logged in!")
                logId.isNullOrEmpty() -> state.value = ViewState.Show(HitchLog(userId = userId))
                else -> repository.getLog(logId).distinctUntilChanged().collect { response ->
                    state.value = when (response) {
                        is Response.Loading -> ViewState.Loading
                        is Response.Success -> ViewState.Show(response.data).also {
                            name = response.data.name
                        }
                        is Response.Failure -> ViewState.Error(response.errorMessage)
                    }
                }
            }
        }
    }

    private fun withLog(action: (HitchLog) -> Unit) {
        val log = (state.value as? ViewState.Show<HitchLog>)?.value
        log?.let { action(it) }
    }

    fun updateName(value: String) {
        withLog {
            name = value
            state.value = ViewState.Show(it.copy(name = value))
        }
    }

    fun saveLog() {
        withLog { log ->
            when {
                log.id.isEmpty() ->
                    repository.user?.uid?.let {
                        repository.addLog(log.copy(name = name)).launchIn(viewModelScope)
                    }
                else -> repository.updateLog(log.copy(name = name)).launchIn(viewModelScope)
            }
        }
    }

    fun deleteLog() {
        withLog {
            repository.deleteLog(it.id).launchIn(viewModelScope)
        }
     }
}

