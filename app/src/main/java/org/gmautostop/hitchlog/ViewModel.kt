package org.gmautostop.hitchlog

import android.annotation.SuppressLint
import android.app.Application
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.NavDirections
import me.tatarka.bindingcollectionadapter2.ItemBinding
import org.gmautostop.hitchlog.util.SingleLiveEvent
import java.text.SimpleDateFormat
import java.util.*

open class NavViewModel(val repository: FirestoreRepository): ViewModel() {
    val navigationCommands: SingleLiveEvent<NavDirections> = SingleLiveEvent()

    fun navigate(directions: NavDirections) {
        navigationCommands.postValue(directions)
    }
}

class HitchLogViewModel @ViewModelInject constructor(
    private val application: Application,
    repository: FirestoreRepository,
    @Assisted private val savedStateHandle: SavedStateHandle) : NavViewModel(repository) {

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

    private val timeFormat = SimpleDateFormat("hh:mm")

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

@SuppressLint("SimpleDateFormat")
class RecordViewModel @ViewModelInject constructor(repository: FirestoreRepository,
                                                   @Assisted private val savedStateHandle: SavedStateHandle): NavViewModel(repository){
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy")
    private val timeFormat = SimpleDateFormat("hh:mm")
    private val dateTimeFormat = SimpleDateFormat("dd.MM.yyyy hh:mm")

    lateinit var type: HitchLogRecordType
    lateinit var id : String
    lateinit var date : String
    lateinit var time : String
    lateinit var text : String

    var existingRecord: Boolean = false

    fun initialize(record: HitchLogRecord) {
        reset()
        existingRecord = true
        type = record.type
        id = record.id
        date = dateFormat.format(record.time)
        time = timeFormat.format(record.time)
        text = record.text
    }

    fun initialize(type: HitchLogRecordType) {
        reset()
        existingRecord = false
        this.type = type
    }

    fun reset() {
        id = ""
        date = dateFormat.format(Date())
        time = timeFormat.format(Date())
        text = ""
    }

    fun getRecord() : HitchLogRecord = HitchLogRecord(id, dateTimeFormat.parse("$date $time")!!, type, text)

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

class AuthViewModel @ViewModelInject constructor(repository: FirestoreRepository): NavViewModel(repository) {
    val signedIn
        get() = repository.user.value != null

    val userName
        get() = repository.user.value?.displayName
}

class LogListViewModel @ViewModelInject constructor(repository: FirestoreRepository): NavViewModel(repository) {
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

class EditLogViewModel @ViewModelInject constructor(repository: FirestoreRepository): NavViewModel(repository) {
    lateinit var id: String
    lateinit var name: String

    fun saveLogAndGoBack() {
        when {
            id.isEmpty() -> {
                repository.user.value?.uid?.let {
                    repository.addLog(HitchLog(name = this.name, userId = it))
                }
            }
            else -> repository.updateLogName(id = id, name = name)
        }
        navigate(EditLogFragmentDirections.actionFinishEditingLog())
    }

    fun deleteLogAndGoBack() {
        repository.deleteLog(id)
        navigate(EditLogFragmentDirections.actionFinishEditingLog())
    }
}
