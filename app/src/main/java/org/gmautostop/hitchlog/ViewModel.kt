package org.gmautostop.hitchlog

import android.annotation.SuppressLint
import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavDirections
import dagger.hilt.android.lifecycle.HiltViewModel
import me.tatarka.bindingcollectionadapter2.ItemBinding
import org.gmautostop.hitchlog.util.SingleLiveEvent
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

open class NavViewModel(val repository: FirestoreRepository): ViewModel() {
    val navigationCommands: SingleLiveEvent<NavDirections> = SingleLiveEvent()

    fun navigate(directions: NavDirections) {
        navigationCommands.postValue(directions)
    }
}

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
class HitchLogOldViewModel @Inject constructor(
    private val application: Application,
    repository: FirestoreRepository) : NavViewModel(repository) {

    var logId :String?
        get() = repository.currentLogId
        set(value) { repository.currentLogId = value }

    val log: HitchLog?
        get() = repository.currentLog

    fun records() = repository.recordsLiveData

    fun goToAddFragment(type: HitchLogRecordType) {
        navigate(HitchLogFragmentDirections.actionHitchlogToEditItem(recordType = type))
    }

    fun goToEditFragment(record: HitchLogRecord) {
        navigate(HitchLogFragmentDirections.actionHitchlogToEditItem(record = record))
    }

    @SuppressLint("SimpleDateFormat")
    private val timeFormat = SimpleDateFormat("HH:mm")

    val recordTypes = HitchLogRecordType.values().asList()

    val recordBinding = ItemBinding.of<HitchLogRecord> { itemBinding, _, item ->
        itemBinding.set(BR.record, R.layout.log_record_item)
            .bindExtra(BR.type, application.resources.getString(item.type.text))
            .bindExtra(BR.time, timeFormat.format(item.time))
            .bindExtra(BR.text, item.text)
            .bindExtra(BR.viewModel, this)
    }

    val buttonBinding = ItemBinding.of<HitchLogRecordType> { itemBinding, _, item ->
        itemBinding.set(BR.recordType, R.layout.record_type_button)
            .bindExtra(BR.text, application.resources.getString(item.text))
            .bindExtra(BR.viewModel, this)
    }
}

@HiltViewModel
class RecordViewModel @Inject constructor(val repository: FirestoreRepository):ViewModel() {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy")
    val timeFormat = SimpleDateFormat("HH:mm")
    private val dateTimeFormat = SimpleDateFormat("dd.MM.yyyy HH:mm")

    var record = mutableStateOf(HitchLogRecord())
        private set

    fun initialize (recordId: String) {
        record.value = repository.recordsLiveData.value?.find {
            it.id == recordId
        } ?: HitchLogRecord()

    }

    fun initialize(type: HitchLogRecordType) {
        record.value = HitchLogRecord(type = type)
    }

    fun updateDate(date: String) {
        val currentTime = timeFormat.format(record.value.time)
        record.value = record.value.copy(time = dateTimeFormat.parse("$date $currentTime") ?: record.value.time) //todo error
    }

    fun updateTime(time: String) {
        val currentDate = dateFormat.format(record.value.time)

        record.value = record.value.copy(time = dateTimeFormat.parse("$currentDate $time") ?: Date()) //todo error
    }

    fun updateText(text:String) {
        record.value = record.value.copy(text = text)
    }

    fun save() = repository.saveRecord(record.value)

    fun delete() = repository.deleteRecord(record.value)
}


@SuppressLint("SimpleDateFormat")
@HiltViewModel
class RecordOldViewModel @Inject constructor(repository: FirestoreRepository): NavViewModel(repository){
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

    fun save(item: HitchLogRecord) = repository.saveRecord(item)

    fun delete(item: HitchLogRecord) = repository.deleteRecord(item)

    fun saveRecordAndGoBack(record: HitchLogRecord) {
        save(record)
        navigate(EditRecordFragmentDirections.actionFinishEditingRecord())
    }

    fun deleteRecordAndGoBack(record: HitchLogRecord) {
        //todo confirm
        delete(record)
        navigate(EditRecordFragmentDirections.actionFinishEditingRecord())
    }
}

@HiltViewModel
class AuthViewModel @Inject constructor(repository: FirestoreRepository): NavViewModel(repository) {
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
class LogListOldViewModel @Inject constructor(repository: FirestoreRepository): NavViewModel(repository) {
    init {
        repository.user.value?.uid?.let {
            repository.getUserLogs(it)
        }
    }

    fun logs() = repository.userLogsLiveData

    val logBinding = ItemBinding.of<HitchLog> { itemBinding, _, item ->
        itemBinding.set(BR.log, R.layout.logs_list_item)
            .bindExtra(BR.text, item.name)
            .bindExtra(BR.viewModel, this)
    }

    fun goToAddFragment() {
        navigate(LogListFragmentDirections.actionLogListFragmentToEditLogFragment())
    }

    fun goToEditFragment(log: HitchLog) {
        navigate(LogListFragmentDirections.actionLogListFragmentToEditLogFragment(logId = log.id, logName = log.name))
    }

    fun openLog(log: HitchLog) {
        navigate(LogListFragmentDirections.actionLogListFragmentToHitchlogFragment(log.id))
    }
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

@HiltViewModel
class EditLogOldViewModel @Inject constructor(repository: FirestoreRepository): NavViewModel(repository) {
    lateinit var id: String
    lateinit var name: String

    fun initialize(logId:String) {
        id = logId

    }

    fun saveLog() {
        when {
            id.isEmpty() -> {
                repository.user.value?.uid?.let {
                    repository.addLog(HitchLog(name = this.name, userId = it))
                }
            }
            else -> repository.updateLogName(id = id, name = name)
        }
    }

    fun deleteLog() {
        repository.deleteLog(id)
    }

    fun goBack() {
        navigate(EditLogFragmentDirections.actionFinishEditingLog())
    }

    fun saveLogAndGoBack() {
        saveLog()
        goBack()
    }

    fun deleteLogAndGoBack() {
        deleteLog()
        goBack()
    }
}
