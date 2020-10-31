package org.gmautostop.hitchlog

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavDirections
import me.tatarka.bindingcollectionadapter2.ItemBinding
import org.gmautostop.hitchlog.util.SingleLiveEvent
import java.text.SimpleDateFormat
import java.util.*


class HitchLogViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FirestoreRepository()

    var logId :String?
        get() = repository.currentLogId
        set(value) { repository.currentLogId = value }

    var log: Hitchlog?
        get() = repository.currentLog
        set(value) { repository.currentLog = value }

    fun records() = repository.recordsLiveData

    fun save(item: HitchLogRecord) = repository.saveRecord(item)

    val navigationCommands: SingleLiveEvent<NavDirections> = SingleLiveEvent()

    fun navigate(directions: NavDirections) {
        navigationCommands.postValue(directions)
    }

    fun goToAddFragment(type: HitchLogRecordType) {
        navigate(HitchLogFragmentDirections.actionHitchlogToAddItem(type))
    }

    fun saveRecordAndGoBack(record: HitchLogRecord) {
        save(record)
        navigate(AddItemFragmentDirections.actionAddItemToHitchlog())
    }

    private val timeFormat = SimpleDateFormat("hh:mm")

    val recordBinding = ItemBinding.of<HitchLogRecord> { itemBinding, _, item ->
        itemBinding.set(BR.record, R.layout.log_item)
            .bindExtra(BR.type, application.resources.getString(item.type.text))
            .bindExtra(BR.time, timeFormat.format(item.time))
            .bindExtra(BR.text, item.text)
    }
}

class RecordViewModel(): ViewModel() {
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy")
    private val timeFormat = SimpleDateFormat("hh:mm")
    private val dateTimeFormat = SimpleDateFormat("dd.MM.yyyy hh:mm")

    lateinit var type: HitchLogRecordType
    lateinit var id : String
    lateinit var date :String
    lateinit var time :String
    lateinit var text :String

    init {
        reset()
    }

    constructor(record: HitchLogRecord) : this() {
        type = record.type
        id = record.id
        date = dateFormat.format(record.time)
        time = timeFormat.format(record.time)
        text = record.text
    }

    fun reset(){
        id = ""
        date = dateFormat.format(Date())
        time = timeFormat.format(Date())
        text = ""
    }

    fun getRecord() : HitchLogRecord = HitchLogRecord(id, dateTimeFormat.parse("$date $time"), type, text)
}

class ListItemViewModel(val record: HitchLogRecord): ViewModel() {
    private val timeFormat = SimpleDateFormat("hh:mm", Locale.ROOT)

    val time = timeFormat.format(record.time)

}