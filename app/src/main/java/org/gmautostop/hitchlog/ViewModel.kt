package org.gmautostop.hitchlog

import android.annotation.SuppressLint
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class HitchLogViewModel @Inject constructor(private val repository: FirestoreRepository) : ViewModel() {
    lateinit var hitchLog: MutableLiveData<HitchLog>
        private set

    lateinit var records: MutableLiveData<List<HitchLogRecord>>
        private set

    fun initialize(logId: String) {
        hitchLog = repository.getLog(logId)

        repository.currentLogId = logId
        records = repository.getLogRecords(logId)
    }
}
@HiltViewModel
class RecordViewModel @Inject constructor(val repository: FirestoreRepository):ViewModel() {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy")
    val timeFormat = SimpleDateFormat("HH:mm")
    private val dateTimeFormat = SimpleDateFormat("dd.MM.yyyy HH:mm")

    var record = mutableStateOf(HitchLogRecord())
        private set

    var date = MutableLiveData<String>()
    var time = MutableLiveData<String>()

    fun initialize (recordId: String) {
        record.value = (repository.recordsLiveData.value?.find {
            it.id == recordId
        } ?: HitchLogRecord()).also {
            date.value = dateFormat.format(it.time)
            time.value = timeFormat.format(it.time)
        }
    }

    fun initialize(type: HitchLogRecordType) {
        record.value = HitchLogRecord(type = type).also {
            date.value = dateFormat.format(it.time)
            time.value = timeFormat.format(it.time)
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
        repository.saveRecord(record.value)
    }

    fun delete() = repository.deleteRecord(record.value)
}


//todo remove
@SuppressLint("SimpleDateFormat")
@HiltViewModel
class RecordOldViewModel @Inject constructor(val repository: FirestoreRepository): ViewModel(){
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy")
    private val timeFormat = SimpleDateFormat("HH:mm")
    private val dateTimeFormat = SimpleDateFormat("dd.MM.yyyy HH:mm")

    lateinit var type: HitchLogRecordType
    lateinit var id : String
    lateinit var date : String
    lateinit var time : String
    lateinit var text : String

    var existingRecord: HitchLogRecord? = null

    fun initialize(record: HitchLogRecord) {
        reset()
        existingRecord = record
        type = record.type
        id = record.id
        date = dateFormat.format(record.time)
        time = timeFormat.format(record.time)
        text = record.text
    }

    fun initialize(type: HitchLogRecordType) {
        reset()
        this.type = type
    }

    private fun reset() {
        id = ""
        date = dateFormat.format(Date())
        time = timeFormat.format(Date())
        text = ""
    }

    fun getRecord() : HitchLogRecord {
        val enteredTime: Date = dateTimeFormat.parse("$date $time")!!

        val newTime: Date =
            existingRecord?.time?.let {
                when (it.toMinutes()) {
                    enteredTime -> it
                    else -> getNextTime(enteredTime)
                }
            } ?: getNextTime(enteredTime)

        return HitchLogRecord(id, newTime, type, text)
    }

    private fun getNextTime(enteredTime: Date): Date {
        return repository.recordsLiveData.value?.filter {
                    it.time.toMinutes() == enteredTime
                }?.maxByOrNull { it.time }?.time?.addSecond()
            ?: enteredTime
    }

    private fun Date.addSecond(): Date = Date(time + 1000)
}

@HiltViewModel
class AuthViewModel @Inject constructor(val repository: FirestoreRepository): ViewModel() {
    val signedIn
        get() = repository.user.value != null

    val userName
        get() = repository.user.value?.displayName
}

@HiltViewModel
class LogListViewModel @Inject constructor(repository: FirestoreRepository): ViewModel() {
    init {
        repository.user.value?.uid?.let {
            repository.getUserLogs(it)
        }
    }

    val logs = repository.userLogsLiveData
}

@HiltViewModel
class EditLogViewModel @Inject constructor(private val repository: FirestoreRepository): ViewModel() {
    var hitchLog = mutableStateOf(HitchLog())
        private set

    fun initialize(logId: String) {
        val userId = repository.user.value?.uid ?: ""
        hitchLog.value = when {
            logId.isEmpty() -> HitchLog(userId = userId)
            else -> repository.userLogsLiveData.value?.find { it.id == logId } ?: HitchLog(userId = userId)
        }
    }

    fun updateName(value: String) {
        hitchLog.value = hitchLog.value.copy(name = value)
    }

    fun saveLog() {
        when {
            hitchLog.value.id.isEmpty() -> {
                repository.user.value?.uid?.let {
                    repository.addLog(hitchLog.value)
                }
            }
            else -> repository.updateLog(hitchLog.value)
        }
    }

    fun deleteLog() {
        repository.deleteLog(hitchLog.value.id)
    }
}

