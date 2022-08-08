package org.gmautostop.hitchlog

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreRepository @Inject constructor(){
    init {
        FirebaseFirestore.setLoggingEnabled(true)
        FirebaseAuth.getInstance().addAuthStateListener {
            user.value = it.currentUser
        }
    }

    private var firestoreDB = FirebaseFirestore.getInstance()

    val user = MutableLiveData<FirebaseUser>(FirebaseAuth.getInstance().currentUser)

    private fun logs() : CollectionReference = firestoreDB.collection("logs")
    private fun logRecords(logId: String) : CollectionReference = firestoreDB.collection("logs/$logId/records")

    val userLogsLiveData = MutableLiveData<List<HitchLog>>()

    fun getUserLogs(userId: String) {
        logs().whereEqualTo("userId", userId)
            .addSnapshotListener { value, error ->
                error?.let { e ->
                    Log.e("Repository", e.message.orEmpty())
                    userLogsLiveData.value = listOf()
                    return@addSnapshotListener
                }

                if (value?.documentChanges?.isNotEmpty() == true) {
                    userLogsLiveData.postValue(
                        value.map { item ->
                            item.toObjectWithId<HitchLog>()
                        }.sortedByDescending { it.creationTime }
                    )
                }
            }
    }

    fun getLog(logId: String): MutableLiveData<HitchLog> {
        val data = MutableLiveData(HitchLog())
        logs().document(logId).get()
            .addOnSuccessListener { value ->
                if (value.exists()) {
                    data.postValue(value.toObjectWithId())
                    Log.d("Repository", "log doc ${value.toObjectWithId<HitchLog>()}")
                } else {
                    Log.e("Repository", "Document $logId doesn't exist")
                }
            }
            .addOnFailureListener {
                Log.e("Repository", it.message.orEmpty())
            }
        return data
    }

    fun addLog(log: HitchLog) {
        logs().add(log)
    }

    fun updateLog(log: HitchLog) {
        logs().document(log.id).set(log)
    }

    fun updateLogName(id: String, name: String) {
        logs().document(id).update("name", name)
    }

    fun saveLog(log: HitchLog) {
        when {
            log.id.isEmpty() -> addLog(log)
            else -> updateLog(log)
        }
    }

    fun deleteLog(id: String) {
        logs().document(id).delete()
    }

    var currentLog: HitchLog? = null
        set(value) {
            field = value
            currentLogId = value?.id
        }
        get() = userLogsLiveData.value?.find { it.id == currentLogId }

    val recordsLiveData = MutableLiveData<List<HitchLogRecord>>()

    fun getLogRecords(logId: String): MutableLiveData<List<HitchLogRecord>> {
        val data = MutableLiveData<List<HitchLogRecord>>()

        logRecords(logId)
            .orderBy("time")
            .addSnapshotListener(EventListener { value, error ->
                error?.let { e ->
                    Log.e("Repository", e.message.orEmpty())
                    data.value = listOf()
                    return@EventListener
                }

                if (value?.documentChanges?.isNotEmpty() == true) {
                    data.postValue(value.map { item ->
                        item.toObjectWithId()
                    })
                }
            })

        return data
    }

    var currentLogId: String? = null
        set(value) {
            field = value
            recordsLiveData.value = listOf()
            value?.let {
                logRecords(it)
                    .orderBy("time")
                    .addSnapshotListener(EventListener { value, error ->
                        error?.let { e ->
                            Log.e("Repository", e.message.orEmpty())
                            return@EventListener
                        }

                        if (value?.documentChanges?.isNotEmpty() == true) {
                            recordsLiveData.postValue(value.map { item ->
                                item.toObjectWithId()
                            })
                        }
                    })
            }
        }

    fun addRecord(record: HitchLogRecord) {
        currentLogId?.let {
            logRecords(it).add(record.copy(time = getNextTime(record.time)))
        } ?: Log.e("HitchLogViewModel", "No current log")
    }

    fun updateRecord(record: HitchLogRecord) {
        currentLogId?.let {
            logRecords(it).document(record.id).get()
                .onSuccessTask { doc ->
                    val existing = doc.toObjectWithId<HitchLogRecord>()
                    val updatedRecord = if (existing.time == record.time) {
                        record
                    } else {
                        record.copy(time = getNextTime(record.time))
                    }

                    logRecords(it).document(record.id).set(updatedRecord)
                }
        } ?: Log.e("HitchLogViewModel", "No current log")
    }

    fun deleteRecord(record: HitchLogRecord) {
        currentLogId?.let {
            logRecords(it).document(record.id).delete()
        } ?: Log.e("HitchLogViewModel", "No current log")
    }

    fun saveRecord(record: HitchLogRecord) {
        when {
            record.id.isEmpty() -> addRecord(record)
            else -> updateRecord(record)
        }
    }

    private fun getNextTime(enteredTime: Date): Date {
        return recordsLiveData.value?.filter {
            it.time.toMinutes() == enteredTime
        }?.maxByOrNull { it.time }?.time?.addSecond()
            ?: enteredTime
    }

    private fun Date.addSecond(): Date = Date(time + 1000)
}

@SuppressLint("SimpleDateFormat")
fun Date.toMinutes(): Date {
    val dateTimeFormat = SimpleDateFormat("dd.MM.yyyy HH:mm")
    return dateTimeFormat.parse(dateTimeFormat.format(this))!!
}