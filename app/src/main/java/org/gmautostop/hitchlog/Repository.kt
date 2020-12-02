package org.gmautostop.hitchlog

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
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
                    userLogsLiveData.value = null
                    return@addSnapshotListener
                }

                userLogsLiveData.value = value?.map { item ->
                    item.toObjectWithId<HitchLog>()
                }?.sortedByDescending { it.creationTime }
            }
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

    var currentLogId: String? = null
        set(value) {
            field = value
            recordsLiveData.value = null
            value?.let {
                logRecords(it)
                    .orderBy("time")
                    .addSnapshotListener(EventListener<QuerySnapshot> { value, error ->
                        error?.let { e ->
                            Log.e("Repository", e.message.orEmpty())
                            recordsLiveData.value = null
                            return@EventListener
                        }

                        recordsLiveData.value = value?.map { item ->
                            item.toObjectWithId<HitchLogRecord>()
                        }
                    })
            }
        }

    val recordsLiveData = MutableLiveData<List<HitchLogRecord>>()

    fun addRecord(record: HitchLogRecord) {
        currentLogId?.let {
            logRecords(it).add(record)
        } ?: Log.e("HitchLogViewModel", "No current log")
    }

    fun updateRecord(record: HitchLogRecord) {
        currentLogId?.let {
            logRecords(it).document(record.id).set(record)
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
}

@SuppressLint("SimpleDateFormat")
fun Date.toMinutes(): Date {
    val dateTimeFormat = SimpleDateFormat("dd.MM.yyyy hh:mm")
    return dateTimeFormat.parse(dateTimeFormat.format(this))!!
}